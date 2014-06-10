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

import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.NullableFunction;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.*;

/**
* @author Dmitry Avdeev
*/
public class TaskSearchSupport extends SearchSupport<Task> {
  private final Task[] myTasks;
  protected NameUtil.Matcher myMatcher;

  public TaskSearchSupport(JTextComponent field, final Project project) {
    super(field);

    TaskManagerImpl manager = (TaskManagerImpl)TaskManager.getManager(project);

    Collection<Task> issues = manager.getCachedIssues().values();
    Set<Task> taskSet = new HashSet<Task>(issues);
    taskSet.removeAll(Arrays.asList(manager.getLocalTasks()));

    myTasks = taskSet.toArray(new Task[taskSet.size()]);
    Arrays.sort(myTasks, new Comparator<Task>() {
      public int compare(Task o1, Task o2) {
        int i = Comparing.compare(isOpen(o2, project), isOpen(o1, project));
        if (i != 0) {
          return i;
        }
        i = Comparing.compare(o1.isClosed(), o2.isClosed());
        if (i != 0) {
          return i;
        }
        i = Comparing.compare(o2.getUpdated(), o1.getUpdated());
        return i == 0 ? Comparing.compare(o2.getCreated(), o1.getCreated()) : i;
      }
    });


    setListRenderer(new ColoredListCellRenderer() {
      @Override
      protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {

        Task task = (Task)value;

        String s = task.toString();
        SimpleTextAttributes attributes =
          task.isClosed() ? SimpleTextAttributes.GRAYED_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES;
        appendWithSearch(s, attributes);

        if (task.isIssue()) {
          appendWithSearch(" (" + StringUtil.first(task.getSummary(), 80, true) + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
        setIcon(task.getIcon());
//        setToolTipText("<html>" + task.getDescription() + "</html>");
      }

      private void appendWithSearch(String s, SimpleTextAttributes attributes) {

          append(s, attributes);

      }
    });

  }


  private static boolean isOpen(Task task, Project project) {
    return !task.isClosed() && !TaskManager.getManager(project).getOpenChangelists(task).isEmpty();
  }


  protected List<Task> getItems(String pattern) {
    final NameUtil.Matcher matcher = getMatcher();
    return ContainerUtil.mapNotNull(myTasks, new NullableFunction<Task, Task>() {
      public Task fun(Task task) {
        return matcher.matches(task.getId()) || matcher.matches(task.getSummary()) ? task : null;
      }
    });
  }

  @Override
  protected void onTextChanged() {
    myMatcher = null;
    super.onTextChanged();
  }

  private NameUtil.Matcher getMatcher() {
    String pattern = getText();
    if (myMatcher == null) {
      StringTokenizer tokenizer = new StringTokenizer(pattern, " ");
      StringBuilder builder = new StringBuilder();
      while (tokenizer.hasMoreTokens()) {
        String word = tokenizer.nextToken();
        builder.append('*');
        builder.append(word);
        builder.append("* ");
      }
      
      myMatcher = NameUtil.buildMatcher(builder.toString(), 0, true, true, pattern.toLowerCase().equals(pattern));
    }
    return myMatcher;
  }

}
