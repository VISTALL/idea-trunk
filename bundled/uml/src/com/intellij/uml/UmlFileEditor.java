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

package com.intellij.uml;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.DataManager;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.geom.YPoint;
import com.intellij.openapi.graph.layout.EdgeLayout;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.NodeCellEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.uml.components.UmlGraphComponent;
import com.intellij.uml.core.actions.UmlActions;
import com.intellij.uml.editors.UmlNodeEditorManager;
import com.intellij.uml.presentation.EdgeInfo;
import com.intellij.uml.presentation.UmlPresentation;
import com.intellij.uml.presentation.UmlPresentationBase;
import com.intellij.uml.presentation.UmlPresentationModel;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.util.messages.MessageBusConnection;
import org.jdom.Document;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class UmlFileEditor extends UserDataHolderBase implements FileEditor, DataProvider, DataContext {
  
  private UmlGraphComponent myPanel;
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private final Graph2D myGraph;
  private final Object myElement;
  @NonNls private static final String NAME = "UML";
  private final UmlPresentationBase myState;
  private UmlPresentation myStateFromFile;
  private boolean initialized;
  private boolean fitOnStartupFlag = false;
  private final VirtualFile myFile;
  private final UmlProvider myProvider;
  private final UmlPresentationModel myPresentationModel;

  public UmlFileEditor(final Object element, boolean initialized, final VirtualFile file, final Project project) {
    this.initialized = initialized;
    myFile = file;
    myGraph = GraphManager.getGraphManager().createGraph2D();

    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    myProvider = getProvider(file);
    myElement = element == null ? getFromFileState(myStateFromFile, project) : element;
    if (file instanceof UmlVirtualFileSystem.UmlVirtualFile) {
      ((UmlVirtualFileSystem.UmlVirtualFile)file).setPresentableName(myProvider.getElementManager().getElementTitle(myElement));
    }

    final UmlDataModel<?> model = myProvider.createDataModel(project, myElement, file);
    model.putUserData(UmlDataModel.ORIGINAL_ELEMENT_FQN, myProvider.getVfsResolver().getQualifiedName(myElement));
    myPresentationModel = new UmlPresentationModel(myGraph, project, myProvider);
    myState = (UmlPresentationBase)myPresentationModel.getPresentation();
    myState.setOriginalFQN(myProvider.getVfsResolver().getQualifiedName(myElement));
    myBuilder = GraphBuilderFactory.getInstance(project).createGraphBuilder(myGraph, view, new UmlDataModelWrapper(model), myPresentationModel);
    Utils.setProvider(myBuilder, myProvider);
    Utils.setGraphBuilder(myBuilder, model);
    myPresentationModel.registerElementProvidersActions();
    final ActionToolbar toolbar = UmlGraphComponent.createToolbarPanel(UmlActions.createToolbarActions(myBuilder));
    myPanel = new UmlGraphComponent(myBuilder, toolbar);
    Disposer.register(UmlFileEditor.this, myPanel);
    GraphViewUtil.addDataProvider(view, UmlFileEditor.this);
    toolbar.getComponent().putClientProperty(DataManager.CLIENT_PROPERTY_DATA_PROVIDER, UmlFileEditor.this);


    final MessageBusConnection connection = project.getMessageBus().connect();
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
      long count = myProvider.getModificationTracker(project).getModificationCount();
      @Override
      public void selectionChanged(FileEditorManagerEvent event) {
        if (event.getNewFile() == myFile
          && count != myProvider.getModificationTracker(project).getModificationCount()) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              Utils.updateGraph(myBuilder, false, false);
            }
          });
        }
      }
    });
  }

  private Object getFromFileState(UmlPresentation state, Project project) {
    final UmlProvider provider = UmlProvider.findByID(state.getProviderID());
    return provider == null ? null : provider.getVfsResolver().resolveElementByFQN(state.getOriginalFQN(), project);
  }

  @Nullable
  public UmlProvider getProvider(VirtualFile file) {
    if (file instanceof UmlVirtualFileSystem.UmlVirtualFile) {
      final UmlProvider umlProvider = ((UmlVirtualFileSystem.UmlVirtualFile)file).getUmlProvider();
      if (umlProvider != null) {
        try {
          return umlProvider.getClass().newInstance();
        } catch (Exception e) {//
        }        
      }
    } else if (file.getFileSystem() instanceof LocalFileSystem) {
      Document doc;
      try {
        doc = UmlUtils.readUmlFileFromFile(file.getInputStream());
        myStateFromFile = UmlEditorProvider.readUmlState(doc.getRootElement());
        return UmlProvider.findByID(myStateFromFile.getProviderID());
      } catch (Exception e) {//
      }
    }
    return null;
  }

  @NotNull
  public JComponent getComponent() {
    return myPanel.getComponent();
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return ((Graph2DView)myGraph.getCurrentView()).getCanvasComponent();
  }

  @NonNls
  @NotNull
  public String getName() {
    return NAME;
  }

  @NotNull
  public FileEditorState getState(@NotNull FileEditorStateLevel level) {
    myState.update(myBuilder);
    return myState;
  }

  public synchronized void setState(@NotNull FileEditorState editorState) {
    if (!initialized && editorState instanceof UmlPresentation) {
      final UmlPresentation state = myStateFromFile == null ?
                                       (UmlPresentation)editorState : myStateFromFile;
      final UmlDataModel model = Utils.getDataModel(myBuilder);
      final Collection<UmlNode> nodes = new ArrayList<UmlNode>(model.getNodes());
      for (UmlNode node : nodes) {
        model.removeNode(node);
      }
      myBuilder.updateDataModel();
      final UmlVfsResolver resolver = myProvider.getVfsResolver();
      final Project project = myBuilder.getProject();

      final Map<String, UmlNode> cache = new HashMap<String, UmlNode>();

      myState.copyFrom(state);

      for (String fqn : state.getFQNs()) {
        Object cl = resolver.resolveElementByFQN(fqn, project);
        if (cl != null) {
          cache.put(fqn, model.addElement(cl));
        }
      }

      myBuilder.updateGraph();

      //SELECTION
      final List<Node> selectedNodes = GraphViewUtil.getSelectedNodes(myBuilder.getGraph());
      for (Node node : selectedNodes) {
        myBuilder.getGraph().setSelected(node, false);
      }
      for (String fqn : state.getSelectedNodes()) {
        final UmlNode umlNode = cache.get(fqn);
        if (umlNode != null) {
          final Node node = myBuilder.getNode(umlNode);
          if (node != null) {
            myGraph.setSelected(node, true);
          }
        }
      }
      myState.copyFrom(state);
      myPresentationModel.update();

      //WTF???
      //if (GraphViewUtil.getSelectedNodes(myBuilder.getGraph()).size() == 0) {
      //  for (Node node : selectedNodes) {
      //    myBuilder.getGraph().setSelected(node, true);
      //  }
      //}

      initialized = true;

      ApplicationManagerEx.getApplicationEx().invokeLater(new Runnable(){
        public void run() {
          myBuilder.getView().setZoom(state.getZoom());
          myBuilder.getView().setCenter(state.getCenter().getX(), state.getCenter().getY());
          Map<UmlNode, String> fqnCache = new HashMap<UmlNode, String>();
          for (UmlNode node : myBuilder.getNodeObjects()) {
            String fqn = resolver.getQualifiedName(node.getIdentifyingElement());

            if (fqn != null) fqnCache.put(node, fqn);

            try {
              double x = Double.parseDouble(state.getNodeX(fqn));
              double y = Double.parseDouble(state.getNodeY(fqn));
              myBuilder.getGraph().setLocation(myBuilder.getNode(node), x, y);
            } catch (Exception e){//
            }
          }
          for (UmlEdge umlEdge : myBuilder.getEdgeObjects()) {
            final String src = fqnCache.get(umlEdge.getSource());
            final String trg = fqnCache.get(umlEdge.getTarget());
            final EdgeInfo info = state.getEdgeInfo(src, trg);
            final Edge edge = myBuilder.getEdge(umlEdge);
            if (info != null && edge != null && info.getPoints().size() > 1) {
              final List<Pair<Double,Double>> points = info.getPoints();
              final EdgeLayout edgeLayout = myGraph.getEdgeLayout(edge);
              if (edgeLayout != null) {
                edgeLayout.clearPoints();
                final YPoint srcP = GraphManager.getGraphManager().createYPoint(points.get(0).getFirst().doubleValue(),
                                                                                points.get(0).getSecond().doubleValue());
                final YPoint trgP = GraphManager.getGraphManager().createYPoint(points.get(points.size() - 1).getFirst().doubleValue(),
                                                                                points.get(points.size() - 1).getSecond().doubleValue());
                edgeLayout.setSourcePoint(srcP);
                edgeLayout.setTargetPoint(trgP);
                for (int i = 1; i < points.size() - 1; i++) {
                  edgeLayout.addPoint(points.get(i).getFirst().doubleValue(), points.get(i).getSecond().doubleValue());
                }
              }
            }
          }
        }
      });
    }
  }

  public boolean isModified() {
    return false;
  }

  public boolean isValid() {
    return true;
  }

  public void selectNotify() {
    if (!fitOnStartupFlag) {
      if (myElement != null) {
        myBuilder.getGraph().updateViews();
        ((Graph2DView)myGraph.getCurrentView()).fitContent();
      } else {
        myBuilder.updateGraph();
      }
      fitOnStartupFlag = true;
    }
    findAndSelectOriginNode();
  }

  private void findAndSelectOriginNode() {
    for (Node node : myGraph.getNodeArray()) {
      if (myGraph.isSelected(node)) return; //Already has selection
      UmlNode umlNode = myBuilder.getNodeObject(node);
      if (umlNode != null && umlNode.getIdentifyingElement() == myElement) {
        myGraph.setSelected(node, true);
        return;
      }
    }

    if (myGraph.N() > 0) {
      myGraph.setSelected(myGraph.firstNode(), true);
    }
  }

  public void deselectNotify() {
  }

  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  @Nullable
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  public FileEditorLocation getCurrentLocation() {
    throw new UnsupportedOperationException("getCurrentLocation is not implemented in : " + getClass());
  }

  @Nullable
  public StructureViewBuilder getStructureViewBuilder() {
   return GraphViewUtil.createStructureViewBuilder(GraphManager.getGraphManager().createOverview(myBuilder.getView()));
  }

  public void dispose() {
    Disposer.dispose(myBuilder);
    Disposer.dispose(myPanel);
  }

    @NonNls
    @Nullable
    public Object getData(@NonNls String dataId) {
      if (DataKeys.FILE_EDITOR.getName().equals(dataId)) {
        return this;
      }
      if (DataKeys.VIRTUAL_FILE.getName().equals(dataId)) {
        final NodeCellEditor editor = UmlNodeEditorManager.getInstance().getCurrentCellEditor();
        if (editor == null) {
          return this.myFile;
        }
      }

      return getData(dataId, myBuilder);
    }

    @NonNls
    @Nullable
    public static Object getData(@NonNls String dataId, @NonNls GraphBuilder<UmlNode, UmlEdge> myBuilder) {
      final UmlNodeEditorManager manager = UmlNodeEditorManager.getInstance();
      final NodeCellEditor editor = manager.getCurrentCellEditor();
      if (PlatformDataKeys.PROJECT.getName().equals(dataId)) {
        return myBuilder.getProject();
      }

      else if (dataId.equals(DataKeys.PSI_ELEMENT.getName())) {
        if (editor != null) return manager.getData(dataId);

        final List<Node> nodes = GraphViewUtil.getSelectedNodes(myBuilder.getGraph());
        if (nodes.size() == 1) {
          final UmlNode umlNode = myBuilder.getNodeObject(nodes.get(0));
          if (umlNode != null) {
            final Object element = umlNode.getIdentifyingElement();
            return element instanceof PsiElement && ((PsiElement)element).isValid() ? element : null;
          }
        }
      }

      else if (UmlDataKeys.BUILDER.getName().equals(dataId)) {
        return myBuilder;
      }

      else if (DataKeys.PSI_FILE.getName().equals(dataId)) {
        if (editor != null) return manager.getData(dataId);
        final Object element = getData(DataKeys.PSI_ELEMENT.getName(), myBuilder);
        if (element instanceof PsiElement) {
          return ((PsiElement)element).getContainingFile();
        }
      }

      else if (DataKeys.VIRTUAL_FILE.getName().equals(dataId)) {
        if (editor != null) return manager.getData(dataId);
        return null;
      }

      else if (PlatformDataKeys.HELP_ID.getName().equals(dataId)) {
        return "reference.uml.class.diagram";
      }

      else if (DataConstants.DOMINANT_HINT_AREA_RECTANGLE.equals(dataId)) {
        final Graph2DView view = myBuilder.getView();
        final List<Node> nodes = GraphViewUtil.getSelectedNodes(myBuilder.getGraph());
        final Component c = view.getComponent();
        final Point p = SwingUtilities.getRoot(c).getLocationOnScreen();
        if (nodes.size() == 1) {
          final Point np = UmlUtils.getNodeCoordinatesOnScreen(nodes.get(0), view);
          return new Rectangle(np.x - p.x, np.y - p.y, 0, 0);
        }
        final Point sp = c.getLocationOnScreen();

        return new Rectangle(sp.x - p.x + c.getWidth() / 3, sp.y - p.y + c.getHeight() * 3 / 7, 0, 0);
      }

      else if (DataKeys.NAVIGATABLE_ARRAY.getName().equals(dataId)) {
        final List<Node> nodes = GraphViewUtil.getSelectedNodes(myBuilder.getGraph());
        final List<NavigatablePsiElement> elements = new ArrayList<NavigatablePsiElement>();
        for (Node node : nodes) {
          final UmlNode umlNode = myBuilder.getNodeObject(node);
          if (umlNode != null) {
            final Object element = umlNode.getIdentifyingElement();
            if (element instanceof NavigatablePsiElement) {
              elements.add((NavigatablePsiElement)element);
            }
          }
        }
        return elements.toArray(new NavigatablePsiElement[elements.size()]);
      }
      return null;
    }

  public GraphBuilder<UmlNode, UmlEdge> getBuilder() {
    return myBuilder;
  }

  public Object getOriginalElement() {
    return myElement;
  }

  public VirtualFile getOriginalVirtualFile() {
    return myFile;
  }
}
