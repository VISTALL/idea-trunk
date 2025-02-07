package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JobDetailsLoader {
  private final Project myProject;
  private final JobsWorker myWorker;
  private PerforceRunner myRunner;

  public JobDetailsLoader(final Project project) {
    myWorker = new JobsWorker(project);
    myProject = project;
    myRunner = PerforceRunner.getInstance(myProject);
  }

  Project getProject() {
    return myProject;
  }

  public List<Pair<String, String>> load(final PerforceJob job) throws VcsException {
    final List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
    final Ref<VcsException> exception = new Ref<VcsException>();
    final Runnable loader = new Runnable() {
      public void run() {
        try {
          result.addAll(myWorker.loadJob(job));
        }
        catch (VcsException e) {
          exception.set(e);
        }
      }
    };
    if (! ApplicationManager.getApplication().isDispatchThread()) {
      loader.run();
    } else {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(loader, "Loading job fields...", true, myProject);
    }
    if (! exception.isNull()) {
      throw exception.get();
    }
    return result;
  }

  public void fillConnections(final LocalChangeList list, final Map<ConnectionKey, P4JobsLogicConn> connMap) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        final ConnectionSelector connectionSelector = new ConnectionSelector(myProject, list);
        final Map<ConnectionKey,P4Connection> map = connectionSelector.getConnections();
        if (map.isEmpty()) return;

        final ErrorReporter reporter = new ErrorReporter("getting related jobs");
        for (Map.Entry<ConnectionKey, P4Connection> entry : map.entrySet()) {
          final ConnectionKey key = entry.getKey();
          final P4Connection connection = entry.getValue();
          try {
            final P4JobsLogicConn p4JobsLogicConn = connMap.get(key);
            final PerforceJobSpecification spec = (p4JobsLogicConn == null) ? myWorker.getSpec(connection) : p4JobsLogicConn.getSpec();
            connMap.put(key, new P4JobsLogicConn(connection, spec, myRunner.getUserJobView(connection, key.user)));
          }
          catch (VcsException e1) {
            reporter.report(myProject, e1);
          }
        }
      }
    }, "Getting jobs specifications", false, myProject);
  }

  public void loadJobsForList(final LocalChangeList list, final Map<ConnectionKey, P4JobsLogicConn> connMap,
                              final Map<ConnectionKey, List<PerforceJob>> perforceJobs) {
    final Runnable loader = new Runnable() {
      public void run() {
        final ConnectionSelector connectionSelector = new ConnectionSelector(myProject, list);
        final Map<ConnectionKey, P4Connection> map = connectionSelector.getConnections();
        if (map.isEmpty()) return;

        final ErrorReporter reporter = new ErrorReporter("getting related jobs");

        for (Map.Entry<ConnectionKey, P4Connection> entry : map.entrySet()) {
          final ConnectionKey key = entry.getKey();
          final P4Connection connection = entry.getValue();
          try {
            final List<String> jobNames = myWorker.getJobNames(list, connection, key);
            final P4JobsLogicConn p4JobsLogicConn = connMap.get(key);
            final PerforceJobSpecification spec = (p4JobsLogicConn == null) ? myWorker.getSpec(connection) : p4JobsLogicConn.getSpec();
            // todo is it needed here?
            // todo is it needed here?
            // todo is it needed here?
            connMap.put(key, new P4JobsLogicConn(connection, spec, myRunner.getUserJobView(connection, key.user)));
            perforceJobs.put(key, jobNames.isEmpty()
                                  ? Collections.<PerforceJob>emptyList()
                                  : myWorker.getJobs(spec, new ByNamesConstraint(jobNames), connection, key));
          }
          catch (VcsException e1) {
            reporter.report(myProject, e1);
          }
        }
      }
    };
    if (ApplicationManager.getApplication().isDispatchThread()) {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(loader, "Loading Perforce jobs for changelist", false, myProject);
    } else {
      loader.run();
    }
  }

  @Nullable
  public PerforceJob searchForJobByPattern(final String text, final PerforceJobSpecification spec, final ConnectionKey key, final P4Connection connection) {
    final List<PerforceJob> perforceJobs;
    try {
      perforceJobs = myWorker
      .getJobs(spec, new ByNamesConstraint(Collections.singletonList(text)), connection, key);
    }
    catch (VcsException e1) {
      new ErrorReporter("searching job to add").report(myProject, e1);
      return null;
    }
    if (perforceJobs.size() > 1) {
      Messages.showMessageDialog(myProject, "There are several jobs matching pattern", "Add Perforce Job", Messages.getInformationIcon());
      return null;
    }
    if (perforceJobs.isEmpty()) {
      Messages.showMessageDialog(myProject, "There are no jobs matching pattern", "Add Perforce Job", Messages.getInformationIcon());
      return null;
    }
    return perforceJobs.get(0);
  }
}
