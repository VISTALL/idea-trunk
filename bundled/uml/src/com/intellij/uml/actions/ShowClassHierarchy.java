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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.GraphUtil;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ActiveIcon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiClass;
import com.intellij.uml.model.UmlClassDiagramDataModel;
import com.intellij.uml.UmlClassDiagramFileEditor;
import com.intellij.uml.UmlDiagramState;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.presentation.UmlClassDiagramPresentationModel;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlIcons;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class ShowClassHierarchy extends AnAction {
  @Override
  public void update(AnActionEvent e) {
    boolean enabled = ShowUmlClassDiagram.getPsiClass(e) != null;
    e.getPresentation().setVisible(enabled);
    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setIcon(UmlIcons.UML_ICON);
  }

  public void actionPerformed(AnActionEvent e) {
    final PsiClass psiClass = ShowUmlClassDiagram.getPsiClass(e);
    if (psiClass == null) return;

    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    final UmlClassDiagramDataModel model = new UmlClassDiagramDataModel(psiClass, null);
    final Project project = psiClass.getProject();
    final UmlClassDiagramPresentationModel presentationModel = new UmlClassDiagramPresentationModel(graph, project, UmlDiagramState.getDefault());
    presentationModel.setPopupMode(true);
    final GraphBuilder<UmlNode,UmlEdge> builder = GraphBuilderFactory
      .getInstance(project).createGraphBuilder(graph, view, model, presentationModel);
    model.setBuilder(builder);

    //Make it AFTER model.setBuilder
    presentationModel.getPresentation().setFitContentAfterLayout(false);

    view.getCanvasComponent().setBackground(Color.GRAY);
    builder.updateGraph();
    final JComponent jComponent = builder.getView().getJComponent();
    final JBPopup popup =
      JBPopupFactory.getInstance()
        .createComponentPopupBuilder(jComponent, jComponent)
        .setResizable(true)
        .setFocusable(true)
        .setMovable(true)
        .setTitle(UmlBundle.message("uml.class.diagram", psiClass.getName()))
        .setTitleIcon(new ActiveIcon(UmlIcons.UML_ICON, UmlIcons.UML_ICON))
        .setCancelOnOtherWindowOpen(true)
        .setAlpha(0.15f)
        .setRequestFocus(true)
        .createPopup();
    GraphUtil.setBestPopupSizeForGraph(popup, builder);
    popup.showInBestPositionFor(e.getDataContext());
    builder.getView().fitContent();
    builder.putUserData(UmlDataKeys.UML_POPUP, popup);
    Disposer.register(popup, builder);
    registerUmlActionsOnComponent(jComponent);

    //Call it twice. yFiles bug?
    view.adjustScrollBarVisibility();
    view.adjustScrollBarVisibility();

    GraphViewUtil.addDataProvider(view, new DataProvider() {
      public Object getData(@NonNls String dataId) {
        return UmlClassDiagramFileEditor.getData(dataId, builder);
      }
    });
  }

  String[] actions = {"Uml.ShowSubtypes", "Uml.ShowSupers", "Uml.ShowUsed", "EditSource"};
  private void registerUmlActionsOnComponent(JComponent component) {
    final ActionManager manager = ActionManager.getInstance();
    for (String action_id : actions) {
      final AnAction action = manager.getAction(action_id);
      if (action != null) {
        action.registerCustomShortcutSet(action.getShortcutSet(), component);
      }
    }
  }
}
