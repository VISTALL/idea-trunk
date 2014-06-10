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

package com.intellij.uml.dnd;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.dnd.ProjectViewDnDSupport;
import com.intellij.psi.*;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class UmlClassDiagrammDnDSupport implements ProjectViewDnDSupport<UmlNode, PsiElement> {
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private static final Comparator<PsiElement> PSI_COMPARATOR = new Comparator<PsiElement>() {
      public int compare(final PsiElement o1, final PsiElement o2) {
        if (o1 instanceof PsiPackage && o2 instanceof PsiClass) return -1;
        if (o2 instanceof PsiPackage && o1 instanceof PsiClass) return 1;
        return 0;
      }
    };

  public UmlClassDiagrammDnDSupport(GraphBuilder<UmlNode, UmlEdge> builder) {
    myBuilder = builder;
  }

  public boolean acceptDraggedElements(@NotNull final List<PsiElement> elements) {
    for (PsiElement element : elements) {
      if (element instanceof PsiPackage
          || (element instanceof PsiDirectory && JavaDirectoryService.getInstance().getPackage((PsiDirectory)element) != null)
          || (element instanceof PsiJavaFile && ((PsiJavaFile)element).getClasses().length > 0)
          || element instanceof PsiClass) {
      } else return false;
    }
    return true;
  }

  @NotNull
  public List<UmlNode> dropElements(@NotNull final List<PsiElement> elements) {
    List<PsiElement> accepted = new ArrayList<PsiElement>();
    for (PsiElement e : elements) {
      PsiElement element = UmlUtils.getNotNull(UmlUtils.getPsiClass(e), UmlUtils.getPsiPackage(e));
      final String fqn = UmlUtils.getFQN(element);
      if (fqn != null && fqn.length() > 0) {
        accepted.add(element);
      }
    }
    List<UmlNode> nodes = new ArrayList<UmlNode>();
    Collections.sort(accepted, PSI_COMPARATOR);
    for (PsiElement element : accepted) {
      final UmlNode node = UmlUtils.getDataModel(myBuilder).addElement(element, false);
      if (node != null) nodes.add(node);
    }
    //UmlUtils.updateGraph(myBuilder);
    return nodes;
  }
}
