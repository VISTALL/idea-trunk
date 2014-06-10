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

package com.intellij.uml.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.uml.utils.UmlIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlConfigurable implements Configurable {
  @NonNls
  private static final String NAME = "UML";
  private UmlSettingsForm settings;
  private UmlConfiguration myConfiguration;

  public UmlConfigurable(UmlConfiguration configuration) {
    myConfiguration = configuration;
  }

  @Nls
  public String getDisplayName() {
    return NAME;
  }

  public Icon getIcon() {
    return UmlIcons.UML_ICON;
  }

  public String getHelpTopic() {
    return "reference.settings.ide.settings.uml";
  }

  public JComponent createComponent() {
    if (settings == null) {
      settings = new UmlSettingsForm(myConfiguration);
    }
    return settings.getJComponent();
  }

  public boolean isModified() {
    return settings.changed();
  }

  public void apply() throws ConfigurationException {
    settings.setChanged(false);
  }

  public void reset() {
  }

  public void disposeUIResources() {
  }
}
