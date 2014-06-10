package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.rollback.DefaultRollbackEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4RevertOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * @author yole
 */
public class PerforceOfflineRollbackEnvironment extends DefaultRollbackEnvironment {
  private final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceOfflineRollbackEnvironment");

  private final Project myProject;

  public PerforceOfflineRollbackEnvironment(final Project project) {
    myProject = project;
  }

  @Override
  public String getRollbackOperationName() {
    return PerforceBundle.message("operation.name.revert");
  }

  public void rollbackChanges(List<Change> changes, final List<VcsException> exceptions, @NotNull final RollbackProgressListener listener) {
    listener.determinate();
    
    for (Change c: changes) {
      listener.accept(c);
      
      final ContentRevision beforeRevision = c.getBeforeRevision();
      if (beforeRevision != null) {
        try {
          final String content = beforeRevision.getContent();
          final ContentRevision afterRevision = c.getAfterRevision();
          final boolean isRename;
          final VirtualFile file;
          if (afterRevision != null && !afterRevision.getFile().equals(beforeRevision.getFile())) {
            file = afterRevision.getFile().getVirtualFile();
            isRename = true;
          }
          else {
            file = beforeRevision.getFile().getVirtualFile();
            isRename = false;
          }
          if (content != null && file != null) {
            VcsOperationLog.getInstance(myProject).addToLog(new P4RevertOperation(c));
            Runnable r = new Runnable() {
              public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                  public void run() {
                    if (myProject.isDisposed()) return;
                    try {
                      VfsUtil.saveText(file, content);
                      if (isRename) {
                        final String oldName = beforeRevision.getFile().getName();
                        if (!file.getName().equals(oldName)) {
                          file.rename(this, oldName);
                        }
                        VcsDirtyScopeManager.getInstance(myProject).fileDirty(beforeRevision.getFile());
                        VcsDirtyScopeManager.getInstance(myProject).fileDirty(afterRevision.getFile());
                      }
                      ((NewVirtualFile) file).setWritable(false);
                    }
                    catch (IOException e) {
                      // TODO[yole]: better reporting
                      LOG.error(e);
                    }
                  }
                });
              }
            };
            if (ApplicationManager.getApplication().isUnitTestMode()) {
              r.run();
            }
            else {
              ApplicationManager.getApplication().invokeLater(r);
            }
          }
          else {
            exceptions.add(new VcsException("Cannot revert file " + beforeRevision.getFile() + ": original content is not available offline"));
          }
        }
        catch(VcsException ex) {
          exceptions.add(ex);
        }
      }
      else {
        VcsOperationLog.getInstance(myProject).addToLog(new P4RevertOperation(c));
      }
    }
  }

  public void rollbackMissingFileDeletion(List<FilePath> files, final List<VcsException> exceptions,
                                                        final RollbackProgressListener listener) {
    throw new UnsupportedOperationException();
  }
}