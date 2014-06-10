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

package com.intellij.beanValidation.model;

import com.intellij.beanValidation.model.xml.ConstraintMappings;
import com.intellij.beanValidation.model.xml.Bean;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchScopeUtil;
import com.intellij.util.xml.ConvertContext;

/**
 * @author Konstantin Bulenkov
 */
public class BvModelUtils {
  public static String getDefaultPackageName(ConvertContext context) {
    final ConstraintMappings mappings = context.getInvocationElement().getParentOfType(ConstraintMappings.class, true);
    if (mappings != null) {
      final PsiPackage aPackage = mappings.getDefaultPackage().getValue();
      if (aPackage != null) {
        return aPackage.getQualifiedName();
      }
    }
    return null;
  }

  public static PsiClass findClass(String fqn, ConvertContext context, boolean useDefaultPackage) {
    final Module module = context.getModule();
    PsiClass result;
    if (module == null) return null;
    final GlobalSearchScope scope = module.getModuleWithDependenciesScope();
    final Project project = module.getProject();
    result = JavaPsiFacade.getInstance(project).findClass(fqn, scope);

    if (result != null || !useDefaultPackage) return result;

    final String packageName = getDefaultPackageName(context);
    return packageName != null ? JavaPsiFacade.getInstance(project).findClass(packageName + "." + fqn, scope) : null;
  }

  public static PsiPackage findPackage(String fqn, ConvertContext context) {
    final Module module = context.getModule();    
    if (module == null) return null;
    final GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(true);
    final Project project = module.getProject();
    final PsiPackage aPackage = JavaPsiFacade.getInstance(project).findPackage(fqn);
    if (aPackage == null) return null;
    return PsiSearchScopeUtil.isInScope(scope, aPackage) ? aPackage : null;
  }

  public static String getQualifiedName(PsiElement element, ConvertContext context, boolean useDefaultPackage) {
    if (element == null) return null;
    if (element instanceof PsiQualifiedNamedElement) {
      final String fqn = ((PsiQualifiedNamedElement)element).getQualifiedName();
      if (fqn == null) return null;
      if (useDefaultPackage) {
        final String packageName = getDefaultPackageName(context);
        if (packageName != null && fqn.startsWith(packageName)) {
          return fqn.substring(packageName.length() + 1);
        }
      }
      return fqn;
    }
    return null;
  }

  public static PsiField getBeanField(String name, ConvertContext context) {
    final Bean bean = context.getInvocationElement().getParentOfType(Bean.class, true);
    if (bean == null) return null;
    final PsiClass psiClass = bean.getClassAttr().getValue();
    if (psiClass == null) return null;
    return psiClass.findFieldByName(name, false);
  }
}
