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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author Konstantin Bulenkov
 */
public class ShowParents extends AddElementsFromPopupAction {
  public PsiElement[] getElements(final PsiClass psiClass) {
    final Set<PsiClass> classes = getSupers(psiClass);
    return classes.toArray(new PsiClass[classes.size()]);
  }

  @NotNull
  public String getPopupTitle(GraphBuilder<UmlNode, UmlEdge> builder) {
    final PsiClass psiClass = getSelectedClass(builder);
    return UmlBundle.message("show.supers.for", psiClass == null ? "" : psiClass.getName());
  }

  public static Set<PsiClass> getSupers(PsiClass child) {
    Set<PsiClass> supers = new HashSet<PsiClass>();
    if (child == null) return supers;
    for (PsiClass psiClass : child.getSupers()) {
      supers.add(psiClass);
      supers.addAll(getSupers(psiClass));
    }
    return supers;
  }
}


