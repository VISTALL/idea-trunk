package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class WritingAdderRemover implements AdderRemover {
  private final JobsTablePresentation myPresentation;
  private final JobsWorker myWorker;

  WritingAdderRemover(final JobsWorker worker, JobsTablePresentation presentation) {
    myWorker = worker;
    myPresentation = presentation;
  }

  @Nullable
  public VcsException add(@NotNull final PerforceJob job, final LocalChangeList list, final Project project) {
    final Ref<VcsException> exceptionRef = new Ref<VcsException>();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        try {
          myWorker.addJob(job, list);
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              try {
                myPresentation.refreshJobs(job);
              }
              catch (VcsException e) {
                new ErrorReporter("linking job to a list").report(project, e);
              }
            }
          });
        }
        catch (VcsException e) {
          exceptionRef.set(e);
        }
      }
    }, "Adding job to changelist", false, myWorker.getProject());
    return exceptionRef.get();
  }

  @Nullable
  public VcsException remove(@NotNull final PerforceJob job, final LocalChangeList list, final Project project) {
    final Ref<VcsException> exceptionRef = new Ref<VcsException>();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        try {
          myWorker.removeJob(job, list);
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              try {
                myPresentation.refreshJobs(null);
                myPresentation.selectDefault();
              }
              catch (VcsException e) {
                new ErrorReporter("removing job from changelist").report(project, e);
              }
            }
          });
        }
        catch (VcsException e) {
          exceptionRef.set(e);
        }
      }
    }, "Removing job from changelist", false, myWorker.getProject());
    return exceptionRef.get();
  }
}
