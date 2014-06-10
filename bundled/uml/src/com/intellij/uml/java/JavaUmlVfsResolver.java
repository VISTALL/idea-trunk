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

package com.intellij.uml.java;

import com.intellij.uml.UmlVfsResolver;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.uml.utils.UmlUtils;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlVfsResolver implements UmlVfsResolver<PsiElement> {
  public String getQualifiedName(PsiElement element) {
    return UmlUtils.getFQN(element);
  }

  public PsiElement resolveElementByFQN(String fqn, Project project) {
    final JavaPsiFacadeEx facadeEx = JavaPsiFacadeEx.getInstanceEx(project);
    final PsiClass psiClass = facadeEx.findClass(fqn, GlobalSearchScope.allScope(project));
    return psiClass == null ? facadeEx.findPackage(fqn) : psiClass;
  }
}
