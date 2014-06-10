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
package com.intellij.j2meplugin.module.settings.ui;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.j2meplugin.module.J2MEModuleBuilder;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * User: anna
 * Date: Sep 10, 2004
 */
public class J2MEModuleExplodedDirStep extends ModuleWizardStep {
  private final J2MEModuleBuilder myModuleBuilder;

  private final Icon myIcon;
  private final String myHelpId;
  private MobileExplodedPanel myExplodedPanel;
  private MobileBuildPanel myBuildPanel;
  private String myDefaultExplodedPath;

  public J2MEModuleExplodedDirStep(WizardContext wizardContext,
                                   J2MEModuleBuilder moduleBuilder,
                                   Icon wizardIcon,
                                   @NonNls String s) {

    myModuleBuilder = moduleBuilder;
    myIcon = wizardIcon;
    myHelpId = s;
  }

  public JComponent getPreferredFocusedComponent() {
    return myBuildPanel.getPreferredFocusedComponent();
  }

  public JComponent getComponent() {
    @NonNls final String output = "output";
    myDefaultExplodedPath = myModuleBuilder.getModuleFileDirectory() != null ?
                            myModuleBuilder.getModuleFileDirectory().replace('/', File.separatorChar) + File.separator +
                            output : "";
    myExplodedPanel =
    new MobileExplodedPanel(myModuleBuilder.getExplodedDirPath() != null,
                            myModuleBuilder.isExcludeFromContent(),
                            myModuleBuilder.isDefaultEDirectoryModified() && myModuleBuilder.getExplodedDirPath() != null
                            ? myModuleBuilder.getExplodedDirPath()
                            : myDefaultExplodedPath);
    myBuildPanel = new MobileBuildPanel(myModuleBuilder.getMobileApplicationType(),
                                        null,
                                        myModuleBuilder.getMobileModuleSettings());
    JPanel myWholePanel = new JPanel(new GridBagLayout());
    myWholePanel.add(myBuildPanel.createComponent(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                                                            GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    myWholePanel.add(myExplodedPanel.getComponent(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                                                                            GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    myWholePanel.setBorder(BorderFactory.createEtchedBorder());
    return myWholePanel;
  }

  public void updateDataModel() {
    storeDifference();
  }

  public boolean validate() throws ConfigurationException {
    myBuildPanel.apply();
    return true;
  }

  public Icon getIcon() {
    return myIcon;
  }

  public String getHelpId() {
    return myHelpId;
  }

  public void onStepLeaving() {
    myExplodedPanel.apply();
    storeDifference();
    try {
      myBuildPanel.apply();
    }
    catch (ConfigurationException e) {
      //ignore
    }

  }

  private void storeDifference() {
    if (myExplodedPanel.isPathEnabled()) {
      myModuleBuilder.setExplodedDirPath(myExplodedPanel.getExplodedDir());
      myModuleBuilder.setExcludeFromContent(myExplodedPanel.isExcludeFromContent());
      myModuleBuilder.setDefaultEDirectoryModified(!myDefaultExplodedPath.equals(myExplodedPanel.getExplodedDir()));
    }
    else {
      myModuleBuilder.setExplodedDirPath(null);
    }
  }
}
