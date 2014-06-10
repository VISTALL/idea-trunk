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

import com.intellij.openapi.options.binding.BindControl;
import com.intellij.openapi.options.binding.ControlBinder;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.tasks.ChangeListInfo;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.TaskManagerImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Iterator;

/**
 * @author Dmitry Avdeev
 */
public class ActivateTaskDialog extends DialogWrapper {

  private JPanel myPanel;

  @BindControl(value = "clearContext", instant = true)
  private JCheckBox myClearContext;
  @BindControl(value = "createChangelist", instant = true)
  private JCheckBox myCreateChangelist;
  private JTextField myTaskName;

  private final Project myProject;
  private Task mySelectedTask;
  private boolean myVcsEnabled;

  protected ActivateTaskDialog(Project project) {

    super(project, true);
    myProject = project;
    setTitle("Open Task");

    new TaskSearchSupport(myTaskName, project) {
      @Override
      protected void onTextChanged() {
        super.onTextChanged();
        mySelectedTask = getResult();
        taskChanged();
      }
    }.setAutoPopup(true);

    TaskManagerImpl manager = (TaskManagerImpl)TaskManager.getManager(project);
    ControlBinder binder = new ControlBinder(manager.getState());
    binder.bindAnnotations(this);
    binder.reset();

    myVcsEnabled = manager.isVcsEnabled();
    taskChanged();

    init();
  }

  private void taskChanged() {
    TaskManagerImpl taskManager = (TaskManagerImpl)TaskManager.getManager(myProject);
    Task task = getSelectedTask();

    if (task != null) {
      // refresh change lists
      ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
      for (Iterator<ChangeListInfo> it = taskManager.getOpenChangelists(task).iterator(); it.hasNext();) {
        ChangeListInfo changeListInfo = it.next();
        if (changeListManager.getChangeList(changeListInfo.id) == null) {
          it.remove();
        }
      }
    }

    if (!myVcsEnabled) {
      myCreateChangelist.setEnabled(false);
      myCreateChangelist.setSelected(false);
    } else if (task != null && !taskManager.getOpenChangelists(task).isEmpty()) {
      myCreateChangelist.setEnabled(false);
      myCreateChangelist.setSelected(true);
    } else {
      myCreateChangelist.setSelected(taskManager.getState().createChangelist);
      myCreateChangelist.setEnabled(true);
    }

    setOKActionEnabled(isOKActionEnabled());
  }

  @Override
  protected void doOKAction() {
    if (mySelectedTask == null) {
      String taskName = getTaskName();
      TaskManager manager = TaskManager.getManager(myProject);

      String lastId = null;
      for (final TaskRepository repository : manager.getAllRepositories()) {
        final String id = repository.extractId(taskName);
        if (id != null) {
          lastId = id;
          ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            public void run() {
              mySelectedTask = repository.findTask(id);
            }
          }, "Getting " + id + " from " + repository.getPresentableName() + "...", true, myProject);
        }
      }
      if (lastId == null) {
        mySelectedTask = manager.createLocalTask(taskName);
      } else if (mySelectedTask == null) {
        Messages.showErrorDialog(myProject, "Issue " + lastId + " not found", "Issue Not Found");
        return;
      }
    }
    super.doOKAction();
  }

  @Nullable
  public Task getSelectedTask() {
    return mySelectedTask;
  }

  public boolean isClearContext() {
    return myClearContext.isSelected();
  }

  public boolean isCreateChangelist() {
    return myCreateChangelist.isSelected();
  }

  @NonNls
  protected String getDimensionServiceKey() {
    return "ActivateTaskDialog";
  }

  @Override
  public boolean isOKActionEnabled() {
    return !StringUtil.isEmptyOrSpaces(getTaskName());
  }

  private String getTaskName() {
    return myTaskName.getText();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTaskName;
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

}
