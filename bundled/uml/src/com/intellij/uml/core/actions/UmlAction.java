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

package com.intellij.uml.core.actions;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.uml.*;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Konstantin Bulenkov
 */
public abstract class UmlAction extends AnAction implements Toggleable {
  @Override
  public void update(final AnActionEvent e) {
    GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    e.getPresentation().setEnabled(builder != null && isEnabled(e, builder));
    if (builder != null) {
      e.getPresentation().putClientProperty(SELECTED_PROPERTY, isSelected(e, builder));
    }
  }

  @Nullable
  public static GraphBuilder<UmlNode, UmlEdge> getBuilder(final AnActionEvent e) {
    return getBuilderFromActionEvent(e);
  }

  public static UmlProvider getProvider(final AnActionEvent e) {
    return Utils.getProvider(getBuilder(e));
  }

  public static UmlDataModel getDataModel(final AnActionEvent e) {
    return Utils.getDataModel(getBuilder(e));
  }

  @Nullable
  public static GraphBuilder<UmlNode, UmlEdge> getBuilderFromActionEvent(final AnActionEvent e) {
    return UmlDataKeys.BUILDER.getData(e.getDataContext());
  }

  @Nullable
  public Graph2D getGraph(final AnActionEvent e) {
    GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    return builder == null ? null : builder.getGraph();
  }

  @Nullable
  public UmlDiagramPresentation getPresentation(final AnActionEvent e) {
    GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    return builder == null ? null : UmlUtils.getPresentation(builder);
  }

  public static boolean prepareClassForWrite(PsiClass psiClass) {
    final boolean prepared = CodeInsightUtil.preparePsiElementsForWrite(psiClass);
    if (!prepared) {
      Messages.showErrorDialog(psiClass.getProject(),
                               UmlBundle.message("class.is.readonly", psiClass.getName()),
                               UmlBundle.message("error"));
    }
    return prepared;
  }

  public boolean isSelected(final AnActionEvent e, GraphBuilder<UmlNode, UmlEdge> b) {
    return false;
  }

  public boolean isEnabled(final AnActionEvent e, GraphBuilder<UmlNode, UmlEdge> b) {
    return true;
  }

  public static List<UmlNode> getSelectedNodes(final AnActionEvent e) {
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    List<UmlNode> selected = new ArrayList<UmlNode>();
    if (builder != null) {
      for (UmlNode umlNode : builder.getNodeObjects()) {
        final Node node = builder.getNode(umlNode);
        if (node != null && builder.getGraph().isSelected(node)) {
          selected.add(umlNode);
        }
      }
    }
    return selected;
  }
}
