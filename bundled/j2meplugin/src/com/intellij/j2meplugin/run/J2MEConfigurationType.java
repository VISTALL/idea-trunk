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
package com.intellij.j2meplugin.run;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.MobileModuleUtil;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.general.UserDefinedOption;
import com.intellij.j2meplugin.util.MobileIcons;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;

/**
 * User: anna
 * Date: Aug 16, 2004
 */
public class J2MEConfigurationType implements LocatableConfigurationType {

  private final ConfigurationFactory myFactory;

  J2MEConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new J2MERunConfiguration("", project, this);
      }

      public RunConfiguration createConfiguration(String name, RunConfiguration template) {
        J2MERunConfiguration j2MERunConfiguration = (J2MERunConfiguration)template;
        final Module[] modules = j2MERunConfiguration.getModules();
        if (j2MERunConfiguration.getModule() == null) {
          if (modules != null && modules.length > 0) {
            j2MERunConfiguration.setModule(modules[0]);
          }
        }
        final Module module = j2MERunConfiguration.getModule();
        if (module != null) {
          final MobileModuleSettings mobileModuleSettings = MobileModuleSettings.getInstance(module);
          j2MERunConfiguration.JAD_NAME = mobileModuleSettings.getMobileDescriptionPath();
        }
        return super.createConfiguration(name, j2MERunConfiguration);
      }
    };
  }

  public String getDisplayName() {
    return J2MEBundle.message("run.configuration.title");
  }

  public String getConfigurationTypeDescription() {
    return J2MEBundle.message("run.configuration.full.name");
  }

  public Icon getIcon() {
    return MobileIcons.MOBILE_SMALL_ICON;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return "#com.intellij.j2meplugin.run.J2MEConfigurationType";
  }

  public static J2MEConfigurationType getInstance() {
    ConfigurationType[] configurationTypes = Extensions.getExtensions(CONFIGURATION_TYPE_EP);
    for(ConfigurationType type: configurationTypes) {
      if (type instanceof J2MEConfigurationType) {
        return (J2MEConfigurationType) type;
      }
    }
    assert false: "Couldn't find J2ME configuration type";
    return null;
  }

  public RunnerAndConfigurationSettings createConfigurationByLocation(Location location) {
    location = stepIntoSingleClass(location);
    final Project project = location.getProject();
    final PsiElement element = location.getPsiElement();
    final PsiClass aClass = getMobileExeClass(element, PsiManager.getInstance(project));
    if (aClass == null) return null;
    RunnerAndConfigurationSettings settings = RunManager.getInstance(project).createRunConfiguration("", getConfigurationFactories()[0]);
    final J2MERunConfiguration configuration = (J2MERunConfiguration)settings.getConfiguration();
    configuration.MAIN_CLASS_NAME = aClass.getQualifiedName();
    configuration.IS_CLASSES = true;
    configuration.userParameters = new ArrayList<UserDefinedOption>();
    configuration
      .setModule(ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(aClass.getContainingFile().getVirtualFile()));
    configuration.setName(configuration.getGeneratedName());
    return settings;
  }

  public boolean isConfigurationByLocation(final RunConfiguration configuration, final Location location) {
    final PsiClass aClass = getMobileExeClass(location.getPsiElement(), PsiManager.getInstance(location.getProject()));
    if (aClass == null) return false;
    return Comparing.equal(aClass.getQualifiedName(), ((J2MERunConfiguration)configuration).MAIN_CLASS_NAME);
  }

  private PsiClass getMobileExeClass(PsiElement element, final PsiManager manager) {
    while (element != null) {
      if (element instanceof PsiClass) {
        final PsiClass aClass = (PsiClass)element;
        if (isMobileExeClass(aClass, manager)) {
          return aClass;
        }
      }
      element = element.getParent();
    }
    return null;
  }

  private boolean isMobileExeClass(final PsiClass aClass, final PsiManager manager) {
    if (aClass instanceof PsiAnonymousClass) return false;
    if (!aClass.hasModifierProperty(PsiModifier.PUBLIC)) return false;
    if (aClass.hasModifierProperty(PsiModifier.ABSTRACT)) return false;
    if (aClass.getContainingClass() != null) return false;

    final GlobalSearchScope scope = GlobalSearchScope.allScope(manager.getProject());
    final MobileApplicationType[] existingMobileApplicationTypes = MobileModuleUtil.getExistingMobileApplicationTypes();
    for (int i = 0; i < existingMobileApplicationTypes.length; i++) {
      MobileApplicationType mobileApplicationType = existingMobileApplicationTypes[i];
      PsiClass mobileClass = JavaPsiFacade.getInstance(manager.getProject()).findClass(mobileApplicationType.getBaseClassName(), scope);
      if (mobileClass != null) {
        if (aClass.isInheritor(mobileClass, true)) return true;
      }
    }
    return false;
  }

  public static Location stepIntoSingleClass(final Location location) {
    PsiElement element = location.getPsiElement();
    if (PsiTreeUtil.getParentOfType(element, PsiClass.class) != null) return location;
    element = PsiTreeUtil.getParentOfType(element, PsiJavaFile.class);
    if (element == null) return location;
    final PsiJavaFile psiFile = ((PsiJavaFile)element);
    final PsiClass[] classes = psiFile.getClasses();
    if (classes.length != 1) return location;
    return PsiLocation.fromPsiElement(classes[0]);
  }
}

