package org.jetbrains.idea.svn.rollback;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.EmptyChangelistBuilder;
import com.intellij.openapi.vcs.rollback.DefaultRollbackEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.MoveRenameReplaceCheck;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnChangeProvider;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.util.*;

/**
 * @author yole
 */
public class SvnRollbackEnvironment extends DefaultRollbackEnvironment {
  private final SvnVcs mySvnVcs;

  public SvnRollbackEnvironment(SvnVcs svnVcs) {
    mySvnVcs = svnVcs;
  }

  @Override
  public String getRollbackOperationName() {
    return SvnBundle.message("action.name.revert");
  }

  public void rollbackChanges(List<Change> changes, final List<VcsException> exceptions, @NotNull final RollbackProgressListener listener) {
    listener.indeterminate();
    final SvnChangeProvider changeProvider = (SvnChangeProvider) mySvnVcs.getChangeProvider();
    
    final UnversionedFilesGroupCollector collector = new UnversionedFilesGroupCollector();

    final ChangesChecker checker = new ChangesChecker(changeProvider, collector);
    checker.gather(changes);
    exceptions.addAll(checker.getExceptions());

      final SVNWCClient client = mySvnVcs.createWCClient();
      client.setEventHandler(new ISVNEventHandler() {
        public void handleEvent(SVNEvent event, double progress) {
          if (event.getAction() == SVNEventAction.REVERT) {
            final File file = event.getFile();
            if (file != null) {
              listener.accept(file);
            }
          }
          if (event.getAction() == SVNEventAction.FAILED_REVERT) {
            exceptions.add(new VcsException("Revert failed"));
          }
        }

        public void checkCancelled() {
          listener.checkCanceled();
        }
      });

    // adds (deletes)
    // deletes (adds)
    // modifications
    final Reverter reverter = new Reverter(client, exceptions);
    reverter.revert(checker.getForAdds(), true);
    reverter.revert(checker.getForDeletes(), true);
    final List<File> edits = checker.getForEdits();
    reverter.revert(edits.toArray(new File[edits.size()]), false);

    final List<Trinity<File, File, File>> fromTo = collector.getFromTo();
    for (Trinity<File, File, File> trinity : fromTo) {
      if (trinity.getFirst().exists()) {
        // parent successfully renamed/moved
        trinity.getSecond().renameTo(trinity.getThird());
      }
    }
    final List<Pair<File, File>> toBeDeleted = collector.getToBeDeleted();
    for (Pair<File, File> pair : toBeDeleted) {
      if (pair.getFirst().exists()) {
        FileUtil.delete(pair.getSecond());
      }
    }
  }

  private static class Reverter {
    private final SVNWCClient myClient;
    private final List<VcsException> myExceptions;

    private Reverter(SVNWCClient client, List<VcsException> exceptions) {
      myClient = client;
      myExceptions = exceptions;
    }

    public void revert(final File[] files, final boolean recursive) {
      if (files.length == 0) return;
      try {
        myClient.doRevert(files, recursive);
      }
      catch (SVNException e) {
        if (e.getErrorMessage().getErrorCode() != SVNErrorCode.WC_NOT_DIRECTORY) {
          // skip errors on unversioned resources.
          myExceptions.add(new VcsException(e));
        }
      }
    }
  }

  public void rollbackMissingFileDeletion(List<FilePath> filePaths, final List<VcsException> exceptions,
                                                        final RollbackProgressListener listener) {
    final SVNWCClient wcClient = mySvnVcs.createWCClient();

    List<File> files = ChangesUtil.filePathsToFiles(filePaths);
    for (File file : files) {
      listener.accept(file);
      try {
        SVNInfo info = wcClient.doInfo(file, SVNRevision.BASE);
        if (info != null && info.getKind() == SVNNodeKind.FILE) {
          wcClient.doRevert(file, false);
        } else {
          // do update to restore missing directory.
          mySvnVcs.createUpdateClient().doUpdate(file, SVNRevision.HEAD, true);
        }
      }
      catch (SVNException e) {
        exceptions.add(new VcsException(e));
      }
    }
  }

  private static class UnversionedFilesGroupCollector extends EmptyChangelistBuilder {
    private File myCurrentBeforeFile;
    private final List<Pair<File, File>> myToBeDeleted;
    private final List<Trinity<File, File, File>> myFromTo;

    private UnversionedFilesGroupCollector() {
      myFromTo = new ArrayList<Trinity<File, File, File>>();
      myToBeDeleted = new ArrayList<Pair<File, File>>();
    }

    @Override
    public void processUnversionedFile(final VirtualFile file) {
      final File to = new File(myCurrentBeforeFile, file.getName());
      myFromTo.add(new Trinity<File, File, File>(myCurrentBeforeFile, new File(file.getPath()), to));
    }

    public void setBefore(@NotNull final File beforeFile, @NotNull final File afterFile) {
      myCurrentBeforeFile = beforeFile;
      myToBeDeleted.add(new Pair<File, File>(beforeFile, afterFile));
    }

    public List<Pair<File, File>> getToBeDeleted() {
      return myToBeDeleted;
    }

    public List<Trinity<File, File, File>> getFromTo() {
      return myFromTo;
    }
  }

  // both adds and deletes
  private static abstract class SuperfluousRemover {
    private final Set<File> myParentPaths;

    private SuperfluousRemover() {
      myParentPaths = new HashSet<File>();
    }

    @Nullable
    protected abstract File accept(final Change change);

    public void check(final File file) {
      for (Iterator<File> iterator = myParentPaths.iterator(); iterator.hasNext();) {
        final File parentPath = iterator.next();
        if (VfsUtil.isAncestor(parentPath, file, true)) {
          return;
        } else if (VfsUtil.isAncestor(file, parentPath, true)) {
          iterator.remove();
          // remove others; dont check for 1st variant any more
          for (; iterator.hasNext();) {
            final File innerParentPath = iterator.next();
            if (VfsUtil.isAncestor(file, innerParentPath, true)) {
              iterator.remove();
            }
          }
          // will be added in the end
        }
      }
      myParentPaths.add(file);
    }

    public Set<File> getParentPaths() {
      return myParentPaths;
    }
  }

  private static class ChangesChecker {
    private final SuperfluousRemover myForAdds;
    private final SuperfluousRemover myForDeletes;
    private final List<File> myForEdits;

    private final SvnChangeProvider myChangeProvider;
    private final UnversionedFilesGroupCollector myCollector;

    private final List<VcsException> myExceptions;

    private ChangesChecker(SvnChangeProvider changeProvider, UnversionedFilesGroupCollector collector) {
      myChangeProvider = changeProvider;
      myCollector = collector;

      myForAdds = new SuperfluousRemover() {
        @Nullable
        @Override
        protected File accept(Change change) {
          final ContentRevision beforeRevision = change.getBeforeRevision();
          final ContentRevision afterRevision = change.getAfterRevision();
          if (beforeRevision == null || MoveRenameReplaceCheck.check(change)) {
            return afterRevision.getFile().getIOFile();
          }
          return null;
        }
      };

      myForDeletes = new SuperfluousRemover() {
        @Nullable
        @Override
        protected File accept(Change change) {
          final ContentRevision beforeRevision = change.getBeforeRevision();
          final ContentRevision afterRevision = change.getAfterRevision();
          if (afterRevision == null || MoveRenameReplaceCheck.check(change)) {
            return beforeRevision.getFile().getIOFile();
          }
          return null;
        }
      };

      myForEdits = new ArrayList<File>();
      myExceptions = new ArrayList<VcsException>();
    }

    public void gather(final List<Change> changes) {
      for (Change change : changes) {
        final ContentRevision beforeRevision = change.getBeforeRevision();
        final ContentRevision afterRevision = change.getAfterRevision();

        if (MoveRenameReplaceCheck.check(change)) {
          myCollector.setBefore(beforeRevision.getFile().getIOFile(), afterRevision.getFile().getIOFile());
          try {
            myChangeProvider.getChanges(afterRevision.getFile(), false, myCollector);
          }
          catch (SVNException e) {
            myExceptions.add(new VcsException(e));
          }
        }

        boolean checked = getAddDelete(myForAdds, change);
        checked |= getAddDelete(myForDeletes, change);

        if (! checked) {
          myForEdits.add(afterRevision.getFile().getIOFile());
        }
      }
    }

    private boolean getAddDelete(final SuperfluousRemover superfluousRemover, final Change change) {
      final File file = superfluousRemover.accept(change);
      if (file != null) {
        superfluousRemover.check(file);
        return true;
      }
      return false;
    }

    public File[] getForAdds() {
      return convert(myForAdds.getParentPaths());
    }

    public File[] getForDeletes() {
      return convert(myForDeletes.getParentPaths());
    }

    private File[] convert(final Collection<File> paths) {
      return paths.toArray(new File[paths.size()]);
    }

    public List<VcsException> getExceptions() {
      return myExceptions;
    }

    public List<File> getForEdits() {
      return myForEdits;
    }
  }
}
