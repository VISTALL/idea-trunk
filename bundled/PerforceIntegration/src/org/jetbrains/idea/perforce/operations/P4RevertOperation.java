package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;
import java.util.Map;

/**
 * @author yole
 */
public class P4RevertOperation extends VcsOperation {
  private String myBeforePath;
  private String myAfterPath;

  public P4RevertOperation() {
  }

  public P4RevertOperation(Change c) {
    FilePath beforePath = ChangesUtil.getBeforePath(c);
    FilePath afterPath = ChangesUtil.getAfterPath(c);
    myBeforePath = (beforePath == null) ? null : beforePath.getPath();
    myAfterPath = (afterPath == null) ? null : afterPath.getPath();
  }

  public String getBeforePath() {
    return myBeforePath;
  }

  public void setBeforePath(final String beforePath) {
    myBeforePath = beforePath;
  }

  public String getAfterPath() {
    return myAfterPath;
  }

  public void setAfterPath(final String afterPath) {
    myAfterPath = afterPath;
  }

  public void execute(final Project project) throws VcsException {
    PerforceRunner runner = PerforceRunner.getInstance(project);
    if (isRenameOrMove()) {
      runner.revert(P4File.createInefficientFromLocalPath(myBeforePath), true);
      runner.revert(P4File.createInefficientFromLocalPath(myAfterPath), true);
      FileUtil.delete(new File(myAfterPath));
    }
    else if (myBeforePath != null) {
      runner.revert(P4File.createInefficientFromLocalPath(myBeforePath), true);
    }
    else {
      assert myAfterPath != null;
      runner.revert(P4File.createInefficientFromLocalPath(myAfterPath), true);
    }
  }

  private boolean isRenameOrMove() {
    return myBeforePath != null && myAfterPath != null && !FileUtil.pathsEqual(myBeforePath, myAfterPath);
  }

  @Override
  public void fillReopenedPaths(final Map<String, String> result) {
    if (myBeforePath != null) {
      result.put(myBeforePath, null);
    }
    if (myAfterPath != null && (myBeforePath == null || !FileUtil.pathsEqual(myBeforePath, myAfterPath))) {
      result.put(myAfterPath, null);
    }
  }

  @Nullable
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    if (!isRenameOrMove()) {
      if (oldOp instanceof P4EditOperation || oldOp instanceof P4AddOperation) {
        VcsOperationOnPath opOnPath = (VcsOperationOnPath) oldOp;
        if (FileUtil.pathsEqual(opOnPath.getPath(), myAfterPath)) {
          return null;
        }
      }
    }
    else if (oldOp instanceof P4MoveRenameOperation) {
      P4MoveRenameOperation renameOp = (P4MoveRenameOperation) oldOp;
      if (FileUtil.pathsEqual(renameOp.getNewPath(), myAfterPath)) {
        return null;
      }
    }
    return super.checkMerge(oldOp);
  }
}