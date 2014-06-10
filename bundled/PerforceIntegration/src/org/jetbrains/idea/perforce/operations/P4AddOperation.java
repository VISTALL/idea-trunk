package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.application.PerforceVcs;

/**
 * @author yole
 */
public class P4AddOperation extends VcsOperationOnPath {
  public P4AddOperation() {
  }

  public P4AddOperation(String changeList, String path) {
    super(changeList, path);
  }

  public P4AddOperation(String changeList, final VirtualFile file) {
    super(changeList, file.getPath());
  }

  public void execute(final Project project) throws VcsException {
    final P4File p4File = P4File.createInefficientFromLocalPath(myPath);

    String complaint = PerforceVcs.getFileNameComplaint(p4File);
    if (complaint != null) {
      throw new VcsException(complaint);
    }

    // check whether it will be under any clientspec
    final FStat p4FStat = p4File.getFstat(project, true);
    if (p4FStat.status == FStat.STATUS_NOT_IN_CLIENTSPEC ||
        p4FStat.status == FStat.STATUS_UNKNOWN) {
      return;
    }
    // already being added or edited or something
    if (p4FStat.local == FStat.LOCAL_ADDING ||
        p4FStat.local == FStat.LOCAL_BRANCHING ||
        p4FStat.local == FStat.LOCAL_CHECKED_OUT ||
        p4FStat.local == FStat.LOCAL_INTEGRATING ||
        p4FStat.local == FStat.LOCAL_MOVE_ADDING) {
      return;
    }

    long changeListNumber = getPerforceChangeList(project, p4File);
    PerforceRunner.getInstance(project).add(p4File, changeListNumber);
    p4File.clearCache();
    VcsUtil.markFileAsDirty(project, myPath);
  }

  @Override
  public Change getChange(final Project project) {
    return new Change(null, CurrentContentRevision.create(getFilePath()));
  }
}
