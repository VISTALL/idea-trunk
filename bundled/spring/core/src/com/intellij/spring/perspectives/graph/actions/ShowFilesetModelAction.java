package com.intellij.spring.perspectives.graph.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.hierarchy.GroupNodeRealizer;
import com.intellij.openapi.project.Project;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.perspectives.graph.SpringBeanDependenciesDataModel;
import com.intellij.spring.perspectives.graph.SpringBeanDependencyInfo;
import org.jetbrains.annotations.NotNull;

public class ShowFilesetModelAction extends AbstractSpringGraphToggleAction {

  public ShowFilesetModelAction() {
    super(SpringIcons.FILESET);
  }

  public ShowFilesetModelAction(final GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder) {
    super(builder, SpringIcons.FILESET);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    final GraphBuilder builder = getBuilder(e);

    e.getPresentation().setVisible(builder != null && builder.getGraphDataModel() instanceof SpringBeanDependenciesDataModel);
  }

  protected boolean isSelected(Graph2D graph, final Project project, final AnActionEvent event) {
    final GraphBuilder builder = getBuilder(event);

    if (builder != null) {
      final GraphDataModel dataModel = builder.getGraphDataModel();
      if (dataModel instanceof SpringBeanDependenciesDataModel) {
        return !((SpringBeanDependenciesDataModel)dataModel).getDiagramContext().isShowLocalModel();
      }
    }
    return false;
  }

  protected void setSelected(Graph2D graph, boolean state, final Project project, final AnActionEvent e) {
    final GraphBuilder builder = getBuilder(e);

    if (builder != null) {
      final GraphDataModel dataModel = builder.getGraphDataModel();
      if (dataModel instanceof SpringBeanDependenciesDataModel) {
        ((SpringBeanDependenciesDataModel)dataModel).getDiagramContext().setShowLocalModel(!state);
         builder.updateGraph();

        final Layouter layouter = GraphSettingsProvider.getInstance(project).getSettings(graph).getCurrentLayouter();
        for (Node node : graph.getNodeArray()) {
          if (graph.getRealizer(node) instanceof GroupNodeRealizer) {
            GraphViewUtil.setRenderedNodeSizes(graph, builder.getView(), node);
          }
        }
        if (layouter.canLayout(graph)) {
          GraphManager.getGraphManager().createBufferedLayouter(layouter).doLayout(graph);
        }
        builder.getView().fitContent();
      }
    }
  }

  protected String getText(final @NotNull Graph2D graph) {
    return SpringBundle.message("spring.bean.dependency.graph.action.show.fileset.model");
  }

}