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
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MultiLineLabelUI;
import com.intellij.util.ui.OptionsDialog;
import org.jetbrains.idea.perforce.PerforceBundle;

import javax.swing.*;

public class PerforceLoginDialog extends OptionsDialog {

  private JPasswordField myPasswordField;
  private final String myPrompt;
  private final String myDefaultValue;

  public PerforceLoginDialog(Project project, String prompt, String defaultValue) {
    super(project, true);
    myDefaultValue = defaultValue;
    setTitle(PerforceBundle.message("dialog.title.perforce.login"));
    myPrompt = prompt;
    init();
  }

  protected JComponent createNorthPanel() {
    final JLabel result = new JLabel(myPrompt);
    result.setUI(new MultiLineLabelUI());
    return result;
  }

  protected JComponent createCenterPanel() {
    myPasswordField = new JPasswordField();

    if (myDefaultValue != null) {
      myPasswordField.setText(myDefaultValue);
    }

    return myPasswordField;
  }

  public JComponent getPreferredFocusedComponent() {
    return myPasswordField;
  }

  public String getPassword() {
    return new String(myPasswordField.getPassword());
  }

  protected boolean isToBeShown() {
    return true;
  }

  protected void setToBeShown(boolean value, boolean onOk) {
    PerforceSettings.getSettings(myProject).LOGIN_SILENTLY = !value;
  }

  protected boolean shouldSaveOptionsOnCancel() {
    return false;
  }

  protected String getDoNotShowMessage() {
    return PerforceBundle.message("checkbox.configure.perforce.try.to.login.silently");
  }
}
