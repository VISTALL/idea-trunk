/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.tfsIntegration.ui.servertree;

import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class ServerBrowserAction extends AnAction {
  private @NotNull ServerTree myServerTree;

  public ServerBrowserAction(String text, Icon icon) {
    super(text, "", icon);
  }

  void setServerTree(@NotNull ServerTree serverTree) {
    myServerTree = serverTree;
  }

  @NotNull
  public ServerTree getServerTree() {
    return myServerTree;
  }

}
