package com.intellij.tasks;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;

import javax.swing.*;

/**
 * @author Dmitry Avdeev
 */
public interface TaskRepositoryType {

  ExtensionPointName<TaskRepositoryType> EP_NAME = new ExtensionPointName<TaskRepositoryType>("com.intellij.tasks.repositoryType");

  String getName();

  Icon getIcon();
  
  UnnamedConfigurable createEditor(TaskRepository repository, Project project, Consumer<TaskRepository> changeListener);

  TaskRepository[] getRepositories();

  TaskRepository createRepository();

  void configureRepositories(TaskRepository[] newRepositories);

}
