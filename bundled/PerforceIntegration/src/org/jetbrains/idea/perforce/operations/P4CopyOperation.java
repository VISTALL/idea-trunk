package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import com.intellij.vcsUtil.ActionWithTempFile;

import java.io.File;

/**
 * @author yole
 */
public class P4CopyOperation extends VcsOperationOnPath {
  private String mySourcePath;

  public P4CopyOperation() {
  }

  public P4CopyOperation(String changeList, final VirtualFile vFile, final VirtualFile copyFrom) {
    super(changeList, vFile.getPath());
    mySourcePath = copyFrom.getPath();
  }

  public String getSourcePath() {
    return mySourcePath;
  }

  public void setSourcePath(final String sourcePath) {
    mySourcePath = sourcePath;
  }

  public void execute(final Project project) throws VcsException {
    final P4File sourceFile = P4File.createInefficientFromLocalPath(mySourcePath);
    final FStat sourceFStat = sourceFile.getFstat(project, false);
    if ((sourceFStat.status != FStat.STATUS_NOT_ADDED && sourceFStat.status != FStat.STATUS_ONLY_LOCAL) ||
        (sourceFStat.local == FStat.LOCAL_ADDING) || (sourceFStat.local == FStat.LOCAL_MOVE_ADDING)) {
      final P4File targetFile = P4File.createInefficientFromLocalPath(myPath);
      final PerforceRunner runner = PerforceRunner.getInstance(project);
      new ActionWithTempFile(new File(targetFile.getLocalPath())) {
        protected void executeInternal() throws VcsException {
          //TODO[yole] assureNoFile(newP4File, false, true);

          runner.integrate(sourceFile, targetFile);
          runner.edit(targetFile);
        }
      }.execute();
      VcsUtil.markFileAsDirty(project, myPath);
    }
  }
}
