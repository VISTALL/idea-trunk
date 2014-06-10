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

package com.intellij.uml.actions;

import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.psi.PsiElement;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.utils.UmlUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class SelectClassesFromPackage extends AbstractAction {
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private final boolean select;

  public SelectClassesFromPackage(final GraphBuilder<UmlNode, UmlEdge> builder, boolean select) {
    myBuilder = builder;
    this.select = select;
  }

  public void actionPerformed(final ActionEvent e) {
    UmlDiagramPresentation presentation = UmlUtils.getPresentation(myBuilder);
    if (select) {
      List<Node> nodes = GraphViewUtil.getSelectedNodes(myBuilder.getGraph());
      if (nodes.size() != 1) return;
      UmlNode umlNode = myBuilder.getNodeObject(nodes.get(0));
      if (umlNode == null) return;
      PsiElement element = umlNode.getIdentifyingElement();
      String fqn = UmlUtils.getRealPackageName(element);
      if (!UmlUtils.isEqual(fqn, presentation.getHighlightedPackage())) {
        presentation.setHighlightedPackage(fqn);
      }
    } else {
      presentation.setHighlightedPackage(null);
    }                                                                      
  }
}
