package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;

public class ErrorReporter {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.jobs.ErrorReporter");

  private final String myOperation;

  public ErrorReporter(String operation) {
    myOperation = operation;
  }

  public void report(final Project project, final VcsException e) {
    LOG.info(e);
    final Application application = ApplicationManager.getApplication();
    final String message = "Error during " + myOperation + ": " + e.getMessage();
    if (application.isDispatchThread()) {
      Messages.showErrorDialog(project, message, "Perforce Jobs Error");
    } else {
      application.invokeLater(new Runnable() {
        public void run() {
          Messages.showErrorDialog(project, message, "Perforce Jobs Error");
        }
      });
    }
  }
}
