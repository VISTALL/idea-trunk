package org.jetbrains.idea.perforce.application;

import com.intellij.ide.errorTreeView.ErrorTreeElementKind;
import com.intellij.ide.errorTreeView.HotfixData;
import com.intellij.ide.errorTreeView.HotfixGate;
import com.intellij.ide.errorTreeView.SimpleErrorData;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ActionType;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsExceptionsHotFixer;
import com.intellij.openapi.vcs.changes.BackgroundFromStartOption;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import com.intellij.openapi.vfs.newvfs.RefreshSession;
import com.intellij.util.Consumer;
import com.intellij.util.ui.MutableErrorTreeView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;
import java.util.*;

public class PerforceExceptionsHotFixer implements VcsExceptionsHotFixer {
  private final MyListChecker myUpdateChecker;
  private final Project myProject;

  public PerforceExceptionsHotFixer(final Project project) {
    myProject = project;
    List<MyChecker> myCheckers = Arrays.<MyChecker>asList(new MyClobberWriteableChecker(project));
    myUpdateChecker = new MyListChecker(myCheckers);
  }

  public Map<HotfixData, List<VcsException>> groupExceptions(final ActionType type, List<VcsException> exceptions) {
    if (ActionType.update.equals(type)) {
      return myUpdateChecker.process(exceptions);
    }
    return null;
  }

  private static List<VcsException> getOrCreate(final Map<HotfixData, List<VcsException>> map, final HotfixData data) {
    List<VcsException> list = map.get(data);
    if (list == null) {
      list = new ArrayList<VcsException>();
      map.put(data, list);
    }
    return list;
  }

  private static class MyListChecker {
    private final List<MyChecker> myCheckers;
    private List<VcsException> myDefault;

    private MyListChecker(final List<MyChecker> checkers) {
      myCheckers = checkers;
    }

    public Map<HotfixData, List<VcsException>> process(final List<VcsException> list) {
      final Map<HotfixData, List<VcsException>> result = new HashMap<HotfixData, List<VcsException>>();
      for (VcsException exception : list) {
        boolean found = false;
        for (MyChecker checker : myCheckers) {
          if (checker.check(exception)) {
            final List<VcsException> excList = getOrCreate(result, checker.getKey());
            excList.add(checker.convert(exception));
            found = true;
            break;
          }
        }
        if (! found) {
          if (myDefault == null) {
            myDefault = new ArrayList<VcsException>();
          }
          myDefault.add(exception);
        }
      }
      if (myDefault != null) {
        result.put(null, myDefault);
      }

      return result;
    }
  }

  private static abstract class MyChecker {
    private final HotfixData myKey;

    public MyChecker(HotfixData key) {
      myKey = key;
    }

    protected abstract boolean check(final VcsException exc);

    public VcsException convert(final VcsException e) {
      return e;
    }

    public HotfixData getKey() {
      return myKey;
    }
  }

  private static class MyClobberWriteableHotfix implements Consumer<HotfixGate> {
    private final static String ourClobberWriteable = "Can't clobber writable file(s)";
    private final Project myProject;
    private final VcsDirtyScopeManager myDirtyScopeManager;

    public MyClobberWriteableHotfix(Project project) {
      myProject = project;
      myDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject);
    }

    public void consume(final HotfixGate hotfixGate) {
      ProgressManager.getInstance().run(new Task.Backgroundable(myProject, "Opening files for edit", true,
                                                                BackgroundFromStartOption.getInstance()) {
        public void run(@NotNull ProgressIndicator indicator) {
          final String name = hotfixGate.getGroupName();
          final java.util.List<Object> childData = hotfixGate.getView().getGroupChildrenData(name);

          final List<SimpleErrorData> processed = new ArrayList<SimpleErrorData>();
          final List<VirtualFile> processedFiles = new ArrayList<VirtualFile>();
          final List<SimpleErrorData> failed = new ArrayList<SimpleErrorData>();
          try {
            edit(childData, processed, processedFiles, failed);
          }
          catch (ProcessCanceledException e) {
            for (Object child : childData) {
              if (child instanceof VirtualFile) {
                final VirtualFile vf = (VirtualFile)child;
                failed.add(createErrorData(vf, "Operation canceled"));
              }
            }
          }

          final MutableErrorTreeView view = hotfixGate.getView();
          view.removeGroup(name);

          if (! processed.isEmpty()) {
            view.addFixedHotfixGroup(ourClobberWriteable, processed);
            refreshVfs(processedFiles);
          }
          if (! failed.isEmpty()) {
            view.addHotfixGroup(new HotfixData(MyHotfixes.FIX_CLOBBER_WRITEABLES, ourClobberWriteable,
                                 " open for edit keeping local changes", MyClobberWriteableHotfix.this), failed);
          }
        }

        @Override
        public void onCancel() {
          onSuccess();
        }

        @Override
        public void onSuccess() {
          hotfixGate.getView().reload();
        }
      });
    }

    private void refreshVfs(final List<VirtualFile> processedFiles) {
      final RefreshSession session = RefreshQueue.getInstance().createSession(true, false, new Runnable() {
        public void run() {
          myDirtyScopeManager.filesDirty(processedFiles, null);
        }
      });
      session.addAllFiles(processedFiles);
      session.launch();
    }

    private void edit(final List<Object> childData, final List<SimpleErrorData> processed, final List<VirtualFile> processedFiles,
                      final List<SimpleErrorData> failed) {
      final PerforceRunner runner = PerforceRunner.getInstance(myProject);
      final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();

      for (Object child : childData) {
        if (child instanceof VirtualFile) {
          final VirtualFile vf = (VirtualFile)child;
          if (indicator != null) {
            indicator.checkCanceled();
          }
          final P4File p4File = P4File.create(new FilePathImpl(vf));
          try {
            final FStat p4FStat = p4File.getFstat(myProject, true);

            if ((FStat.STATUS_ON_SERVER_AND_LOCAL != p4FStat.status) || (FStat.LOCAL_CHECKED_IN != p4FStat.local)) {
              failed.add(createErrorData(vf, "Skipped"));
            } else {
              final String complaint = PerforceVcs.getFileNameComplaint(p4File);
              if (complaint != null) {
                failed.add(createErrorData(vf, PerforceBundle.message("message.text.filename.non.acceptable", complaint)));
              } else {
                runner.edit(p4File);
                processed.add(createFixedData(vf));
                processedFiles.add(vf);
              }
            }
          }
          catch (VcsException e) {
            failed.add(createErrorData(vf, e.getMessage()));
            continue;
          }
        }
      }
    }

    private SimpleErrorData createFixedData(final VirtualFile vf) {
      return new SimpleErrorData(ErrorTreeElementKind.ERROR, new String[] {vf.getPath()}, vf);
    }

    private SimpleErrorData createErrorData(final VirtualFile vf, final String comment) {
      // todo message format
      return new SimpleErrorData(ErrorTreeElementKind.ERROR, new String[] {vf.getPath() + " (fix failed: " + comment + ")"}, vf);
    }
  }

    private static class MyClobberWriteableChecker extends MyChecker {
      private final static String ourClobberWriteable = "Can't clobber writable file";
      private final Project myProject;

      private MyClobberWriteableChecker(final Project project) {
      super(new HotfixData(MyHotfixes.FIX_CLOBBER_WRITEABLES, "Can't clobber writable file(s)",
                           " open for edit keeping local changes", new MyClobberWriteableHotfix(project)));
        myProject = project;
      }

      @Override
      public VcsException convert(final VcsException e) {
        final VirtualFile vf = e.getVirtualFile();
        if (vf == null) {
          return e;
        }
        final VcsException newE = new VcsException(vf.getPath());
        newE.setVirtualFile(vf);
        return newE;
      }

      protected boolean check(final VcsException exc) {
      final String[] messages = exc.getMessages();
      if (messages != null && messages.length > 0 && messages[0].startsWith(ourClobberWriteable)) {

        String filePathCandidate = messages[0].substring(ourClobberWriteable.length(), messages[0].length());
        filePathCandidate = PerforceManager.getInstance(myProject).convertP4ParsedPath(null, filePathCandidate);
        filePathCandidate = FileUtil.toSystemDependentName(filePathCandidate);
        LocalFileSystem lfs = LocalFileSystem.getInstance();
        File ioFile = new File(filePathCandidate);
        VirtualFile vf = lfs.findFileByIoFile(ioFile);
        if (vf == null) {
          vf = lfs.refreshAndFindFileByIoFile(ioFile);
        }
        if (vf != null) {
          exc.setVirtualFile(vf);
          return true;
        }
      }
      return false;
    }
  }

  private interface MyHotfixes {
    String FIX_CLOBBER_WRITEABLES = "FIX_CLOBBER_WRITEABLES";
  }
}
