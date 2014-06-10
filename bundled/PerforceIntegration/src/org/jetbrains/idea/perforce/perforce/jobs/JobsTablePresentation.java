package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.vcs.VcsException;

public interface JobsTablePresentation {
  void refreshJobs(PerforceJob job) throws VcsException;
  void addJob(final PerforceJob job);
  void removeSelectedJob();
  void selectDefault();
}
