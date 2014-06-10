/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives.graph;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.components.SelectionDependenciesPresentationModel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.perspectives.graph.actions.GroupSpringBeansAction;
import com.intellij.spring.perspectives.graph.actions.ShowAutowiredDependencies;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class SpringBeanDependenciesPresentation
  extends SelectionDependenciesPresentationModel<SpringBaseBeanPointer, SpringBeanDependencyInfo> {
  private final Project myProject;
  private final SpringBeanDependenciesDataModel myDataModel;
  private final SpringBeanDependenciesDiagramContext myDiagramContext;

  private SimpleBeanNodeRenderer mySimpleBeanNodeRenderer;
  private SpringBeanNodeRenderer mySpringBeanNodeRenderer;
  private PopupBeanNodeRenderer myPopupBeanNodeRenderer;

  public SpringBeanDependenciesPresentation(final Graph2D graph,
                                           SpringBeanDependenciesDiagramContext diagramContext,
                                           SpringBeanDependenciesDataModel dataModel) {
    super(graph, diagramContext.isFilterDependenciesMode());
    
    myDiagramContext = diagramContext;
    myDataModel = dataModel;
    myProject = dataModel.getProject();

    setShowEdgeLabels(diagramContext.isShowEdgeLabels());
  }

  @Override
  protected void addGraph2DSelectionListener(GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> springBaseBeanPointerSpringBeanDependencyInfoGraphBuilder) {
    if (myDiagramContext.getRenderedNodeType() != SpringBeanNodeType.POPUP) {
      super.addGraph2DSelectionListener(springBaseBeanPointerSpringBeanDependencyInfoGraphBuilder);
    }
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final SpringBaseBeanPointer node) {
    return GraphViewUtil.createNodeRealizer("SpringNodeRenderer", getNodeCellRenderer());
  }

  private NodeCellRenderer getNodeCellRenderer() {
    SpringBeanNodeType nodeType = myDiagramContext.getRenderedNodeType();
    switch (nodeType) {
      case SIMPLE:
        return getSimpleNodeRenderer();
      case POPUP:
        return getPopupNodeRenderer();
    }

    return getBeanInfoNodeRenderer();
  }

  private NodeCellRenderer getPopupNodeRenderer() {
    if (myPopupBeanNodeRenderer == null) {
      myPopupBeanNodeRenderer = new PopupBeanNodeRenderer(getGraphBuilder(), getModificationTracker());
    }
    return myPopupBeanNodeRenderer;
  }

  private SpringBeanNodeRenderer getBeanInfoNodeRenderer() {
    if (mySpringBeanNodeRenderer == null) {
      mySpringBeanNodeRenderer = new SpringBeanNodeRenderer(getGraphBuilder(), getModificationTracker());
    }
    return mySpringBeanNodeRenderer;
  }

  private SimpleBeanNodeRenderer getSimpleNodeRenderer() {
    if (mySimpleBeanNodeRenderer == null) {
      mySimpleBeanNodeRenderer = new SimpleBeanNodeRenderer(getGraphBuilder(), getModificationTracker());
    }
    return mySimpleBeanNodeRenderer;
  }

  public ModificationTracker getModificationTracker() {
    return myModificationTracker;
  }

  @NotNull
  public EdgeRealizer getEdgeRealizer(@Nullable final SpringBeanDependencyInfo edge) {
    final PolyLineEdgeRealizer edgeRealizer = GraphManager.getGraphManager().createPolyLineEdgeRealizer();
    edgeRealizer.setLineType(LineType.LINE_1);

    if (edge == null) return edgeRealizer;

    switch (edge.getType()) {
      case SpringBeanDependencyInfo.INNER:
        edgeRealizer.setLineColor(Color.GREEN.darker());
        edgeRealizer.setTargetArrow(Arrow.NONE);
        edgeRealizer.setSourceArrow(Arrow.DIAMOND);
        break;
      case SpringBeanDependencyInfo.PARENT:
        edgeRealizer.setLineColor(Color.RED.darker());
        edgeRealizer.setSourceArrow(Arrow.NONE);
        edgeRealizer.setTargetArrow(Arrow.WHITE_DELTA);
        break;
      case SpringBeanDependencyInfo.AUTOWIRE:
      case SpringBeanDependencyInfo.ANNO_AUTOWIRED:
        edgeRealizer.setLineType(LineType.DASHED_1);
        edgeRealizer.setLineColor(Color.BLUE);
        edgeRealizer.setArrow(Arrow.STANDARD);
        break;
      default:
        edgeRealizer.setLineType(LineType.LINE_1);
        edgeRealizer.setLineColor(Color.GRAY);
        edgeRealizer.setArrow(Arrow.STANDARD);

        break;
    }
    return edgeRealizer;
  }

  public boolean editNode(final SpringBaseBeanPointer springBean) {
    if (springBean != null) {
      final PsiElement element = springBean.getPsiElement();
      if (element instanceof Navigatable) {
        OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)element}, true);
        return true;
      }
    }
    return super.editNode(springBean);
  }

  public Project getProject() {
    return myProject;
  }


  protected DefaultActionGroup getCommonActionGroup() {
    final DefaultActionGroup group = super.getCommonActionGroup();
    group.add(Separator.getInstance(), Constraints.LAST);
    group.add(new ShowAutowiredDependencies(), Constraints.LAST);
    group.add(new GroupSpringBeansAction(), Constraints.LAST);

    return group;
  }


  public String getNodeTooltip(@Nullable final SpringBaseBeanPointer springBean) {
    if (springBean == null) return null;

    StringBuffer tooltip = new StringBuffer();
    final String beanName = springBean.getName();
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanName != null) {
      tooltip.append("<tr><td><strong>bean:</strong></td><td>").append(beanName).append("</td></tr>");
    }
    if (beanClass != null) {
      tooltip.append("<tr><td><strong>class:</strong></td><td>").append(beanClass.getQualifiedName()).append("</td></tr>");
    }

    if (isShowContainingFileInfo()) {
      PsiFile file = springBean.getContainingFile();
      if (file != null) {
        tooltip.append("<tr><td><strong>location:</strong></td><td>").append(file.getName()).append("</td></tr>");
      }
    }

    if (tooltip.length() != 0) {
      return "<html><table>" + tooltip.toString() + "</table></html>";
    }

    return super.getNodeTooltip(springBean);
  }


  public String getEdgeTooltip(final SpringBeanDependencyInfo springBeanDependencyInfo) {
    final String sourceName = getName(springBeanDependencyInfo.getSource());
    final String targetName = getName(springBeanDependencyInfo.getTarget());

    final String dependencyName = springBeanDependencyInfo.getName();
    return StringUtil.isEmptyOrSpaces(dependencyName)
           ? ""
           : "<html>\"" + sourceName + "\" " + "<strong> " + dependencyName + " </strong>" + "\"" + targetName + "\"</html>";
  }

  private static String getName(final SpringBaseBeanPointer pointer) {
    final String name = pointer.getName();
    if (!StringUtil.isEmptyOrSpaces(name)) return name;

    final PsiClass psiClass = pointer.getBeanClass();
    if (psiClass != null) return psiClass.getName();

    return SpringBundle.message("spring.bean.with.unknown.name");
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    view.setAntialiasedPainting(false);
    editMode.allowEdgeCreation(false);
    editMode.allowBendCreation(false);

    if (myDiagramContext.getRenderedNodeType() == SpringBeanNodeType.POPUP) {
      editMode.setPopupMode(null);
      editMode.allowMoveSelection(false);
    }

    view.setFitContentOnResize(false);
    view.setGridVisible(false);
  }

  public boolean isShowContainingFileInfo() {
    final SpringModel springModel = myDataModel.getModel();

    if (springModel == null) return false;

    for (final XmlFile xmlFile : springModel.getConfigFiles()) {
      if (!xmlFile.isValid()) return false;
    }

    return springModel.getRoots().size() > 1;
  }

  public DefaultActionGroup getNodeActionGroup(final SpringBaseBeanPointer springBaseBeanPointer) {
    final DefaultActionGroup actionGroup = super.getNodeActionGroup(springBaseBeanPointer);
    actionGroup.add(ActionManager.getInstance().getAction("Beans.Dependencies.PsiElement.Actions"), Constraints.FIRST);
    return actionGroup;
  }

  private final ModificationTracker myModificationTracker = new ModificationTracker() {
    public long getModificationCount() {
      return PsiManager.getInstance(getProject()).getModificationTracker().getModificationCount();
    }
  };

}
