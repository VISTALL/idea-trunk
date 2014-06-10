/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.actions.popup;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.PsiUtils;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author Konstantin Bulenkov
 */
public class ShowClassesUsed extends AddElementsFromPopupAction {
  public PsiElement[] getElements(PsiClass psiClass) {
    final Set<PsiClass> classes = new HashSet<PsiClass>();
    final Set<PsiModifierListOwner> modListOwners = new HashSet<PsiModifierListOwner>();

    for (PsiAnnotation annotation : PsiUtils.getAnnotations(psiClass, false)) {
      classes.add(PsiUtils.getPsiClass(annotation));
    }

    for (PsiField field : psiClass.getFields()) {
      modListOwners.add(field);
      classes.addAll(resolveType(field.getTypeElement()));
    }

    for (PsiMethod method : psiClass.getMethods()) {
      modListOwners.add(method);
      if (! method.isConstructor()) {
        classes.addAll(resolveType(method.getReturnTypeElement()));
      }

      for (PsiParameter parameter : method.getParameterList().getParameters()) {
        modListOwners.add(parameter);
        classes.addAll(resolveType(parameter.getTypeElement()));
      }
    }

    for (PsiModifierListOwner owner : modListOwners) {
      for (PsiAnnotation anno : PsiUtils.getAnnotations(owner, true)) {
        classes.add(PsiUtils.getPsiClass(anno));
      }      
    }

    classes.remove(null);
    return classes.toArray(new PsiElement[classes.size()]);
  }

  @NotNull
  public String getPopupTitle(GraphBuilder<UmlNode, UmlEdge> builder) {
    return "Classes from signatures";
  }

  @Nullable
  private static Set<PsiClass> resolveType(PsiTypeElement type) {
    final Set<PsiClass> classes =  new HashSet<PsiClass>();
    classes.add(PsiUtil.resolveClassInType(type.getType()));
    final PsiJavaCodeReferenceElement element = type.getInnermostComponentReferenceElement();
    if (element != null) {
      final PsiReferenceParameterList params = element.getParameterList();
      if (params != null) {
        for (PsiTypeElement typeElement : params.getTypeParameterElements()) {
          classes.addAll(resolveType(typeElement));
        }
      }
    }
    classes.remove(null);

    for (PsiClass aClass : classes.toArray(new PsiClass[classes.size()])) {
      if (aClass instanceof PsiTypeParameter) {
        classes.remove(aClass);
      }
    }
    return classes;
  }
}
