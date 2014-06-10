/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

/*
 * User: anna
 * Date: 05-Feb-2007
 */
package com.intellij.j2meplugin.module.settings.ui;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.j2meplugin.module.J2MEModuleBuilder;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

public class J2MEModuleTypeStep extends ModuleWizardStep {
  private final J2MEModuleBuilder myBuilder;
  private final Icon myWizardIcon;
  private final String myHelpId;
  final J2MEModuleConfEditor myModuleConfEditor;


  public J2MEModuleTypeStep(J2MEModuleBuilder builder, final Icon wizardIcon, @NonNls final String helpId) {
    myBuilder = builder;
    myWizardIcon = wizardIcon;
    myHelpId = helpId;
    myModuleConfEditor = new J2MEModuleConfEditor(null, null) {
      public MobileApplicationType getApplicationType(final Module module) {
        return myBuilder.getMobileApplicationType();
      }

      public MobileModuleSettings getModuleSettings(final Module module) {
        return myBuilder.getMobileModuleSettings();
      }
    };
  }

  public JComponent getComponent() {
    final JPanel panel = myModuleConfEditor.createComponent();
    myModuleConfEditor.disableMidletProperties();
    myModuleConfEditor.reset();
    return panel;
  }

  public void updateDataModel() {
    try {
      myModuleConfEditor.apply();
    }
    catch (ConfigurationException e) {
      //can't be
    }
  }


  public void onStepLeaving() {
    updateDataModel();
  }

  @NonNls
  public String getHelpId() {
    return myHelpId;
  }


  public Icon getIcon() {
    return myWizardIcon;
  }
}
