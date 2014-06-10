package com.intellij.seam.fileEditor;

import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.dnd.ProjectViewDnDHelper;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.Overview;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.graph.PagesDataModel;
import com.intellij.seam.graph.PagesPresentationModel;
import com.intellij.seam.graph.beans.BasicPagesEdge;
import com.intellij.seam.graph.beans.BasicPagesNode;
import com.intellij.seam.graph.dnd.PagesProjectViewDnDSupport;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomEventAdapter;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class PagesGraphComponent extends JPanel implements DataProvider, Disposable {
  @NonNls public static final String SEAM_PAGEFLOW_DESIGNER_COMPONENT = "SEAM_PAGEFLOW_DESIGNER_COMPONENT";
  @NonNls private static final String SEAM_PAGEFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME = "SEAM_PAGEFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME";

  @NonNls private final SeamPagesGraphNavigationProvider myNavigationProvider = new SeamPagesGraphNavigationProvider();

  private final GraphBuilder<BasicPagesNode, BasicPagesEdge> myBuilder;
  private final XmlFile myXmlFile;
  private final PagesDataModel myDataModel;

  public PagesGraphComponent(final XmlFile xmlFile) {
    myXmlFile = xmlFile;
    final Project project = xmlFile.getProject();

    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    myDataModel = new PagesDataModel(xmlFile);
    PagesPresentationModel presentationModel = new PagesPresentationModel(graph, project);

    myBuilder = GraphBuilderFactory.getInstance(project).createGraphBuilder(graph, view, myDataModel, presentationModel);

    setLayout(new BorderLayout());

    add(createToolbarPanel(), BorderLayout.NORTH);
    add(myBuilder.getView().getJComponent(), BorderLayout.CENTER);

    Disposer.register(this, myBuilder);

    myBuilder.initialize();

    addDnDSupport(xmlFile, myBuilder);
    
    DomManager.getDomManager(myBuilder.getProject()).addDomEventListener(new DomEventAdapter() {
      public void eventOccured(final DomEvent event) {
        if (isShowing()) {
          myBuilder.queueUpdate();
        }
      }
    }, this);
  }

  private static void addDnDSupport(final XmlFile xmlFile, final GraphBuilder<BasicPagesNode, BasicPagesEdge> builder) {
    final WebFacet webFacet = WebUtil.getWebFacet(xmlFile.getContainingFile());
    if (webFacet != null) {
      ProjectViewDnDHelper.getInstance(xmlFile.getProject())
        .addProjectViewDnDSupport(builder, new PagesProjectViewDnDSupport(xmlFile, builder, webFacet));
    }
  }

  private JComponent createToolbarPanel() {
    DefaultActionGroup actions = new DefaultActionGroup();

    actions.add(GraphViewUtil.getBasicToolbar(myBuilder));

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);

    return actionToolbar.getComponent();
  }

  public List<DomElement> getSelectedDomElements() {
    List<DomElement> selected = new ArrayList<DomElement>();
    final Graph2D graph = myBuilder.getGraph();
    for (Node n : graph.getNodeArray()) {
      if (graph.isSelected(n)) {
        final BasicPagesNode nodeObject = myBuilder.getNodeObject(n);
        if (nodeObject != null) {
          ContainerUtil.addIfNotNull(nodeObject.getIdentifyingElement(), selected);
        }
      }
    }
    return selected;
  }

  public void setSelectedDomElement(final DomElement domElement) {
    //if (domElement == null) return;
    //
    //final SeamPagesDomElement pageflowDomElement = domElement.getParentOfType(SeamPagesDomElement.class, false);
    //if (pageflowDomElement == null) return;
    //
    //final Node selectedNode = myBuilder.getNode(pageflowDomElement);
    //
    //if (selectedNode != null) {
    //  final Graph2D graph = myBuilder.getGraph();
    //
    //  for (Node n : graph.getNodeArray()) {
    //    final boolean selected = n.equals(selectedNode);
    //    graph.setSelected(n, selected);
    //    if (selected) {
    //      final YRectangle yRectangle = graph.getRectangle(n);
    //      if (!myBuilder.getView().getVisibleRect().contains(
    //        new Rectangle((int)yRectangle.getX(), (int)yRectangle.getY(), (int)yRectangle.getWidth(), (int)yRectangle.getHeight()))) {
    //        myBuilder.getView().setCenter(graph.getX(n), graph.getY(n));
    //      }
    //    }
    //  }
    //}
    //myBuilder.getView().updateView();
  }

  public GraphBuilder getBuilder() {
    return myBuilder;
  }

  public Overview getOverview() {
    return GraphManager.getGraphManager().createOverview(myBuilder.getView());
  }

  public void dispose() {
  }

  private class SeamPagesGraphNavigationProvider extends DomElementNavigationProvider {
    public String getProviderName() {
      return SEAM_PAGEFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME;
    }

    public void navigate(final DomElement domElement, final boolean requestFocus) {
      setSelectedDomElement(domElement);
    }

    public boolean canNavigate(final DomElement domElement) {
      return domElement.isValid();
    }
  }

  public SeamPagesGraphNavigationProvider getNavigationProvider() {
    return myNavigationProvider;
  }

  public XmlFile getXmlFile() {
    return myXmlFile;
  }

  @Nullable
  public Object getData(@NonNls final String dataId) {
    if (dataId.equals(SEAM_PAGEFLOW_DESIGNER_COMPONENT)) return this;

    return null;
  }

  public PagesDataModel getDataModel() {
    return myDataModel;
  }

  }