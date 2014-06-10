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
package com.intellij.j2meplugin.module.settings.doja;

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * User: anna
 * Date: Oct 11, 2004
 */
public class OptionalDojaSettingsEditor extends DialogWrapper {
  private final JPanel myWholePanel = new JPanel(new BorderLayout());
  private final JPanel myLabelPanel = new JPanel(new GridBagLayout());
  private final JPanel myTextFieldPanel = new JPanel(new GridBagLayout());
  private final JTextField[] myTextFields = new JTextField[DOJAApplicationType.ADDITIONAL_SETTINGS.length];
  private final HashMap<String, String> myAdditionalSettings;

  public OptionalDojaSettingsEditor(Component parent, HashMap<String, String> dojaSettings) {
    super(parent, true);
    myAdditionalSettings = dojaSettings;
    setTitle(J2MEBundle.message("module.settings.optional.doja.settings"));
    init();
  }

  protected JComponent createCenterPanel() {
    for (int i = 0; i < DOJAApplicationType.ADDITIONAL_SETTINGS.length; i++) {
      double last = i == DOJAApplicationType.ADDITIONAL_SETTINGS.length - 1 ? 1.0 : 0.0;
      myLabelPanel.add(new Label(DOJAApplicationType.ADDITIONAL_SETTINGS[i] + ":"),
                       new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, last, GridBagConstraints.NORTHWEST,
                                              GridBagConstraints.HORIZONTAL, new Insets(3, 5, 3, 0), 0, 0));
      myTextFields[i] = new JTextField();
      myTextFields[i].setText(myAdditionalSettings.get(DOJAApplicationType.ADDITIONAL_SETTINGS[i]));
      myTextFieldPanel.add(myTextFields[i], new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, last, GridBagConstraints.NORTHWEST,
                                                                   GridBagConstraints.HORIZONTAL, new Insets(4, 5, 4, 0), 0, 0));
    }
    myWholePanel.add(myLabelPanel, BorderLayout.WEST);
    myWholePanel.add(myTextFieldPanel, BorderLayout.CENTER);
    myWholePanel.setPreferredSize(new Dimension(350, 100));
    return myWholePanel;
  }

  protected void doOKAction() {
    for (int i = 0; i < DOJAApplicationType.ADDITIONAL_SETTINGS.length; i++) {
      if (myTextFields[i].getText() != null && myTextFields[i].getText().length() > 0) {
        myAdditionalSettings.put(DOJAApplicationType.ADDITIONAL_SETTINGS[i], myTextFields[i].getText());
      }
      else {
        myAdditionalSettings.remove(DOJAApplicationType.ADDITIONAL_SETTINGS[i]);
      }
    }
    super.doOKAction();
  }
}
