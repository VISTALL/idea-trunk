package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;

/**
 * @author yole
 */
public class P4DeleteOperation extends VcsOperationOnPath {
  public P4DeleteOperation() {
  }

  public P4DeleteOperation(String changeList, final FilePath item) {
    super(changeList, FileUtil.toSystemIndependentName(item.getPath()));
  }

  public void execute(final Project project) throws VcsException {
    final P4File p4File = P4File.createInefficientFromLocalPath(myPath);
    final long list = getPerforceChangeList(project, p4File);
    PerforceRunner.getInstance(project).assureDel(p4File, list);
    VcsUtil.markFileAsDirty(project, myPath);
  }

  @Override
  public Change getChange(final Project project) {
    FilePath path = VcsContextFactory.SERVICE.getInstance().createFilePathOn(new File(myPath));
    ContentRevision beforeRevision = PerforceCachingContentRevision.create(project, path, -1);
    return new Change(beforeRevision, null);
  }

  @Override
  @Nullable
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    if (oldOp instanceof P4AddOperation && FileUtil.pathsEqual(((P4AddOperation) oldOp).getPath(), myPath)) {
      return null;
    }
    return super.checkMerge(oldOp);
  }
}