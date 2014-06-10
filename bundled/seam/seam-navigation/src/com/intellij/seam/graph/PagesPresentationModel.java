package com.intellij.seam.graph;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.DeleteProvider;
import com.intellij.openapi.graph.builder.EdgeCreationPolicy;
import com.intellij.openapi.graph.builder.components.SelectionDependenciesPresentationModel;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.xml.XmlElement;
import com.intellij.seam.graph.beans.*;
import com.intellij.seam.graph.renderers.DefaultPagesNodeRenderer;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class PagesPresentationModel extends SelectionDependenciesPresentationModel<BasicPagesNode, BasicPagesEdge> {
  private final Project myProject;
  private BasicGraphNodeRenderer myRenderer;

  public PagesPresentationModel(final Graph2D graph, Project project) {
    super(graph);
    myProject = project;
    setShowEdgeLabels(false);
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final BasicPagesNode node) {
    return GraphViewUtil.createNodeRealizer("PagesNodeRenderer", getRenderer());
  }

  public BasicGraphNodeRenderer getRenderer() {
    if (myRenderer == null) {
      myRenderer = new DefaultPagesNodeRenderer(getGraphBuilder());
    }
    return myRenderer;
  }

  @NotNull
  public EdgeRealizer getEdgeRealizer(final BasicPagesEdge edge) {
    final PolyLineEdgeRealizer edgeRealizer = GraphManager.getGraphManager().createPolyLineEdgeRealizer();

    final boolean isExceptionEdge = edge instanceof ExceptionEdge;
    edgeRealizer.setLineType(isExceptionEdge ? LineType.DASHED_1 : LineType.LINE_1);
    edgeRealizer.setArrow(isExceptionEdge? Arrow.DELTA : Arrow.STANDARD);
    edgeRealizer.setLineColor(Color.GRAY);

    return edgeRealizer;
  }

  public boolean editNode(final BasicPagesNode node) {
    final XmlElement xmlElement = node.getIdentifyingElement().getXmlElement();
    if (xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)xmlElement}, true);
      return true;
    }
    return super.editNode(node);
  }

  public boolean editEdge(final BasicPagesEdge pagesEdge) {
    final XmlElement xmlElement = pagesEdge.getViewId().getXmlElement();
    if (xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)xmlElement}, true);
      return true;
    }
    return super.editEdge(pagesEdge);
  }

  public Project getProject() {
    return myProject;
  }


  public String getNodeTooltip(final BasicPagesNode node) {
    return node.getName();
  }

  public String getEdgeTooltip(final BasicPagesEdge edge) {
    return edge.getName();
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    editMode.allowEdgeCreation(true);
    editMode.allowBendCreation(false);

    view.setFitContentOnResize(false);
    view.fitContent();
  }

  public DeleteProvider getDeleteProvider() {
    return new DeleteProvider<BasicPagesNode, BasicPagesEdge>() {
      public boolean canDeleteNode(@NotNull final BasicPagesNode node) {
        return !((CellEditorMode)getGraphBuilder().getEditMode().getEditNodeMode()).isCellEditing();
      }

      public boolean canDeleteEdge(@NotNull final BasicPagesEdge edge) {
        return true;
      }

      public boolean deleteNode(@NotNull final BasicPagesNode node) {
        final Collection<BasicPagesEdge> edges = getGraphBuilder().getEdgeObjects();

        final List<BasicPagesEdge> deleteThis = new ArrayList<BasicPagesEdge>();
        for (BasicPagesEdge edge : edges) {
          if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
            deleteThis.add(edge);
          }
        }

        new WriteCommandAction(getProject()) {
          protected void run(final Result result) throws Throwable {
            node.getIdentifyingElement().undefine();

            for (BasicPagesEdge edge : deleteThis) {
              if (edge.getViewId().isValid()) {
                edge.getViewId().undefine();
              }
            }
          }
        }.execute();


        
        return true;
      }

      public boolean deleteEdge(@NotNull final BasicPagesEdge edge) {
        new WriteCommandAction(getProject()) {
          protected void run(final Result result) throws Throwable {
            edge.getViewId().undefine();
          }
        }.execute();

        return true;
      }
    };
  }

  public NodeCellEditor getCustomNodeCellEditor(final BasicPagesNode pagesNode) {
    return null;
  }

  public DefaultActionGroup getNodeActionGroup(final BasicPagesNode pagesNode) {
    return super.getNodeActionGroup(pagesNode);
  }

  public EdgeCreationPolicy<BasicPagesNode> getEdgeCreationPolicy() {
    return new EdgeCreationPolicy<BasicPagesNode>() {
      public boolean acceptSource(@NotNull final BasicPagesNode source) {
        return source instanceof PageNode || source instanceof ExceptionNode;
      }

      public boolean acceptTarget(@NotNull final BasicPagesNode target) {
        return !target.getName().contains("*") && !(target instanceof ExceptionNode);
      }
    };
  }
}
