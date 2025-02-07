/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.tasks.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskManager;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public abstract class BaseTaskAction extends AnAction implements DumbAware {

  protected BaseTaskAction() {
  }

  protected BaseTaskAction(String text) {
    super(text);
  }

  public void update(AnActionEvent event){
    event.getPresentation().setEnabled(getProject(event) != null);
  }

  @Nullable
  public static Project getProject(@Nullable AnActionEvent event) {
      return event == null ? null : PlatformDataKeys.PROJECT.getData(event.getDataContext());
  }

  @Nullable
  public static TaskManager getTaskManager(AnActionEvent event) {
    Project project = getProject(event);
    if (project == null) {
      return null;
    }
    return TaskManager.getManager(project);
  }
  
  @Nullable
  public static LocalTask getActiveTask(AnActionEvent event) {
    TaskManager manager = getTaskManager(event);
    return manager == null ? null : manager.getActiveTask();
  }
}
