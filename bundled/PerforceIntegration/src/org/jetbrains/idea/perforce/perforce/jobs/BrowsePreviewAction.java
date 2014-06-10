package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class BrowsePreviewAction extends AnAction {
  private static Icon ourIcon;
  private final Project myProject;
  private final String myJobView;
  private final PerforceJobSpecification mySpecification;
  private final P4Connection myConnection;
  private final ConnectionKey myKey;

  public BrowsePreviewAction(final String jobView, final PerforceJobSpecification specification, final P4Connection connection,
                             final ConnectionKey key, final Project project) {
    myJobView = jobView;
    mySpecification = specification;
    myConnection = connection;
    myKey = key;
    myProject = project;
  }

  private Icon lazyIcon() {
    if (ourIcon == null) {
      ourIcon = IconLoader.getIcon("/actions/showChangesOnly.png");
    }
    return ourIcon;
  }

  @Override
  public void update(final AnActionEvent e) {
    final Presentation presentation = e.getPresentation();
    presentation.setText("Job View");
    presentation.setDescription("Job View");
    presentation.setIcon(lazyIcon());
  }

  @Override
  public void actionPerformed(final AnActionEvent e) {
    final List<PerforceJob> perforceJobs = new ArrayList<PerforceJob>();
    final JobViewSearchSpecificator searchSpecificator = new JobViewSearchSpecificator(myJobView, null);

    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        try {
          final JobsWorker jw = new JobsWorker(myProject);
          perforceJobs.addAll(jw.getJobs(mySpecification, searchSpecificator, myConnection, myKey));
        }
        catch (VcsException e1) {
          Messages.showErrorDialog(myProject, e1.getMessage(), "Error when searching for jobs");
        }
      }
    }, PerforceBundle.message("perforce.jobs.searching.by.jobview.progress.text"), false, myProject);

    if ((perforceJobs == null) || (perforceJobs.isEmpty())) {
      Messages.showInfoMessage(myProject, "No jobs found using Job View.", "No jobs found.");
      return;
    }

    final BrowsePreviewDialog dialog = new BrowsePreviewDialog(myProject, myJobView, perforceJobs, searchSpecificator.getMaxCount());
    dialog.show();
    //dialog.getSelectedJob();
  }
}
