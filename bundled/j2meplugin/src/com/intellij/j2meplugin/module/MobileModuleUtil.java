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

import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: anna
 * Date: Sep 20, 2004
 */
public class MobileModuleUtil {
  private MobileModuleUtil() {
  }
  /*public static MobileModuleSettings getMobileModuleSettings(Module module, MobileApplicationType mobileApplicationType) {
    MobileModuleSettings[] existSettings = module.getComponents(MobileModuleSettings.class);
    for (int i = 0; existSettings != null && i < existSettings.length; i++) {
      if (existSettings[i].getMobileApplicationType().equals(mobileApplicationType)) {
        return existSettings[i];
      }
    }
    return null;
  }*/


  public static MobileApplicationType[] getExistingMobileApplicationTypes() {
    return ApplicationManager.getApplication().getExtensions(MobileApplicationType.MOBILE_APPLICATION_TYPE);
  }

  @Nullable
  public static MobileApplicationType getMobileApplicationTypeByName(String name) {
    for (MobileApplicationType applicationType : getExistingMobileApplicationTypes()) {      
      if (Comparing.strEqual(name, applicationType.getName())) {
        return applicationType;
      }
    }
    return null;
  }

  public static boolean isExecutable(@Nullable PsiClass psiClass, @NotNull final Module module) {
    if (psiClass != null) {
      if (psiClass.getNameIdentifier() == null) return false; //exclude anonymous classes
      if (module == ModuleUtil.findModuleForPsiElement(psiClass)) {
        final J2MEModuleProperties moduleProperties = J2MEModuleProperties.getInstance(module);
        if (moduleProperties != null) {
          final Project project = module.getProject();
          final String fqName = moduleProperties.getMobileApplicationType().getBaseClassName();
          final PsiClass baseClass = JavaPsiFacade.getInstance(project).findClass(fqName, GlobalSearchScope.allScope(project));
          if (baseClass != null && psiClass.isInheritor(baseClass, true)) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
