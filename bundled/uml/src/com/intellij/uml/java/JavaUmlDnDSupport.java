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

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.uml.UmlDnDProvider;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlDnDSupport implements UmlDnDProvider<PsiElement> {
  public boolean isAcceptedForDnD(Object o, Project project) {
    return o instanceof PsiClass
           ||
           (o instanceof PsiDirectory && JavaDirectoryService.getInstance().getPackage((PsiDirectory)o) != null);
  }

  public PsiElement wrapToModelObject(Object o, Project project) {
    return o instanceof PsiClass ? (PsiClass)o : JavaDirectoryService.getInstance().getPackage((PsiDirectory)o);
  }
}
