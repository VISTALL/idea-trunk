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

package com.intellij.tasks.jira;

import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAComment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskType;
import com.intellij.tasks.Comment;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

/**
 * @author Dmitry Avdeev
*/
class JiraTask extends Task {
  
  private final JIRAIssue myJiraIssue;

  public JiraTask(JIRAIssue jiraIssue) {
    myJiraIssue = jiraIssue;
  }

  @NotNull
  public String getId() {
    return myJiraIssue.getKey();
  }

  @NotNull
  public String getSummary() {
    return myJiraIssue.getSummary();
  }

  public String getDescription() {
    return myJiraIssue.getDescription();
  }

  @NotNull
  public Comment[] getComments() {
    List<JIRAComment> jiraComments = myJiraIssue.getComments();
    if (jiraComments == null) return new Comment[0];
    return ContainerUtil.map2Array(jiraComments, Comment.class, new Function<JIRAComment, Comment>() {
      public Comment fun(JIRAComment jiraComment) {
        return new JiraComment(jiraComment);
      }
    });
  }

  public Icon getIcon() {
    String iconUrl = myJiraIssue.getTypeIconUrl();
    return iconUrl == null ? null : isClosed() ? CachedIconLoader.getDisabledIcon(iconUrl) : CachedIconLoader.getIcon(iconUrl);
  }

  @NotNull
  @Override
  public TaskType getType() {
    String type = myJiraIssue.getType();
    if (type == null) {
      return TaskType.OTHER;
    } else if (type.equals("Bug")) {
      return TaskType.BUG;
    } else if (type.equals("Exception")) {
      return TaskType.EXCEPTION;
    } else if (type.equals("New Feature")) {
      return TaskType.FEATURE_REQUEST;
    } else {
      return TaskType.OTHER;
    }
  }

  @Nullable
  @Override
  public Date getUpdated() {
    return getDate(myJiraIssue.getUpdated());
  }

  @Nullable
  private static Date getDate(String date) {
    try {
      return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(date);
    }
    catch (ParseException e) {
      return null;
    }
  }

  @Override
  public Date getCreated() {
    return getDate(myJiraIssue.getCreated());
  }

  @Override
  public boolean isClosed() {
    return !JiraConstants.UNRESOLVED.equals(myJiraIssue.getResolution());
  }

  public boolean isIssue() {
    return true;
  }

  @Override
  public String getIssueUrl() {
    return myJiraIssue.getIssueUrl();
  }
}
