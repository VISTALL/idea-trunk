package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

class MemoryAdderRemover implements AdderRemover {
  private final JobsTablePresentation myPresentation;

  MemoryAdderRemover(JobsTablePresentation presentation) {
    myPresentation = presentation;
  }

  public VcsException add(@NotNull final PerforceJob job, LocalChangeList list, Project project) {
    myPresentation.addJob(job);
    return null;
  }

  public VcsException remove(@NotNull final PerforceJob job, LocalChangeList list, Project project) {
    myPresentation.removeSelectedJob();
    return null;
  }
}
