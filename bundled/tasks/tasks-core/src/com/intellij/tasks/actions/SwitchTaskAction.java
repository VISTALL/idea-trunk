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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Ref;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.tasks.impl.TaskUtil;
import com.intellij.ui.popup.list.ListPopupImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Avdeev
 */
public class SwitchTaskAction extends BaseTaskAction {

  private static final int MAX_ROW_COUNT = 20;

  @Override
  public void actionPerformed(AnActionEvent e) {
    DataContext dataContext = e.getDataContext();
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    assert project != null;
    final ListPopupImpl popup = createPopup(dataContext, null);
    popup.showCenteredInCurrentWindow(project);
  }

  public static ListPopupImpl createPopup(DataContext dataContext, Runnable onDispose) {
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    assert project != null;
    final Ref<Boolean> shiftPressed = Ref.create(false);
    DefaultActionGroup group = createPopupActionGroup(project, shiftPressed);
    final ListPopupImpl popup = (ListPopupImpl)JBPopupFactory.getInstance()
      .createActionGroupPopup("Switch to Task", group, dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false, onDispose,
                              MAX_ROW_COUNT);
    popup.setAdText("Press SHIFT to merge with current context");

    popup.registerAction("shiftPressed", KeyStroke.getKeyStroke("shift pressed SHIFT"), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        shiftPressed.set(true);
        popup.setCaption("Merge with Current Context");
      }
    });
    popup.registerAction("shiftReleased", KeyStroke.getKeyStroke("released SHIFT"), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        shiftPressed.set(false);
        popup.setCaption("Switch to Task");
      }
    });
    popup.registerAction("invoke", KeyStroke.getKeyStroke("shift ENTER"), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        popup.handleSelect(true);
      }
    });
    return popup;
  }

  @NotNull
  private static DefaultActionGroup createPopupActionGroup(Project project, final Ref<Boolean> shiftPressed) {
    DefaultActionGroup group = new DefaultActionGroup();
    final TaskManager manager = TaskManager.getManager(project);

    group.add(new OpenTaskAction());
    
    group.addSeparator();

    LocalTask activeTask = manager.getActiveTask();
    LocalTask[] localTasks = manager.getLocalTasks();
    Arrays.sort(localTasks, TaskManagerImpl.TASK_UPDATE_COMPARATOR);
    ArrayList<LocalTask> temp = new ArrayList<LocalTask>();
    boolean vcsEnabled = manager.isVcsEnabled();
    for (final LocalTask task : localTasks) {
      if (task == activeTask) {
        continue;
      }
      if (vcsEnabled && manager.getOpenChangelists(task).isEmpty()) {
        temp.add(task);
        continue;
      }
      group.add(createActivateTaskAction(manager, task, shiftPressed, false));
    }
    if (vcsEnabled && !temp.isEmpty()) {
      group.addSeparator();
      for (int i = 0, tempSize = temp.size(); i < Math.min(tempSize, 5); i++) {
        LocalTask task = temp.get(i);
        group.add(createActivateTaskAction(manager, task, shiftPressed, true));
      }
    }
    return group;
  }

  private static AnAction createActivateTaskAction(final TaskManager manager, final LocalTask task, final Ref<Boolean> shiftPressed,
                                                   final boolean temp) {
    String trimmedSummary = TaskUtil.getTrimmedSummary(task);

    Icon icon = temp ? IconLoader.getTransparentIcon(task.getIcon(), 0.5f) : task.getIcon();
    final AnAction action = new AnAction("&Switch to") {
      public void actionPerformed(AnActionEvent e) {
        manager.activateTask(task, !shiftPressed.get(), !temp);
      }
    };
    ActionGroup group = new ActionGroup(trimmedSummary, task.getSummary(), icon) {

      @NotNull
      @Override
      public AnAction[] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{action, new AnAction("&Remove") {
          @Override
          public void actionPerformed(AnActionEvent e) {
            manager.removeTask(task);            
          }
        }};
      }

      @Override
      public void actionPerformed(AnActionEvent e) {
        action.actionPerformed(e);
      }

      @Override
      public boolean canBePerformed() {
        return true;
      }
    };
    group.setPopup(true);
    return group;
  }

}
