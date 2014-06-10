package com.intellij.seam.dependencies;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.components.BasicGraphComponent;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.Disposer;
import com.intellij.seam.dependencies.beans.SeamComponentNodeInfo;
import com.intellij.seam.dependencies.beans.SeamDependencyInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class SeamDependenciesFileEditor extends UserDataHolderBase implements FileEditor {
  private final BasicGraphComponent myPanel;
  private final GraphBuilder<SeamComponentNodeInfo, SeamDependencyInfo> myBuilder;

  public SeamDependenciesFileEditor(final Module module) {
    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();

    final SeamDependenciesDataModel model = new SeamDependenciesDataModel(module);
    final SeamDependenciesPresentationModel presentationModel = new SeamDependenciesPresentationModel(graph, module);

    myBuilder = GraphBuilderFactory.getInstance(module.getProject()).createGraphBuilder(graph, view, model, presentationModel);

    myPanel = new BasicGraphComponent<SeamComponentNodeInfo, SeamDependencyInfo>(myBuilder);

    Disposer.register(this, myPanel);
    
    GraphViewUtil.addDataProvider(view, new DataProvider() {
      @Nullable
      public Object getData(@NonNls String dataId) {
        if (PlatformDataKeys.PROJECT.getName().equals(dataId)) {
          return module.getProject();
        }
        return null;
      }
    });
  }


  @NotNull
  public JComponent getComponent() {
    return myPanel.getComponent();
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return ((Graph2DView)myBuilder.getGraph().getCurrentView()).getJComponent();
  }

  @NonNls
  @NotNull
  public String getName() {
    return "SeamDependenciesFileEditor";
  }

  @NotNull
  public FileEditorState getState(@NotNull FileEditorStateLevel level) {
    return FileEditorState.INSTANCE;
  }

  public void setState(@NotNull FileEditorState state) {
  }

  public boolean isModified() {
    return false;
  }

  public boolean isValid() {
    return true;
  }

  public void selectNotify() {
    myBuilder.updateGraph();
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
    myBuilder.dispose();
    myPanel.dispose();
  }
}
