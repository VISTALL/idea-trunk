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

package org.jetbrains.plugins.grails;

import org.jetbrains.plugins.groovy.lang.psi.PropertyEnhancer;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author peter
 */
public class InjectedServiceTypeProvider extends PropertyEnhancer {
  @Override
  public PsiType getPropertyType(GrField property) {
    String name = property.getName();
    if (name != null && name.endsWith("Service")) {
      VirtualFile grailsApp = GrailsUtils.findModuleGrailsAppDir(ModuleUtil.findModuleForPsiElement(property));
      VirtualFile vFile = getVirtualFile(property);
      if (vFile != null && grailsApp != null && VfsUtil.isAncestor(grailsApp, vFile, true)) {
        VirtualFile servicesDir = grailsApp.findChild("services");
        if (servicesDir != null) {
          JavaPsiFacade facade = JavaPsiFacade.getInstance(property.getProject());
          for (PsiClass candidate : facade.getShortNamesCache().getClassesByName(StringUtil.capitalize(name), property.getResolveScope())) {
            VirtualFile candidateFile = getVirtualFile(candidate);
            if (candidateFile != null && VfsUtil.isAncestor(servicesDir, candidateFile, true)) {
              return facade.getElementFactory().createType(candidate);
            }
          }
        }
      }
    }
    return null;
  }

  @Nullable
  private static VirtualFile getVirtualFile(PsiElement candidate) {
    return candidate.getContainingFile().getOriginalFile().getVirtualFile();
  }

}
