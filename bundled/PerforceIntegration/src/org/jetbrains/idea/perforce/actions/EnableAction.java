/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

public class EnableAction extends ToggleAction {
  public boolean isSelected(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    return project != null && !PerforceSettings.getSettings(project).ENABLED;
  }

  public void setSelected(AnActionEvent e, boolean state) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project != null) {
      if (!state) {
        PerforceSettings.getSettings(project).enable();
      }
      else {
        PerforceSettings.getSettings(project).disable();
      }
    }
  }
}
