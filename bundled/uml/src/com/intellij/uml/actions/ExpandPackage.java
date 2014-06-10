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

package com.intellij.uml.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.uml.utils.UmlBundle;

import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class ExpandPackage extends UmlAction {

  @Override
  public void update(final AnActionEvent e) {
    super.update(e);
    Graph2D graph = getGraph(e);
    boolean enabled = graph != null && GraphViewUtil.getSelectedNodes(graph).size() > 0;
    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setVisible(enabled);
  }

  public void actionPerformed(final AnActionEvent e) {
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    final Project project = DataKeys.PROJECT.getData(e.getDataContext());
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        if (builder == null) return;
        ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
        List<Node> nodes = GraphViewUtil.getSelectedNodes(builder.getGraph());
        boolean needUpdate = false;
        for (Node node : nodes) {
          UmlNode umlNode = builder.getNodeObject(node);
          if (umlNode != null) {
            PsiElement element = umlNode.getIdentifyingElement();
            if (element instanceof PsiPackage) {
              UmlUtils.getDataModel(builder).expandPackage((PsiPackage)element);
              needUpdate = true;
            }
          }
        }
        if (needUpdate) UmlUtils.updateGraph(builder, false, false);
      }
    },
      UmlBundle.message("rebuilding.uml.diagram"),
      true,
      project);
  }
}
