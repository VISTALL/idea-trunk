package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.components.*;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import org.jetbrains.idea.perforce.PerforceBundle;

import java.util.*;

/**
 * @author yole
 */
@State(
  name="VcsOperationLog",
  roamingType = RoamingType.DISABLED,
  storages= {
    @Storage(
      id="other",
      file = "$WORKSPACE_FILE$"
    )}
)
public class VcsOperationLog implements PersistentStateComponent<VcsOperationLog.OperationList>{
  public static class OperationList {
    @AbstractCollection(
      elementTypes = {
        P4AddOperation.class, P4CopyOperation.class, P4DeleteOperation.class, P4MoveRenameOperation.class, P4EditOperation.class,
        P4RevertOperation.class, P4MoveToChangeListOperation.class
      })
    public List<VcsOperation> operations = new ArrayList<VcsOperation>();
  }

  private final Project myProject;
  private OperationList myOperations = new OperationList();

  public VcsOperationLog(final Project project) {
    myProject = project;
  }

  public static VcsOperationLog getInstance(Project project) {
    return ServiceManager.getService(project, VcsOperationLog.class);
  }

  public OperationList getState() {
    return myOperations;
  }

  public void loadState(OperationList state) {
    myOperations = state;
  }

  public void addToLog(final VcsOperation vcsOperation) {
    for(int i=0; i<myOperations.operations.size(); i++) {
      VcsOperation oldOp = myOperations.operations.get(i);
      VcsOperation mergedOp = vcsOperation.checkMerge(oldOp);
      if (mergedOp != oldOp) {
        if (mergedOp == null) {
          myOperations.operations.remove(i);
        }
        else {
          myOperations.operations.set(i, mergedOp);
        }
        return;
      }
    }
    vcsOperation.prepareOffline();
    myOperations.operations.add(vcsOperation);
  }

  public void replayLog() {
    final List<VcsException> exceptions = new ArrayList<VcsException>();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        for(VcsOperation op: myOperations.operations) {
          try {
            op.execute(myProject);
          }
          catch (VcsException e) {
            exceptions.add(e);
          }
        }
      }
    }, PerforceBundle.message("replaying.offline.operations"), false, myProject);
    if (!exceptions.isEmpty()) {
      AbstractVcsHelper.getInstance(myProject).showErrors(exceptions, PerforceBundle.message("replaying.offline.operations"));
    }
    myOperations.operations.clear();
  }

  public List<VcsOperation> getPendingOperations() {
    return Collections.unmodifiableList(myOperations.operations);
  }

  /**
   * Returns a map from a file path to the name of the changelist in which the file was reopened while offline.
   * If the value is null, the file has been reverted while offline.
   *
   * @return map of path to changelist name or null
   */
  public Map<String, String> getReopenedPaths() {
    Map<String, String> result = new TreeMap<String, String>();
    for(VcsOperation op: myOperations.operations) {
      op.fillReopenedPaths(result);
    }
    return result;
  }
}