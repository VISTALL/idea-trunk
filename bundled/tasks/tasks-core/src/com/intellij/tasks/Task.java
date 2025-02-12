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

package com.intellij.tasks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Date;

/**
 * @author Dmitry Avdeev
 */
public abstract class Task {

  /**
   * Task identifier, e.g. IDEA-00001
   * @return unique id
   */
  @NotNull
  public abstract String getId();

  /**
   * Short task description.
   * @return description
   */
  @NotNull
  public abstract String getSummary();

  @Nullable
  public abstract String getDescription();

  @NotNull
  public abstract Comment[] getComments();

  @Nullable
  public abstract Icon getIcon();

  @NotNull
  public abstract TaskType getType();

  @Nullable
  public abstract Date getUpdated();

  @Nullable
  public abstract Date getCreated();

  public abstract boolean isClosed();

  /**
   * @return true if bugtracking issue is associated
   */
  public abstract boolean isIssue();

  @Nullable
  public abstract String getIssueUrl();

  @Override
  public final String toString() {
    return isIssue() ? getId() : getSummary();
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof Task && ((Task)obj).getId().equals(getId());
  }

  @Override
  public final int hashCode() {
    return getId().hashCode();
  }
}
