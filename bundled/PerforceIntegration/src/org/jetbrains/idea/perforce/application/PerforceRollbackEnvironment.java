package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4RevertOperation;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.util.List;

/**
 * @author yole
 */
public class PerforceRollbackEnvironment implements RollbackEnvironment {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceRollbackEnvironment");

  private final Project myProject;
  private final PerforceRunner myRunner;

  public PerforceRollbackEnvironment(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
  }

  public String getRollbackOperationName() {
    return PerforceBundle.message("operation.name.revert");
  }

  public void rollbackChanges(List<Change> changes, final List<VcsException> vcsExceptions, @NotNull final RollbackProgressListener listener) {
    listener.determinate();
    
    for (Change change : changes) {
      listener.accept(change);
      try {
        new P4RevertOperation(change).execute(myProject);
      }
      catch (VcsException e) {
        vcsExceptions.add(e);
      }
    }
  }

  public void rollbackMissingFileDeletion(List<FilePath> files, final List<VcsException> exceptions,
                                                        final RollbackProgressListener listener) {
    for (FilePath file : files) {
      listener.accept(file);
      try {
        P4File p4file = P4File.create(file);
        FStat fStat;
        try {
          fStat = p4file.getFstat(myProject, true);
        }
        catch (VcsException e) {
          continue;
        }
        if (fStat.local == FStat.LOCAL_CHECKED_OUT || fStat.local == FStat.LOCAL_INTEGRATING) {
          myRunner.revert(p4file, false);
        }
        else {
          myRunner.sync(p4file, true);
        }
      }
      catch (VcsException e) {
        exceptions.add(e);
      }
    }
  }

  public void rollbackModifiedWithoutCheckout(final List<VirtualFile> files, final List<VcsException> exceptions,
                                                            final RollbackProgressListener listener) {
    for(VirtualFile file: files) {
      listener.accept(file);
      P4File p4File = P4File.create(file);
      try {
        myRunner.edit(p4File);
        myRunner.revert(p4File, false);
      }
      catch (VcsException e) {
        exceptions.add(e);
      }
    }
  }

  public void rollbackIfUnchanged(final VirtualFile file) {
    Task.Backgroundable rollbackTask =
      new Task.Backgroundable(myProject, PerforceBundle.message("progress.title.reverting.unmodified.file")) {
        public void run(@NotNull final ProgressIndicator indicator) {
          try {
            final boolean reverted = PerforceRunner.getInstance(myProject).revertUnchanged(P4File.create(file));
            if (reverted) {
              file.refresh(true, false, new Runnable() {
                public void run() {
                  VcsDirtyScopeManager.getInstance(myProject).fileDirty(file);
                }
              });
            }
          }
          catch (VcsException e) {
            // ignore
            LOG.debug(e);
          }
        }
      };
    PerforceVcs.getInstance(myProject).runTask(rollbackTask);
  }

}
