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

package com.intellij.uml.java.providers;

import com.intellij.openapi.project.Project;
import static com.intellij.psi.CommonClassNames.JAVA_LANG_OBJECT;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.uml.providers.ImplementationsProvider;

import java.util.Comparator;

/**
 * @author Konstantin Bulenkov
 */
public class PsiClassImplementations extends ImplementationsProvider<PsiElement>{
  public PsiClass[] getElements(PsiElement element, Project project) {
    return element instanceof PsiClass && JAVA_LANG_OBJECT.equals(((PsiClass)element).getQualifiedName()) ?
    PsiClass.EMPTY_ARRAY : ClassInheritorsSearch.search((PsiClass)element).toArray(PsiClass.EMPTY_ARRAY);
  }

  public String getHeaderName(PsiElement element, Project project) {
    return "Implementations of " + (element instanceof PsiClass ? ((PsiClass)element).getName() : "");
  }

  public Comparator getComparator() {
    return PSI_COMPARATOR;
  }
}
