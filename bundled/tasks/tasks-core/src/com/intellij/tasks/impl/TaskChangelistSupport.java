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

package com.intellij.tasks.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.ui.EditChangelistSupport;
import com.intellij.tasks.Task;
import com.intellij.tasks.actions.TaskSearchSupport;
import com.intellij.util.Consumer;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * @author Dmitry Avdeev
 */
public class TaskChangelistSupport implements EditChangelistSupport {

  private Project myProject;
  private final TaskManagerImpl myTaskManager;

  public TaskChangelistSupport(Project project, TaskManagerImpl taskManager) {
    this.myProject = project;
    myTaskManager = taskManager;
  }

  public void installSearch(JTextComponent name, final JTextComponent comment) {
    new TaskSearchSupport(name, myProject) {
      @Override
      protected void onItemChosen(Task result) {
        super.onItemChosen(result);
        String name = TaskUtil.getChangeListName(result);
        comment.setText(name);
      }
    }.setAutoPopup(false);
  }

  public Consumer<LocalChangeList> addControls(JPanel bottomPanel, LocalChangeList initial) {
    final JCheckBox checkBox = new JCheckBox("Track context");
    checkBox.setMnemonic('t');
    checkBox.setToolTipText("Reload context (e.g. open editors) when changelist is set active");
    checkBox.setSelected(initial == null ?
                           myTaskManager.getState().trackContextForNewChangelist :
                           myTaskManager.getAssociatedTask(initial) != null);
    bottomPanel.add(checkBox);
    return new Consumer<LocalChangeList>() {
      public void consume(LocalChangeList changeList) {
        myTaskManager.getState().trackContextForNewChangelist = checkBox.isSelected();
        if (checkBox.isSelected()) {
          myTaskManager.associateWithTask(changeList);
        }
      }
    };
  }

  public void changelistCreated(LocalChangeList changeList) {
  }
}
