package com.intellij.seam.graph;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.DeleteProvider;
import com.intellij.openapi.graph.builder.SimpleNodeCellEditor;
import com.intellij.openapi.graph.builder.components.BasicGraphPresentationModel;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.layout.OrientationLayouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicGroupLayouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicLayouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.xml.XmlElement;
import com.intellij.seam.graph.renderers.DefaultPageflowNodeRenderer;
import com.intellij.seam.model.xml.pageflow.PageflowNamedElement;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PageflowPresentationModel extends BasicGraphPresentationModel<PageflowNode, PageflowEdge> {
  private final Project myProject;
  private BasicGraphNodeRenderer myRenderer;

  public PageflowPresentationModel(final Graph2D graph, Project project) {
    super(graph);
    myProject = project;
    setShowEdgeLabels(true);

    customizeDefaultSettings(GraphSettingsProvider.getInstance(project).getSettings(graph));
  }

  private static void customizeDefaultSettings(final GraphSettings settings) {
    final HierarchicGroupLayouter groupLayouter = settings.getGroupLayouter();

    groupLayouter.setOrientationLayouter(GraphManager.getGraphManager().createOrientationLayouter(OrientationLayouter.TOP_TO_BOTTOM));
    groupLayouter.setMinimalNodeDistance(20);
    groupLayouter.setMinimalLayerDistance(50);
    groupLayouter.setRoutingStyle(HierarchicLayouter.ROUTE_POLYLINE);
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final PageflowNode node) {
    return GraphViewUtil.createNodeRealizer("PageflowNodeRenderer", getRenderer());
  }

  public BasicGraphNodeRenderer getRenderer() {
    if (myRenderer == null) {
      myRenderer = new DefaultPageflowNodeRenderer(getGraphBuilder());
    }
    return myRenderer;
  }

  @NotNull
  public EdgeRealizer getEdgeRealizer(final PageflowEdge edge) {
    final PolyLineEdgeRealizer edgeRealizer = GraphManager.getGraphManager().createPolyLineEdgeRealizer();

    edgeRealizer.setLineType(LineType.LINE_1);
    edgeRealizer.setLineColor(Color.GRAY);
    edgeRealizer.setArrow(Arrow.STANDARD);

    return edgeRealizer;
  }

  public boolean editNode(final PageflowNode node) {
    return super.editNode(node);
  }

  public boolean editEdge(final PageflowEdge pageflowEdge) {
    // todo inplace editing
    // final Edge edge = getGraphBuilder().getEdge(pageflowEdge);
    //final EdgeRealizer realizer = getGraphBuilder().getGraph().getRealizer(edge);
    //
    //final EdgeLabel edgeLabel = realizer.getLabel();
    //if (edgeLabel != null) {
    //  final PropertyChangeListener listener = new PropertyChangeListener() {
    //    public void propertyChange(final PropertyChangeEvent evt) {
    //      final String newValue = evt.getNewValue().toString();
    //      if (newValue != null) {
    //        new WriteCommandAction(getProject()) {
    //          protected void run(final Result result) throws Throwable {
    //            pageflowEdge.getIdentifyingElement().getName().setStringValue(newValue);
    //          }
    //        }.execute();
    //      }
    //    }
    //  };
    //
    //  getGraphBuilder().getView().openLabelEditor(edgeLabel, edgeLabel.getBox().getX(), edgeLabel.getBox().getY(), listener, true);
    //}
    final XmlElement xmlElement = pageflowEdge.getIdentifyingElement().getXmlElement();
    if (xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)xmlElement}, true);
      return true;
    }
    return super.editEdge(pageflowEdge);
  }

  public Project getProject() {
    return myProject;
  }


  public String getNodeTooltip(final PageflowNode node) {
    return node.getName();
  }

  public String getEdgeTooltip(final PageflowEdge edge) {
    return edge.getName();
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    editMode.allowEdgeCreation(true);
    editMode.allowBendCreation(false);

    view.setFitContentOnResize(false);
    view.fitContent();
  }

  public DeleteProvider getDeleteProvider() {
    return new DeleteProvider<PageflowNode, PageflowEdge>() {
      public boolean canDeleteNode(@NotNull final PageflowNode node) {
        return !((CellEditorMode)getGraphBuilder().getEditMode().getEditNodeMode()).isCellEditing();
      }

      public boolean canDeleteEdge(@NotNull final PageflowEdge edge) {
        return true;
      }

      public boolean deleteNode(@NotNull final PageflowNode node) {
        new WriteCommandAction(getProject()) {
          protected void run(final Result result) throws Throwable {
             node.getIdentifyingElement().undefine();
          }
        }.execute();

        return true;
      }

      public boolean deleteEdge(@NotNull final PageflowEdge edge) {
        new WriteCommandAction(getProject()) {
          protected void run(final Result result) throws Throwable {
             edge.getIdentifyingElement().undefine();
          }
        }.execute();

        return true;
      }
    };
  }

  public NodeCellEditor getCustomNodeCellEditor(final PageflowNode pageflowNode) {
    return new SimpleNodeCellEditor<PageflowNode>(pageflowNode, getProject()) {
      protected String getEditorValue(final PageflowNode value) {
        final String s = value.getName();
        return s == null ? "" : s;
      }

      protected void setEditorValue(final PageflowNode value, final String newValue) {
        final DomElement element = value.getIdentifyingElement();
        if (element instanceof PageflowNamedElement) {
          new WriteCommandAction(myProject) {
            protected void run(final Result result) throws Throwable {
              ((PageflowNamedElement)element).getName().setStringValue(newValue);
            }
          }.execute();
        }

        IdeFocusManager.getInstance(getProject()).requestFocus(getGraphBuilder().getView().getJComponent(), true);
      }
    };
  }

  public DefaultActionGroup getNodeActionGroup(final PageflowNode pageflowNode) {
    final DefaultActionGroup group = super.getNodeActionGroup(pageflowNode);

    group.add(ActionManager.getInstance().getAction("Pageflow.Designer"), Constraints.FIRST);

    return group;
  }
}
