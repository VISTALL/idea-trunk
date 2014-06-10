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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.uml.actions.DefaultUmlActions;
import com.intellij.uml.actions.UmlDataKeys;
import com.intellij.uml.components.UmlGraphComponent;
import com.intellij.uml.editors.UmlNodeEditorManager;
import com.intellij.uml.model.UmlClassDiagramDataModel;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.presentation.UmlClassDiagramPresentationModel;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.uml.utils.VcsUtils;
import com.intellij.util.containers.HashMap;
import com.intellij.util.messages.MessageBusConnection;
import org.jdom.Document;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Bulenkov
 */
public class UmlClassDiagramFileEditor extends UserDataHolderBase implements FileEditor, DataProvider, DataContext {
  private UmlGraphComponent myPanel;
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private final Graph2D myGraph;
  private final PsiElement myPsiElement;
  @NonNls private static final String NAME = "UML";
  private final UmlDiagramState myState = UmlDiagramState.getDefault();
  private final UmlDiagramState myStateFromFile = null;
  private boolean initialized;
  private boolean fitOnStartupFlag = false;
  private final VirtualFile myFile;


  public UmlClassDiagramFileEditor(final PsiElement psiElement, boolean initialized, final VirtualFile file, final Project project) {
    myPsiElement = psiElement;
    this.initialized = initialized;
    myFile = file;
    myGraph = GraphManager.getGraphManager().createGraph2D();

    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    final UmlClassDiagramDataModel model = myPsiElement == null
                                           ? new UmlClassDiagramDataModel(project, file) : new UmlClassDiagramDataModel(psiElement, myFile);
    final UmlClassDiagramPresentationModel presentationModel = VcsUtils.isShowChangesFile(myFile) ?
                                                               new UmlClassDiagramPresentationModel(myGraph, project, VcsUtils.CHANGES_PRESENTATION)
                                                               :
                                                               new UmlClassDiagramPresentationModel(myGraph, project, UmlDiagramState.getDefault());
    myBuilder = GraphBuilderFactory.getInstance(project).createGraphBuilder(myGraph, view, model, presentationModel);
    model.setBuilder(myBuilder);
    final ActionToolbar toolbar = UmlGraphComponent.createToolbarPanel(DefaultUmlActions.createToolbarActions());


    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
        myPanel = new UmlGraphComponent(myBuilder, toolbar);
        Disposer.register(UmlClassDiagramFileEditor.this, myPanel);
        GraphViewUtil.addDataProvider(view, UmlClassDiagramFileEditor.this);
        toolbar.getComponent().putClientProperty(DataManager.CLIENT_PROPERTY_DATA_PROVIDER, UmlClassDiagramFileEditor.this);
        if (file != null) {
          Document doc;
          final String[] error = {""};
          try {
            doc = UmlUtils.readUmlFileFromFile(file.getInputStream());
            final UmlDiagramState state = UmlClassDiagramEditorProvider.readUmlState(doc.getRootElement());
            setState(state);
          } catch (Exception e) {
            error[0] = UmlBundle.message("cant.read.uml.file", file.getName());
          }

        }
      }
    }, UmlBundle.message("building.uml.class.diagram"), true, project);

    final MessageBusConnection connection = project.getMessageBus().connect();
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
      long count = PsiManager.getInstance(project).getModificationTracker().getJavaStructureModificationCount();
      @Override
      public void selectionChanged(FileEditorManagerEvent event) {
        if (event.getNewFile() == myFile
          && count != PsiManager.getInstance(project).getModificationTracker().getJavaStructureModificationCount()) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              UmlUtils.updateGraph(myBuilder, false, false);
            }
          });
        }
      }
    });
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
    if (!initialized && editorState instanceof UmlDiagramState) {
      final UmlDiagramState state = myStateFromFile == null ?
                                       (UmlDiagramState)editorState : myStateFromFile;
      UmlClassDiagramDataModel model = UmlUtils.getDataModel(myBuilder);
      model.removeAllElements();
      myBuilder.updateDataModel();
      GlobalSearchScope scope = GlobalSearchScope.allScope(myBuilder.getProject());
      final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(myBuilder.getProject());

      UmlDiagramPresentation presentation = UmlUtils.getPresentation(myBuilder);
      presentation.setCamel(state.isCamel());
      presentation.setColorManagerEnabled(state.isColorManagerEnabled());
      presentation.setFieldsVisible(state.isFieldsVisible());
      presentation.setConstructorVisible(state.isConstructorsVisible());
      presentation.setMethodsVisible(state.isMethodsVisible());
      presentation.setShowDependencies(state.isShowDependencies());
      presentation.setShowInnerClasses(state.isShowInnerClasses());
      presentation.setVisibilityLevel(state.getVisibilityLevel());
      presentation.setPropertiesVisible(state.isPropertiesVisible());
      presentation.setVcsFilterEnabled(state.isVcsFilterEnabled());
      presentation.setFitContentAfterLayout(state.isFitContentAfterLayout());

      myBuilder.getView().setZoom(state.getZoom());

      for (String fqn : state.classes) {
        PsiClass cl = psiFacade.findClass(fqn, scope);
        if (cl != null) {
          model.addElement(cl);
        }
      }

      for (String fqn : state.packages) {
        PsiPackage p = psiFacade.findPackage(fqn);
        if (p != null) {
          model.addElement(p);
        }
      }

      myBuilder.updateGraph();
      //SELECTION
      final List<Node> selectedNodes = GraphViewUtil.getSelectedNodes(myBuilder.getGraph());
      for (Node node : selectedNodes) {
        myBuilder.getGraph().setSelected(node, false);
      }
      for (String fqn : state.getSelectedNodes()) {
        final Node node = myBuilder.getNode(UmlUtils.getNodeByFQN(fqn, myBuilder));
        if (node != null) {
          myGraph.setSelected(node, true);
        }
      }
      if (GraphViewUtil.getSelectedNodes(myBuilder.getGraph()).size() == 0) {
        for (Node node : selectedNodes) {
          myBuilder.getGraph().setSelected(node, true);
        }
      }

      initialized = true;
      
      ApplicationManagerEx.getApplicationEx().invokeLater(new Runnable(){
        public void run() {                            
          myBuilder.getView().setZoom(state.getZoom());
          Map<UmlNode, String> fqnCache = new HashMap<UmlNode, String>();
          for (UmlNode node : myBuilder.getNodeObjects()) {
            final PsiElement element = node.getIdentifyingElement();
            String fqn = null;
            if (element instanceof PsiClass) {
              fqn = ((PsiClass) element).getQualifiedName();
            } else if (element instanceof PsiPackage) {
              fqn = ((PsiPackage)element).getQualifiedName();
            }

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
            final UmlDiagramState.EdgeInfo info = state.getEdgeInfo(src, trg);
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
      if (myPsiElement != null) {
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
      if (umlNode != null && umlNode.getIdentifyingElement() == myPsiElement) {
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
            final PsiElement element = umlNode.getIdentifyingElement();
            return element.isValid() ? element : null;
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
        return UmlUtils.getDataModel(myBuilder).getFile();
      }

      else if (PlatformDataKeys.HELP_ID.getName().equals(dataId)) {
        return "reference.uml.class.diagram";
      }

      else if (DataKeys.NAVIGATABLE_ARRAY.getName().equals(dataId)) {
        final List<Node> nodes = GraphViewUtil.getSelectedNodes(myBuilder.getGraph());
        final List<NavigatablePsiElement> elements = new ArrayList<NavigatablePsiElement>();
        for (Node node : nodes) {
          final UmlNode umlNode = myBuilder.getNodeObject(node);
          if (umlNode != null && umlNode.getIdentifyingElement().isValid()) {
            final PsiElement element = umlNode.getIdentifyingElement();
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
}
