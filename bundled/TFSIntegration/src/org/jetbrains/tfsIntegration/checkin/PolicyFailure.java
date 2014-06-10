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

package org.jetbrains.tfsIntegration.checkin;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Policy evaluation error
 */
public class PolicyFailure {
  private @NotNull final PolicyBase myPolicy;
  private @NotNull final String myMessage;
  private @Nullable final String myTooltipText;

  /**
   * @param policy  policy instance that produced the error
   * @param message displayable message
   */
  public PolicyFailure(@NotNull PolicyBase policy, @NotNull String message) {
    this(policy, message, null);
  }

  /**
   * @param policy      policy instance that produced the error
   * @param message     displayable message
   * @param tooltipText displayable text describing error details
   */

  public PolicyFailure(@NotNull PolicyBase policy, @NotNull String message, @Nullable String tooltipText) {
    myPolicy = policy;
    myMessage = message;
    myTooltipText = tooltipText;
  }

  @NotNull
  public String getMessage() {
    return myMessage;
  }

  @Nullable
  public String getTooltipText() {
    return myTooltipText;
  }

  public void activate(@NotNull Project project) {
    myPolicy.activate(project, this);
  }

  public String getPolicyName() {
    return myPolicy.getPolicyType().getName();
  }
}
