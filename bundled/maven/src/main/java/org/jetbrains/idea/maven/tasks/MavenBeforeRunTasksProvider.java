package org.jetbrains.idea.maven.tasks;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.ide.DataAccessors;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.navigator.SelectMavenGoalDialog;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.utils.MavenLog;

import java.util.Collections;

public class MavenBeforeRunTasksProvider implements BeforeRunTaskProvider<MavenBeforeRunTask> {
  public static final Key<MavenBeforeRunTask> TASK_ID = Key.create("Maven.BeforeRunTask");
  private final Project myProject;

  public MavenBeforeRunTasksProvider(Project project) {
    myProject = project;
  }

  public Key getId() {
    return TASK_ID;
  }

  public String getDescription(RunConfiguration runConfiguration, MavenBeforeRunTask task) {
    String desc = null;
    if (task.isEnabled()) {
      Pair<MavenProject, String> projectAndGoal = getProjectAndGoalChecked(task);
      if (projectAndGoal != null) desc = projectAndGoal.first.getDisplayName() + ":" + projectAndGoal.second;
    }
    return desc == null
           ? TasksBundle.message("maven.tasks.before.run.empty")
           : TasksBundle.message("maven.tasks.before.run", desc);
  }

  private Pair<MavenProject, String> getProjectAndGoalChecked(MavenBeforeRunTask task) {
    String path = task.getProjectPath();
    String goal = task.getGoal();
    if (path == null || goal == null) return null;

    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
    if (file == null) return null;

    MavenProject project = MavenProjectsManager.getInstance(myProject).findProject(file);
    if (project == null) return null;

    return Pair.create(project, goal);
  }

  public boolean hasConfigurationButton() {
    return true;
  }

  public MavenBeforeRunTask createTask(RunConfiguration runConfiguration) {
    return new MavenBeforeRunTask();
  }

  public void configureTask(RunConfiguration runConfiguration, MavenBeforeRunTask task) {
    SelectMavenGoalDialog dialog = new SelectMavenGoalDialog(myProject,
                                                             task.getProjectPath(),
                                                             task.getGoal(),
                                                             TasksBundle.message("maven.tasks.select.goal.title"));
    dialog.show();
    if (!dialog.isOK()) return;

    task.setProjectPath(dialog.getSelectedProjectPath());
    task.setGoal(dialog.getSelectedGoal());

    MavenTasksManager.getInstance(myProject).fireTasksChanged();
  }

  public boolean executeTask(final DataContext context, RunConfiguration configuration, final MavenBeforeRunTask task) {
    final Semaphore targetDone = new Semaphore();
    final boolean[] result = new boolean[1];
    try {
      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        public void run() {
          final Project project = DataAccessors.PROJECT.from(context);
          final Pair<MavenProject, String> projectAndGoal = getProjectAndGoalChecked(task);

          if (project == null || project.isDisposed() || projectAndGoal == null) return;

          targetDone.down();
          new Task.Backgroundable(project, TasksBundle.message("maven.tasks.executing"), true) {
            public void run(@NotNull ProgressIndicator indicator) {
              try {
                MavenRunnerParameters params = new MavenRunnerParameters(
                  true,
                  projectAndGoal.first.getDirectory(),
                  Collections.singletonList(projectAndGoal.second),
                  MavenProjectsManager.getInstance(project).getActiveProfiles());

                result[0] = MavenRunner.getInstance(project).runBatch(Collections.singletonList(params),
                                                                      null,
                                                                      null,
                                                                      TasksBundle.message("maven.tasks.executing"),
                                                                      indicator);
              }
              finally {
                targetDone.up();
              }
            }

            @Override
            public boolean shouldStartInBackground() {
              return MavenRunner.getInstance(project).getSettings().isRunMavenInBackground();
            }

            @Override
            public void processSentToBackground() {
              MavenRunner.getInstance(project).getSettings().setRunMavenInBackground(true);
            }
          }.queue();
        }
      }, ModalityState.NON_MODAL);
    }
    catch (Exception e) {
      MavenLog.LOG.error(e);
      return false;
    }
    targetDone.waitFor();
    return result[0];
  }
}
