package com.intellij.tasks;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Avdeev
 */
public abstract class TaskManager {

  public static TaskManager getManager(Project project) {
    return project.getComponent(TaskManager.class);
  }

  public abstract Map<String,Task> getCachedIssues();

  @Nullable
  public abstract Task updateIssue(String id); 

  public abstract LocalTask[] getLocalTasks();

  public abstract LocalTask createLocalTask(String summary);

  public abstract void activateTask(@NotNull Task task, boolean clearContext, boolean createChangelist);

  public abstract List<ChangeListInfo> getOpenChangelists(Task task);

  @NotNull
  public abstract LocalTask getActiveTask();

  public abstract void updateIssues();

  public abstract boolean isVcsEnabled();

  @Nullable
  public abstract LocalTask getAssociatedTask(LocalChangeList list);

  public abstract void associateWithTask(LocalChangeList changeList);

  public abstract void closeTask(LocalTask activeTask);

  public abstract void removeTask(LocalTask task);

  // repositories management
  public abstract TaskRepository[] getAllRepositories();

  public abstract void configureRepositories(TaskRepository[] newRepositories);

  public abstract boolean testConnection(TaskRepository repository);

  public abstract TaskRepositoryType[] getAllRepositoryTypes();
}
