/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives.graph;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.geom.YRectangle;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.Overview;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.perspectives.graph.actions.GroupSpringBeansAction;
import com.intellij.spring.perspectives.graph.actions.ShowAutowiredDependencies;
import com.intellij.spring.perspectives.graph.actions.ShowFilesetModelAction;
import com.intellij.spring.perspectives.graph.actions.ShowSimpleModeAction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomEventAdapter;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SpringBeansDependencyGraphComponent extends JPanel implements DataProvider, Disposable {
  @NonNls public static final String SPRING_BEAN_DEPENDENCIES_GRAPH_COMPONENT = "SPRING_BEAN_DEPENDENCIES_GRAPH_COMPONENT";
  @NonNls private static final String SPRING_BEAN_DEPENDENCIES_NAVIGATION_PROVIDER_NAME =
    "SPRING_BEAN_DEPENDENCIES_NAVIGATION_PROVIDER_NAME";

  @NonNls private final SpringStructureEditorNavigationProvider myNavigationProvider = new SpringStructureEditorNavigationProvider();

  private final GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> myBuilder;
  private final XmlFile myXmlFile;
  private final SpringBeanDependenciesDataModel myDataModel;
  private SpringBeanDependenciesPresentation myPresentationModel;

  public SpringBeansDependencyGraphComponent(final XmlFile xmlFile) {
     this(xmlFile, true, SpringBeanDependenciesDiagramContext.DEFAULT);
  }

  public SpringBeansDependencyGraphComponent(final XmlFile xmlFile, boolean createToolbar, SpringBeanDependenciesDiagramContext diagramContext) {
    myXmlFile = xmlFile;
    final Project project = xmlFile.getProject();

    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    myDataModel = new SpringBeanDependenciesDataModel(xmlFile, diagramContext);
    myPresentationModel = new SpringBeanDependenciesPresentation(graph,diagramContext, myDataModel);

    myBuilder = GraphBuilderFactory.getInstance(project).createGraphBuilder(graph, view, myDataModel, myPresentationModel);

    setLayout(new BorderLayout());

    if(createToolbar) {
      add(createToolbarPanel(), BorderLayout.NORTH);
    }
    add(myBuilder.getView().getComponent(), BorderLayout.CENTER);

    GraphViewUtil.addDataProvider(view, new MyDataProvider(myBuilder));
    Disposer.register(this, myBuilder);

    myBuilder.initialize();

    DomManager.getDomManager(myBuilder.getProject()).addDomEventListener(new DomEventAdapter() {
      public void eventOccured(final DomEvent event) {
        if (isShowing()) {
          myBuilder.queueUpdate();
        }
      }
    }, this);
  }

  private JComponent createToolbarPanel() {
    DefaultActionGroup actions = new DefaultActionGroup();

    actions.add(new ShowFilesetModelAction(myBuilder));
    actions.add(new GroupSpringBeansAction(myBuilder));
    actions.add(new ShowAutowiredDependencies(myBuilder));
    actions.add(new ShowSimpleModeAction(myBuilder));
    actions.addSeparator();
    actions.add(GraphViewUtil.getBasicToolbar(myBuilder));

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);

    return actionToolbar.getComponent();
  }

  public List<SpringBeanPointer> getSelectedBeans() {
    List<SpringBeanPointer> selected = new ArrayList<SpringBeanPointer>();
    final Graph2D graph = myBuilder.getGraph();
    for (Node n : graph.getNodeArray()) {
      if (graph.isSelected(n)) {
        ContainerUtil.addIfNotNull(myBuilder.getNodeObject(n), selected);
      }
    }
    return selected;
  }

  public void setSelectedDomElement(final DomElement domElement) {
    if (domElement == null) return;

    myBuilder.updateGraph();

    final SpringBean springBean = domElement.getParentOfType(SpringBean.class, false);
    if (springBean == null) return;

    final Node selectedNode = myBuilder.getNode(SpringBeanPointer.createSpringBeanPointer(springBean));

    if (selectedNode != null) {
      final Graph2D graph = myBuilder.getGraph();

      for (Node n : graph.getNodeArray()) {
        final boolean selected = n.equals(selectedNode);
        graph.setSelected(n, selected);
        if (selected) {
          final YRectangle yRectangle = graph.getRectangle(n);
          if (!myBuilder.getView().getVisibleRect().contains(
            new Rectangle((int)yRectangle.getX(), (int)yRectangle.getY(), (int)yRectangle.getWidth(), (int)yRectangle.getHeight()))) {
            myBuilder.getView().setCenter(graph.getX(n), graph.getY(n));
          }
        }
      }
    }
    myBuilder.getView().updateView();
  }

  public GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> getBuilder() {
    return myBuilder;
  }

  public Overview getOverview() {
    return GraphManager.getGraphManager().createOverview(myBuilder.getView());
  }

  public void dispose() {
  }

  private class SpringStructureEditorNavigationProvider extends DomElementNavigationProvider {
    public String getProviderName() {
      return SPRING_BEAN_DEPENDENCIES_NAVIGATION_PROVIDER_NAME;
    }

    public void navigate(final DomElement domElement, final boolean requestFocus) {
      setSelectedDomElement(domElement);
    }

    public boolean canNavigate(final DomElement domElement) {
      return domElement.isValid();
    }
  }

  public SpringStructureEditorNavigationProvider getNavigationProvider() {
    return myNavigationProvider;
  }

  public XmlFile getXmlFile() {
    return myXmlFile;
  }

  @Nullable
  public Object getData(@NonNls final String dataId) {
    if (dataId.equals(SPRING_BEAN_DEPENDENCIES_GRAPH_COMPONENT)) return this;

    return null;
  }

  public SpringBeanDependenciesDataModel getDataModel() {
    return myDataModel;
  }

  private class MyDataProvider implements DataProvider {
    private final Project myProject;
    private final Graph2D myGraph;

    public MyDataProvider(@NotNull final GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder) {
      myProject = builder.getProject();
      myGraph = builder.getGraph();
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
      if (dataId.equals(DataConstants.PROJECT)) {
        return myProject;
      }
      else if (dataId.equals(DataConstants.PSI_ELEMENT)) {
        for (Node node : myGraph.getNodeArray()) {
          if (myGraph.getRealizer(node).isSelected()) {
            final SpringBaseBeanPointer beanPointer = myBuilder.getNodeObject(node);
            if (beanPointer != null) {
              return beanPointer.getPsiElement();
            }
          }
        }
      }
      return null;
    }
  }

  public SpringBeanDependenciesPresentation getPresentationModel() {
    return myPresentationModel;
  }
}