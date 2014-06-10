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

package com.intellij.spring.perspectives.graph.actions;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.graph.GraphUtil;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringManager;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.perspectives.graph.SpringBeanDependenciesDiagramContext;
import com.intellij.spring.perspectives.graph.SpringBeanDependencyInfo;
import com.intellij.spring.perspectives.graph.SpringBeanNodeType;
import com.intellij.spring.perspectives.graph.SpringBeansDependencyGraphComponent;
import org.jetbrains.annotations.Nullable;

/**
 * @author Serega.Vasiliev
 */
public class ShowBeanDependenciesPopup extends CodeInsightAction {

  @Override
  public void update(AnActionEvent event) {
    super.update(event);
    event.getPresentation().setIcon(SpringIcons.SPRING_ICON);
    event.getPresentation().setVisible(event.getPresentation().isEnabled());
    event.getPresentation().setText(SpringBundle.message("show.bean.dependencies.diagramm"));
  }

  @Override
  protected CodeInsightActionHandler getHandler() {
    return new CodeInsightActionHandler() {
      public void invoke(Project project, Editor editor, PsiFile file) {
        SpringBean bean = SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
        if (bean != null) {
          SpringBeansDependencyGraphComponent graphComponent = createGraphComponent(file);
          if (graphComponent != null) {
            final JBPopup popup = createPopup(graphComponent, SpringBundle.message("bean.dependencies.popup.title", bean.getBeanName()));

            GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder = graphComponent.getBuilder();
            setNodeSelectionAndFilterDependencies(bean, builder);
            GraphUtil.setBestPopupSizeForGraph(popup, builder);

            popup.showInBestPositionFor(editor);
            builder.getView().fitContent();
            adjustScrollBar(builder);

            Disposer.register(popup, builder);
          }
        }
      }

      public boolean startInWriteAction() {
        return false;
      }
    };
  }

  private void adjustScrollBar(GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder) {
    //Call it twice. yFiles bug?
    Graph2DView graph2DView = (Graph2DView)builder.getGraph().getCurrentView();
    graph2DView.adjustScrollBarVisibility();
    graph2DView.adjustScrollBarVisibility();
  }

  @Nullable
  private SpringBeansDependencyGraphComponent createGraphComponent(final PsiFile file) {
    final SpringBeansDependencyGraphComponent[] graphComponent = {null};
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        graphComponent[0] = new SpringBeansDependencyGraphComponent((XmlFile)file, false, getPopupContext());
      }
    }, SpringBundle.message("generating.bean.dependencies.diagramm"), true, file.getProject());


    return graphComponent[0];
  }

  private JBPopup createPopup(final SpringBeansDependencyGraphComponent graphComponent, String title) {
    ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(graphComponent, graphComponent);

    popupBuilder.setResizable(true).setFocusable(true).setMovable(true).setTitle(title)
      .setTitleIcon(new ActiveIcon(SpringIcons.SPRING_ICON, SpringIcons.SPRING_ICON)).setCancelOnOtherWindowOpen(true).setAlpha(0.15f)
      .setRequestFocus(true);

    popupBuilder.addListener(new JBPopupAdapter() {
      @Override
      public void onClosed(LightweightWindowEvent event) {
        Disposer.dispose(graphComponent);
      }
    });
    return popupBuilder.createPopup();
  }

  private static void setNodeSelectionAndFilterDependencies(SpringBean bean,
                                                            GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder) {
    final Graph2D graph = builder.getGraph();
    SpringBaseBeanPointer pointer = SpringBeanPointer.createSpringBeanPointer(bean);

    for (Node n : graph.getNodeArray()) {
      final boolean selected = pointer.equals(builder.getNodeObject(n));
      graph.setSelected(n, selected);
    }

    GraphUtil.filterSelectedNodesDependencies(builder, false);
  }

  private SpringBeanDependenciesDiagramContext getPopupContext() {
    SpringBeanDependenciesDiagramContext diagramContext = new SpringBeanDependenciesDiagramContext();

    diagramContext.setRenderedNodeType(SpringBeanNodeType.POPUP);
    diagramContext.setGroupSpringBeans(false);
    diagramContext.setShowAutowiredDependencies(true);
    diagramContext.setShowEdgeLabels(true);
    diagramContext.setShowLocalModel(false);
    diagramContext.setFilterDependenciesMode(true);

    return diagramContext;
  }

  @Override
  protected boolean isValidForFile(Project project, Editor editor, PsiFile file) {
    return file instanceof XmlFile &&
           SpringManager.getInstance(project).isSpringBeans((XmlFile)file) &&
           SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file) != null;
  }
}