package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.ChangeListSynchronizer;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JobsWorker {
  private final Project myProject;
  private final ChangeListSynchronizer mySynchronizer;
  private final PerforceRunner myRunner;

  Project getProject() {
    return myProject;
  }

  public JobsWorker(final Project project) {
    myProject = project;

    mySynchronizer = ChangeListSynchronizer.getInstance(myProject);
    myRunner = PerforceRunner.getInstance(myProject);
  }

  public PerforceJobSpecification getSpec(final P4Connection connection) throws VcsException {
    final List<String> lines = myRunner.getJobSpecification(connection);
    final SpecificationParser parser = new SpecificationParser(lines);
    return parser.parse();
  }

  public List<PerforceJob> getJobs(final PerforceJobSpecification specification, final JobsSearchSpecificator specificator,
                                   final P4Connection connection, final ConnectionKey key) throws VcsException {
    final List<String> lines = myRunner.getJobs(connection, specificator);
    final JobsOutputParser parser = new JobsOutputParser(specification, lines, connection, key);
    return parser.parse();
  }

  public long getNativeListNumber(final LocalChangeList list, final ConnectionKey key) {
    return mySynchronizer.getListNumber(key, list);
  }

  @NotNull
  public List<Pair<String, String>> loadJob(final PerforceJob job) throws VcsException {
    final List<String> lines = myRunner.getJobDetails(job);
    final JobDetailsParser parser = new JobDetailsParser(lines);
    return parser.parse();
  }

  public List<String> getJobNames(LocalChangeList list, P4Connection connection, ConnectionKey key) throws VcsException {
    final long num = getNativeListNumber(list, key);
    final List<String> lines = myRunner.getJobsForChange(connection, num);
    final FixesOutputParser parser = new FixesOutputParser(lines);
    return parser.parseJobNames();
  }

  public List<String> getFreeFields(final PerforceJobSpecification spec) {
    final List<String> result = new ArrayList<String>();
    final Collection<PerforceJobField> fields = spec.getFields();
    for (PerforceJobField field : fields) {
      if (! StandardJobFields.isStandardField(field)) {
        result.add(field.getName());
      }
    }
    return result;
  }

  public void addJob(final PerforceJob job, LocalChangeList list) throws VcsException {
    myRunner.addJobForList(job.getConnection(), getNativeListNumber(list, job.getConnectionKey()), job.getValueForStandardField(StandardJobFields.name.getFixedCode()).getValue());
  }

  public void removeJob(final PerforceJob job, LocalChangeList list) throws VcsException {
    myRunner.removeJobFromList(job.getConnection(), getNativeListNumber(list, job.getConnectionKey()), job.getValueForStandardField(StandardJobFields.name.getFixedCode()).getValue());
  }

  public List<PerforceJob> getJobsForList(final PerforceJobSpecification specification, LocalChangeList list, P4Connection connection,
                                          ConnectionKey key) throws VcsException {
    final List<String> jobNames = getJobNames(list, connection, key);
    // list should be modifiable
    if (jobNames.isEmpty()) return new ArrayList<PerforceJob>();
    return getJobs(specification, new ByNamesConstraint(jobNames), connection, key);
  }
}
