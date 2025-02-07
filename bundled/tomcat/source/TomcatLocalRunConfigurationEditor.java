/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package org.jetbrains.idea.tomcat;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.javaee.run.configuration.CommonModel;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

/**
 * @author nik
 */
public class TomcatLocalRunConfigurationEditor extends SettingsEditor<CommonModel> {
  private JCheckBox myRunTomcatManagerCheckBox;
  private JPanel myMainPanel;

  protected void resetEditorFrom(final CommonModel commonModel) {
    myRunTomcatManagerCheckBox.setSelected(((TomcatModel)commonModel.getServerModel()).DEPLOY_TOMCAT_MANAGER);
  }

  protected void applyEditorTo(final CommonModel commonModel) throws ConfigurationException {
    ((TomcatModel)commonModel.getServerModel()).DEPLOY_TOMCAT_MANAGER = myRunTomcatManagerCheckBox.isSelected();
  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void disposeEditor() {
  }
}
