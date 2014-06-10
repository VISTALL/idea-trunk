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

package com.intellij.uml.actions.popup;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin Bulenkov
 */
public class ShowSubtypes extends AddElementsFromPopupAction {
  @Nullable("For the case the search has been cancelled")
  public PsiElement[] getElements(final PsiClass element) {
    final PsiElement[][] result = new PsiElement[1][];
    if (!ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        result[0] =  ClassInheritorsSearch.search(element).toArray(PsiClass.EMPTY_ARRAY);
      }
    }, UmlBundle.message("searching.for.subtypes.title", element.getQualifiedName()), true, element.getProject())) {
      return null;
    }
    return result[0];
  }

  @NotNull
  public String getPopupTitle(final GraphBuilder<UmlNode, UmlEdge> builder) {
    final PsiClass cls = getSelectedClass(builder);
    return UmlBundle.message("add.subtypes.to.the.diagram", cls == null ? "" : cls.getName());
  }
}
