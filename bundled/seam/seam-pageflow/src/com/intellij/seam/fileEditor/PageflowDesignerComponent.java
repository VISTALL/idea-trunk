package com.intellij.seam.fileEditor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.dnd.GraphDnDUtils;
import com.intellij.openapi.graph.builder.dnd.SimpleDnDPanel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.Overview;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.graph.*;
import com.intellij.seam.graph.dnd.PageflowDnDSupport;
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
public class PageflowDesignerComponent extends JPanel implements DataProvider, Disposable {
  @NonNls public static final String SEAM_PAGEFLOW_DESIGNER_COMPONENT = "SEAM_PAGEFLOW_DESIGNER_COMPONENT";
  @NonNls private static final String SEAM_PAGEFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME = "SEAM_PAGEFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME";

  @NonNls private final SeamPageflowDesignerNavigationProvider myNavigationProvider = new SeamPageflowDesignerNavigationProvider();

  private final GraphBuilder<PageflowNode, PageflowEdge> myBuilder;
  private final XmlFile myXmlFile;
  private final PageflowDataModel myDataModel;

  public PageflowDesignerComponent(final XmlFile xmlFile) {
    myXmlFile = xmlFile;
    final Project project = xmlFile.getProject();

    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    myDataModel = new PageflowDataModel(xmlFile);
    PageflowPresentationModel presentationModel = new PageflowPresentationModel(graph, project);

    myBuilder = GraphBuilderFactory.getInstance(project).createGraphBuilder(graph, view, myDataModel, presentationModel);

    GraphViewUtil.addDataProvider(view, new MyDataProvider(myBuilder));

    final Splitter splitter = new Splitter(false, 0.85f);

    setLayout(new BorderLayout());

    final SimpleDnDPanel simpleDnDPanel = GraphDnDUtils.createDnDActions(project, myBuilder, new PageflowDnDSupport(myDataModel));
    final JComponent graphComponent = myBuilder.getView().getJComponent();

    splitter.setSecondComponent(simpleDnDPanel.getTree());

    splitter.setFirstComponent(graphComponent);
    splitter.setDividerWidth(5);

    add(createToolbarPanel(), BorderLayout.NORTH);
    add(splitter, BorderLayout.CENTER);

    Disposer.register(this, myBuilder);

    myBuilder.initialize();

    DomManager.getDomManager(myBuilder.getProject()).addDomEventListener(new DomEventAdapter() {
      public void eventOccured(final DomEvent event) {
        if (isShowing()) {
          simpleDnDPanel.getBuilder().updateFromRoot();
          myBuilder.queueUpdate();
        }
      }
    }, this);
  }

  private JComponent createToolbarPanel() {
    DefaultActionGroup actions = new DefaultActionGroup();
    // todo add custom actions

    actions.add(GraphViewUtil.getBasicToolbar(myBuilder));

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);

    return actionToolbar.getComponent();
  }

  public List<DomElement> getSelectedDomElements() {
    List<DomElement> selected = new ArrayList<DomElement>();
    final Graph2D graph = myBuilder.getGraph();
    for (Node n : graph.getNodeArray()) {
      if (graph.isSelected(n)) {
        final PageflowNode nodeObject = myBuilder.getNodeObject(n);
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
    //final SeamPageflowDomElement pageflowDomElement = domElement.getParentOfType(SeamPageflowDomElement.class, false);
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

  private class SeamPageflowDesignerNavigationProvider extends DomElementNavigationProvider {
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

  public SeamPageflowDesignerNavigationProvider getNavigationProvider() {
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

  public PageflowDataModel getDataModel() {
    return myDataModel;
  }

  private class MyDataProvider implements DataProvider {
    private final Project myProject;
    private final Graph2D myGraph;

    public MyDataProvider(final GraphBuilder<PageflowNode, PageflowEdge> builder) {
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
            final PageflowNode pageflowNode = myBuilder.getNodeObject(node);
            if (pageflowNode != null) {
              return pageflowNode.getIdentifyingElement().getXmlElement();
            }
          }
        }
      }
      return null;
    }
  }
}