package com.intellij.tasks;

import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public interface TaskRepository {

  boolean isConfigured();

  boolean isShared();

  Task[] getMyIssues(int count);

  String getPresentableName();

  TaskRepositoryType getType();

  void testConnection() throws Exception;

  TaskRepository clone();

  @Nullable
  Task findTask(String id);

  void closeTask(Task task);

  @Nullable
  String extractId(String taskName);

  @Nullable
  String getTaskUrl(Task task);
}
