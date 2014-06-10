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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.util.Consumer;
import com.intellij.util.IJSwingUtilities;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Avdeev
 */
public class SwitchTaskCombo extends ComboBoxAction implements DumbAware {

  private static final Key<ComboBoxButton> BUTTON_KEY = Key.create("SWITCH_TASK_BUTTON");

  @Override
  public void actionPerformed(AnActionEvent e) {
    final IdeFrameImpl ideFrame = findFrame(e);
    final ComboBoxButton button = (ComboBoxButton)ideFrame.getRootPane().getClientProperty(BUTTON_KEY);
    if (button == null || !button.isShowing()) return;
    button.showPopup();
  }

  private static IdeFrameImpl findFrame(AnActionEvent e) {
    return IJSwingUtilities.findParentOfType(e.getData(PlatformDataKeys.CONTEXT_COMPONENT), IdeFrameImpl.class);
  }

  public JComponent createCustomComponent(final Presentation presentation) {
    return new ComboBoxButton(presentation) {
      protected void updateButtonSize() {
        super.updateButtonSize();
        final Dimension preferredSize = getPreferredSize();
        final int width = preferredSize.width;
        final int height = preferredSize.height;
        if (width > height * 15) {
          setPreferredSize(new Dimension(height * 15, height));
        }
      }

      public void addNotify() {
        super.addNotify();
        final IdeFrameImpl frame = IJSwingUtilities.findParentOfType(this, IdeFrameImpl.class);
        frame.getRootPane().putClientProperty(BUTTON_KEY, this);
      }

      @Override
      protected ListPopup createPopup(Runnable onDispose) {
        return SwitchTaskAction.createPopup(DataManager.getInstance().getDataContext(), onDispose);
      }
    };
  }


  @NotNull
  protected DefaultActionGroup createPopupActionGroup(JComponent button) {
    DefaultActionGroup group = new DefaultActionGroup();
    final Project project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(button));
    if (project == null) {
      return group;
    }
    final TaskManager manager = TaskManager.getManager(project);

    group.add(new OpenTaskAction());

    group.addSeparator();
    final boolean vcsEnabled = manager.isVcsEnabled();

    return fillActionsGroup(group, manager, new Consumer<LocalTask>() {

      public void consume(LocalTask localTask) {
        manager.activateTask(localTask, true, vcsEnabled && manager.getOpenChangelists(localTask).isEmpty());
      }
    }, true);
  }

  public static DefaultActionGroup fillActionsGroup(DefaultActionGroup group, TaskManager manager, Consumer<LocalTask> action, boolean acceptDefault) {
    LocalTask activeTask = manager.getActiveTask();
    LocalTask[] localTasks = manager.getLocalTasks();
    Arrays.sort(localTasks, TaskManagerImpl.TASK_UPDATE_COMPARATOR);
    ArrayList<LocalTask> temp = new ArrayList<LocalTask>();
    boolean vcsEnabled = manager.isVcsEnabled();
    for (final LocalTask task : localTasks) {
      if ((task == activeTask) || (!acceptDefault && task.isDefault())) {
        continue;
      }
      if (vcsEnabled && manager.getOpenChangelists(task).isEmpty()) {
        temp.add(task);
        continue;
      }
      group.add(createActivateTaskAction(manager, task, action));
    }
    if (vcsEnabled && !temp.isEmpty()) {
      group.addSeparator();
      for (int i = 0, tempSize = temp.size(); i < Math.min(tempSize, 5); i++) {
        LocalTask task = temp.get(i);
        group.add(createActivateTaskAction(manager, task, action));
      }
    }
    return group;
  }

  private static AnAction createActivateTaskAction(final TaskManager manager, final LocalTask task, final Consumer<LocalTask> action) {
    String trimmedSummary = getTrimmedSummary(task);

    boolean temp = manager.isVcsEnabled() && manager.getOpenChangelists(task).isEmpty();
    Icon icon = temp ? IconLoader.getTransparentIcon(task.getIcon(), 0.5f) : task.getIcon();
    return new AnAction(trimmedSummary, task.getSummary(), icon) {
      public void actionPerformed(AnActionEvent e) {
        action.consume(task);
      }
    };
  }

  public static String getTrimmedSummary(LocalTask task) {
    String text;
    if (task.isIssue()) {
      text = task.getId() + ": " + task.getSummary();
    } else {
      text = task.getSummary();
    }
    return StringUtil.first(text, 60, true);
  }

  @Override
  public void update(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    Presentation presentation = e.getPresentation();
    if (project == null || project.isDisposed() || (ActionPlaces.MAIN_MENU.equals(e.getPlace()) && findFrame(e) == null)) {
      presentation.setEnabled(false);
      presentation.setText("");
    }
    else {
      LocalTask activeTask = TaskManager.getManager(project).getActiveTask();
      presentation.setVisible(true);
      presentation.setEnabled(true);
      String s = getText(activeTask);
      presentation.setText(s);
      presentation.setIcon(activeTask.getIcon());
      presentation.setDescription(activeTask.getSummary());
    }
  }

  private static String getText(LocalTask activeTask) {
    String text = activeTask.toString();
    return StringUtil.first(text, 80, true);
  }

}
