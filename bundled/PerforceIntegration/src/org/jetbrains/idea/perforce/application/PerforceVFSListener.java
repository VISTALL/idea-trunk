package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsVFSListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.util.Processor;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4AddOperation;
import org.jetbrains.idea.perforce.operations.P4CopyOperation;
import org.jetbrains.idea.perforce.operations.P4MoveRenameOperation;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.util.*;

/**
 * @author yole
 */
public class PerforceVFSListener extends VcsVFSListener {
  private final FileTypeManager myFileTypeManager;

  public PerforceVFSListener(final Project project) {
    super(project, PerforceVcs.getInstance(project));
    myFileTypeManager = FileTypeManager.getInstance();
  }

  @Override
  protected boolean isEventIgnored(final VirtualFileEvent event) {
    return super.isEventIgnored(event) || event.getRequestor() instanceof PerforceOfflineRollbackEnvironment;
  }

  protected void performAdding(final Collection<VirtualFile> addedFiles, final Map<VirtualFile, VirtualFile> copyFromMap) {
    VcsBackgroundTask<VirtualFile> task = new VcsBackgroundTask<VirtualFile>(myProject,
                                                                             PerforceBundle.message("progress.title.running.perforce.commands"),
                                                                             VcsConfiguration.getInstance(myProject).getAddRemoveOption(),
                                                                             addedFiles) {
      protected void process(final VirtualFile item) throws VcsException {
        createdVFile(item, copyFromMap);
      }
    };

    PerforceVcs.getInstance(myProject).runTask(task);
  }

  private void createdVFile(final VirtualFile vFile, final Map<VirtualFile, VirtualFile> copyFromMap) throws VcsException {
    final String defaultListName = myChangeListManager.getDefaultChangeList().getName();

    final Ref<VcsException> excRef = new Ref<VcsException>();

    final Processor<VirtualFile> fileProcessor = new Processor<VirtualFile>() {
      public boolean process(VirtualFile virtualFile) {
        if (virtualFile.isDirectory()) return true;

        final VirtualFile copyFrom = copyFromMap.get(virtualFile);
        try {
          if (copyFrom != null && !isAdding(copyFrom)) {
            new P4CopyOperation(defaultListName, virtualFile, copyFrom).executeOrLog(myProject);
          }
          else {
            new P4AddOperation(defaultListName, virtualFile).executeOrLog(myProject);
          }
        }
        catch (VcsException e) {
          excRef.set(e);
          // interrupt execution
          return false;
        }
        return true;
      }
    };

    if (vFile.isDirectory()) {
      VfsUtil.processFileRecursivelyWithoutIgnored(vFile, fileProcessor);
      return;
    } else {
      fileProcessor.process(vFile);
    }

    if (! excRef.isNull()) {
      throw excRef.get();
    }
  }

  private boolean isAdding(final VirtualFile copyFrom) throws VcsException {
    if (!PerforceSettings.getSettings(myProject).ENABLED) {
      return false;
    }
    final FStat sourceFStat = P4File.create(copyFrom).getFstat(myProject, true);
    return sourceFStat.local == FStat.LOCAL_ADDING;
  }

  protected String getSingleFileAddPromptTemplate() {
    return PerforceBundle.message("confirmation.text.add.files");
  }

  protected String getSingleFileAddTitle() {
    return PerforceBundle.message("confirmation.title.add.files");
  }

  protected String getAddTitle() {
    return PerforceBundle.message("add.select.files");
  }

  @Override
  protected VcsDeleteType needConfirmDeletion(final VirtualFile file) {
    final P4File p4File = P4File.create(file);

    if (PerforceSettings.getSettings(myProject).ENABLED) {
      try {
        final FStat fstat = p4File.getFstat(myProject, true);

        if (fstat.status == FStat.STATUS_NOT_ADDED ||
            fstat.status == FStat.STATUS_NOT_IN_CLIENTSPEC ||
            fstat.status == FStat.STATUS_UNKNOWN ||
            fstat.local == FStat.LOCAL_NOT_LOCAL) {
          return VcsDeleteType.IGNORE;
        }

        if (fstat.status == FStat.STATUS_ONLY_LOCAL) {
          return VcsDeleteType.SILENT;
        }
      }
      catch (VcsException e) {
        //ignore
      }
    }
    return VcsDeleteType.CONFIRM;
  }

  protected void performDeletion(final List<FilePath> filesToDelete) {
    PerforceVcs.getInstance(myProject).getOfflineCheckinEnvironment().scheduleMissingFileForDeletion(filesToDelete);
    final Set<FilePath> dirtyParentDirs = new HashSet<FilePath>();
    for (FilePath path : filesToDelete) {
      final FilePath parent = path.getParentPath();
      if (parent != null) {
        dirtyParentDirs.add(parent);
      }
    }
    VcsDirtyScopeManager.getInstance(myProject).filePathsDirty(null, dirtyParentDirs);
  }

  protected String getSingleFileDeletePromptTemplate() {
    return PerforceBundle.message("confirmation.text.remove.files");
  }

  protected String getSingleFileDeleteTitle() {
    return PerforceBundle.message("confirmation.title.remove.files");
  }

  protected String getDeleteTitle() {
    return PerforceBundle.message("delete.select.files");
  }

  protected void processMovedFile(final VirtualFile file, final String newParentPath, final String newName) {
    P4File.invalidateFstat(file);
    super.processMovedFile(file, newParentPath, newName);
  }

  protected void performMoveRename(final List<MovedFileInfo> movedFiles) {
    VcsBackgroundTask<MovedFileInfo> task = new VcsBackgroundTask<MovedFileInfo>(myProject,
                                                                                 PerforceBundle.message("progress.title.running.perforce.commands"),
                                                                                 VcsConfiguration.getInstance(myProject).getAddRemoveOption(),
                                                                                 new ArrayList<MovedFileInfo>(movedFiles)) {
      protected void process(final MovedFileInfo info) throws VcsException {
        new P4MoveRenameOperation(ChangeListManager.getInstance(myProject).getDefaultChangeList().getName(),
                                  info.myOldPath, info.myNewPath).executeOrLog(myProject);
      }
    };

    PerforceVcs.getInstance(myProject).runTask(task);
  }

  protected boolean isDirectoryVersioningSupported() {
    return false;
  }
}
