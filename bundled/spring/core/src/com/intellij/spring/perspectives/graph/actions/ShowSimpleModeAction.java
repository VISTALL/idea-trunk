package com.intellij.spring.perspectives.graph.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.actions.layout.AbstractLayoutAction;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.project.Project;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.perspectives.graph.*;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class ShowSimpleModeAction extends AbstractSpringGraphToggleAction {

  public ShowSimpleModeAction() {
    super(SpringIcons.SPRING_ICON);
  }

  public ShowSimpleModeAction(final GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder) {
    super(builder, SpringIcons.SPRING_ICON);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    final GraphBuilder builder = getBuilder(e);

    e.getPresentation().setVisible(builder != null && builder.getGraphPresentationModel() instanceof SpringBeanDependenciesPresentation);
  }

  protected boolean isSelected(Graph2D graph, final Project project, final AnActionEvent event) {
    final GraphBuilder builder = getBuilder(event);

    if (builder != null) {
       final GraphDataModel dataModel = builder.getGraphDataModel();
      if (dataModel instanceof SpringBeanDependenciesDataModel) {
        return ((SpringBeanDependenciesDataModel)dataModel).getDiagramContext().getRenderedNodeType() == SpringBeanNodeType.SIMPLE;
      }
    }
    return false;
  }

  protected void setSelected(Graph2D graph, boolean state, final Project project, final AnActionEvent e) {
    final GraphBuilder builder = getBuilder(e);

    if (builder != null) {
       final GraphDataModel dataModel = builder.getGraphDataModel();
      if (dataModel instanceof SpringBeanDependenciesDataModel) {
        SpringBeanDependenciesDiagramContext context = ((SpringBeanDependenciesDataModel)dataModel).getDiagramContext();

        context.setRenderedNodeType(state ? SpringBeanNodeType.SIMPLE : SpringBeanNodeType.INFO);

        builder.updateRealizers(true);
        AbstractLayoutAction.doLayout((Graph2DView)graph.getCurrentView(), GraphSettingsProvider.getInstance(project).getSettings(graph).getCurrentLayouter(), project);
      }
    }
  }

  protected String getText(final @NotNull Graph2D graph) {
    return SpringBundle.message("spring.bean.dependency.graph.action.show.simple.mode");
  }

}