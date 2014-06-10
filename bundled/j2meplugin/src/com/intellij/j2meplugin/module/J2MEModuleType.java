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
package com.intellij.j2meplugin.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.MobileSdk;
import com.intellij.j2meplugin.module.settings.ui.J2MEModuleTypeStep;
import com.intellij.j2meplugin.util.MobileIcons;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.ArrayList;

/**
 * User: anna
 * Date: Aug 16, 2004
 */
public class J2MEModuleType extends ModuleType<J2MEModuleBuilder> {
  @NonNls public static final String ID = "J2ME_MODULE";

  public J2MEModuleType() {
    super(ID);
  }

  public static J2MEModuleType getInstance() {
    return (J2MEModuleType) ModuleTypeManager.getInstance().findByID(ID);
  }

  public J2MEModuleBuilder createModuleBuilder() {
    return new J2MEModuleBuilder();
  }

  public ModuleWizardStep[] createWizardSteps(final WizardContext wizardContext, final J2MEModuleBuilder moduleBuilder,
                                              ModulesProvider modulesProvider) {
    final ProjectWizardStepFactory wizardFactory = ProjectWizardStepFactory.getInstance();
    ArrayList<ModuleWizardStep> steps = new ArrayList<ModuleWizardStep>();
    steps.add(wizardFactory.createSourcePathsStep(wizardContext, moduleBuilder, MobileIcons.WIZARD_ICON, "reference.dialogs.new.project.fromScratch.source"));
    steps.add(wizardFactory.createProjectJdkStep(wizardContext, MobileSdk.getInstance(), moduleBuilder, new Computable<Boolean>() {
      public Boolean compute() {
        return Boolean.TRUE;
      }
    }, MobileIcons.WIZARD_ICON, "reference.dialogs.new.project.fromScratch.sdk"));
    steps.add(new J2MEModuleTypeStep(moduleBuilder, MobileIcons.WIZARD_ICON, "j2me.support.creating.mobile.module"));
    final ModuleWizardStep[] wizardSteps = steps.toArray(new ModuleWizardStep[steps.size()]);
    return ArrayUtil.mergeArrays(wizardSteps, super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider), ModuleWizardStep.class);
  }

  public String getName() {
    return J2MEBundle.message("module.type.title");
  }

  public String getDescription() {
    return J2MEBundle.message("module.type.description");
  }

  public Icon getBigIcon() {
    return MobileIcons.MOBILE_MODULE_ICON;
  }

  public Icon getNodeIcon(boolean isOpened) {
    return MobileIcons.MOBILE_SMALL_ICON;
  }

  @Override
  public boolean isValidSdk(final Module module, final Sdk projectSdk) {
    return JavaModuleType.isValidJavaSdk(module);
  }
}
