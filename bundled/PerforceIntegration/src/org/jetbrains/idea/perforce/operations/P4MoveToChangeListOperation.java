package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;
import java.util.Map;

/**
 * @author yole
 */
public class P4MoveToChangeListOperation extends VcsOperationOnPath {
  public P4MoveToChangeListOperation() {
  }

  public P4MoveToChangeListOperation(Change c, String changeList) {
    super(changeList, ChangesUtil.getFilePath(c).getPath());
  }

  public void execute(final Project project) throws VcsException {
    File f = new File(myPath);
    long changeListNumber = getPerforceChangeList(project, P4File.createInefficientFromLocalPath(myPath));
    PerforceRunner.getInstance(project).reopen(new File[] { f }, changeListNumber);
  }

  public void fillReopenedPaths(final Map<String, String> result) {
    result.put(myPath, myChangeList);
  }

  @Override
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    if ((oldOp instanceof VcsOperationOnPath && ((VcsOperationOnPath) oldOp).getPath().equals(myPath)) ||
        (oldOp instanceof P4MoveRenameOperation && ((P4MoveRenameOperation) oldOp).getNewPath().equals(myPath))) {
      VcsOperation clone = (VcsOperation) oldOp.clone();
      clone.setChangeList(myChangeList);
      return clone;
    }
    return super.checkMerge(oldOp);
  }
}