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

import com.intellij.ui.IdeBorderFactory;
import com.intellij.uml.presentation.VisibilityLevel;
import com.intellij.uml.utils.UmlBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Konstantin Bulenkov
 */
public class UmlSettingsForm {
  private JCheckBox showFields;
  private JCheckBox showConstructors;
  private JCheckBox showMethods;
  private JCheckBox showProperties;
  private JCheckBox showInnerClasses;
  private JPanel classElementsVisibility;
  private JPanel commonVisibility;
  private JCheckBox showChanged;
  private JCheckBox showCamelNames;
  private JCheckBox showDependencies;
  private JComboBox visibilityLevel;
  private JComboBox layouts;
  private JCheckBox fitContentAfterLayout;
  private JCheckBox showColors;
  private JPanel visibilityPanel;
  private JPanel component;
  private UmlConfiguration myConfiguration;
  private boolean changed;

  public UmlSettingsForm(UmlConfiguration configuration) {
    myConfiguration = configuration;
    final ActionListener notifier = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        update();
        changed = true;
      }
    };
    initUI(myConfiguration);
    installListener(component, notifier);
  }

  private static void installListener(JComponent component, ActionListener listener) {
    if (component instanceof JCheckBox) {
      ((JCheckBox)component).addActionListener(listener);
      return;
    }
    if (component instanceof JComboBox) {
      ((JComboBox)component).addActionListener(listener);      
      return;
    }
    for (Component comp : component.getComponents()) {
      if (comp instanceof JComponent) {
        installListener((JComponent)comp, listener);
      }
    }
  }

  public void initUI(UmlConfiguration conf) {
    myConfiguration = conf;
    classElementsVisibility.setBorder(IdeBorderFactory.createTitledBorder(UmlBundle.message("class.elements")));
    visibilityPanel.setBorder(IdeBorderFactory.createTitledBorder(UmlBundle.message("default.visibility.settings")));
    commonVisibility.setBorder(IdeBorderFactory.createTitledBorder(UmlBundle.message("other")));

    showFields.setSelected(conf.showFields);
    showConstructors.setSelected(conf.showConstructors);
    showProperties.setSelected(conf.showProperties);
    showMethods.setSelected(conf.showMethods);
    showInnerClasses.setSelected(conf.showInnerClasses);

    showChanged.setSelected(conf.showChanges);
    showColors.setSelected(conf.showColors);
    showCamelNames.setSelected(conf.showCamelNames);
    showDependencies.setSelected(conf.showDependencies);
    fitContentAfterLayout.setSelected(conf.fitContentAfterLayout);

    for (UmlLayout layout : UmlLayout.values()) {
      layouts.addItem(layout.getPresentableName());
    }
    if (conf.layout == null) {
      conf.layout = UmlLayout.HIERARCHIC_GROUP;
    }
    layouts.setSelectedItem(conf.layout.getPresentableName());

    for (VisibilityLevel level : VisibilityLevel.values()) {
      visibilityLevel.addItem(level.toString());
    }
    if (conf.visibilityLevel == null) {
      conf.visibilityLevel = VisibilityLevel.PRIVATE;
    }
    visibilityLevel.setSelectedItem(conf.visibilityLevel.toString());
  }

  public JPanel getJComponent() {
    return component;
  }

  public void update() {
    myConfiguration.showFields = showFields.isSelected();
    myConfiguration.showConstructors = showConstructors.isSelected();
    myConfiguration.showProperties = showProperties.isSelected();
    myConfiguration.showMethods = showMethods.isSelected();
    myConfiguration.showInnerClasses = showInnerClasses.isSelected();

    myConfiguration.showChanges = showChanged.isSelected();
    myConfiguration.showColors = showColors.isSelected();
    myConfiguration.showCamelNames = showCamelNames.isSelected();
    myConfiguration.visibilityLevel = VisibilityLevel.fromString(visibilityLevel.getSelectedItem().toString());
    myConfiguration.showDependencies = showDependencies.isSelected();
    myConfiguration.fitContentAfterLayout = fitContentAfterLayout.isSelected();
    myConfiguration.layout = UmlLayout.fromString(layouts.getSelectedItem());
  }

  public boolean changed() {
    return changed;
  }

  public void setChanged(boolean changed) {
    this.changed = changed;
  }
}
