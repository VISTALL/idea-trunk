package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ChangeListSynchronizer;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.Map;

/**
 * @author yole
 */
public abstract class VcsOperation implements Cloneable {
  protected String myChangeList;

  public abstract void execute(final Project project) throws VcsException;

  public void executeOrLog(final Project project) throws VcsException {
    if (PerforceSettings.getSettings(project).ENABLED) {
      execute(project);
    }
    else {
      VcsOperationLog.getInstance(project).addToLog(this);      
    }
  }

  protected VcsOperation() {
  }

  protected VcsOperation(String changeList) {
    myChangeList = changeList;
  }

  public String getChangeList() {
    return myChangeList;
  }

  public void setChangeList(final String changeList) {
    myChangeList = changeList;
  }

  @Nullable
  public Change getChange(final Project project) {
    return null;
  }

  public void fillReopenedPaths(Map<String, String> result) {
  }

  /**
   * Checks if this operation modifies or reverts the specified operation.
   *
   * @param oldOp the operation to check for replacement.
   * @return null if this operation cancels <code>oldOp</code>; <code>oldOp</code> if the operations
   * are independent
   */
  @Nullable
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    return oldOp;
  }

  protected long getPerforceChangeList(final Project project, final P4File p4File) throws VcsException {
    final ChangeListManager listManager = ChangeListManager.getInstanceChecked(project);
    LocalChangeList list = listManager.findChangeList(myChangeList);
    if (list == null) {
      list = listManager.getDefaultChangeList();
    }
    P4Connection connection = PerforceConnectionManager.getInstanceChecked(project).getConnectionForFile(p4File);
    return ChangeListSynchronizer.getInstanceChecked(project).findOrCreatePerforceChangeList(connection, list);
  }

  public void prepareOffline() {
    //To change body of created methods use File | Settings | File Templates.
  }

  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
