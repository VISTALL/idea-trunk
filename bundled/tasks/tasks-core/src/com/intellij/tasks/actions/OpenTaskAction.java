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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskType;

/**
 * @author Dmitry Avdeev
 */
public class OpenTaskAction extends BaseTaskAction {

  public OpenTaskAction() {
    super("Open _New Task...");
  }

  public void actionPerformed(AnActionEvent e) {

    Project project = getProject(e);
    ActivateTaskDialog dialog = new ActivateTaskDialog(project);
    dialog.show();
    if (dialog.isOK()) {
      Task task = dialog.getSelectedTask();
      if (task != null) {
        TaskManager.getManager(project).activateTask(task, dialog.isClearContext(), dialog.isCreateChangelist());
        if (task.getType() == TaskType.EXCEPTION) {
          AnalyzeTaskStacktraceAction.analyzeStacktrace(task, project);
        }
      }
    }
  }

}
