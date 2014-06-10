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

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uml.presentation.EdgeInfo;
import com.intellij.uml.presentation.UmlPresentationBase;
import com.intellij.uml.settings.UmlLayout;
import com.intellij.uml.utils.VcsUtils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.IOException;

/**
 * @author Konstantin Bulenkov
 */
public class UmlEditorProvider implements FileEditorProvider {
  @NonNls private static final String NODES = "nodes";
  @NonNls private static final String SETTINGS = "settings";
  @NonNls private static final String EDGES = "edges";
  @NonNls private static final String SELECTED_NODES = "SelectedNodes";
  @NonNls private static final String IS_CAMEL = "isCamel";
  @NonNls private static final String IS_COLOR_MANAGER_ENABLED = "isColorManagerEnabled";
  @NonNls private static final String IS_SHOW_DEPENDENCIES = "isShowDependencies";
  @NonNls private static final String IS_VCS_FILTER_ENABLED = "isVcsFilterEnabled";
  @NonNls private static final String LAYOUT = "layout";
  @NonNls private static final String VISIBILITY_LEVEL = "defaultVisibility";
  @NonNls private static final String ZOOM = "zoom";
  @NonNls private static final String EDGE = "edge";
  @NonNls private static final String X = "x";
  @NonNls private static final String Y = "y";
  @NonNls private static final String SOURCE = "source";
  @NonNls private static final String TARGET = "target";
  @NonNls private static final String RELATIONSHIP = "relationship";
  @NonNls private static final String POINT = "point";
  @NonNls private static final String NODE = "node";
  @NonNls private static final String CATEGORIES = "Categories";
  private static final String CATEGORY = "Category";
  @NonNls private static final String ORIGINAL_ELEMENT = "OriginalElement";
  @NonNls private static final String ID = "ID";

  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    if (UmlFileType.EXTENSION.equalsIgnoreCase(file.getExtension()) && isNewUmlFormat(file)) return true;
    
    return file instanceof UmlVirtualFileSystem.UmlVirtualFile
           && ((UmlVirtualFileSystem.UmlVirtualFile)file).getUmlProvider() != null
           && (isNameAsRealFQN(project, file) || VcsUtils.isShowChangesFile(file));
  }

  private boolean isNewUmlFormat(VirtualFile file) {
    byte[] bytes = new byte[100];
    try {
      file.getInputStream().read(bytes, 0, 100);
      return new String(bytes).contains("<Diagramm>");
    } catch (IOException e) {//
    }

    return false;
  }

  private static boolean isNameAsRealFQN(Project project, VirtualFile file) {
    return getElementFromFile(project, file) != null;
  }

  @Nullable
  private static Object getElementFromFile(Project project, VirtualFile file) {
    if (file instanceof UmlVirtualFileSystem.UmlVirtualFile) {
      final UmlVirtualFileSystem.UmlVirtualFile umlVirtualFile = (UmlVirtualFileSystem.UmlVirtualFile)file;
      final UmlProvider umlProvider = umlVirtualFile.getUmlProvider();
      if (umlProvider != null) {
        return umlProvider.getVfsResolver().resolveElementByFQN(umlVirtualFile.getFQN(), project);
      }
    }
    return null;
  }

  @NotNull
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    if (file instanceof UmlVirtualFileSystem.UmlVirtualFile) {
      return createEditorFromPsiElement(project, file);
    } else {
      return new UmlFileEditor(null, false, file, project);
    }
  }

  private static FileEditor createEditorFromPsiElement(@NotNull Project project, @NotNull VirtualFile file) {
    boolean initialized = UmlVirtualFileSystem.isInitialized(file);
    return new UmlFileEditor(getElementFromFile(project, file), initialized, file, project);
  }

  public void disposeEditor(@NotNull FileEditor editor) {
    Disposer.dispose(editor);
  }

  @NotNull
  public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
    return readUmlState(sourceElement);
  }

  private static boolean getBoolean(Attribute attr) {
    return attr != null && Boolean.valueOf(attr.getValue()).booleanValue();
  }

  private static double getDouble(Attribute attr, double defaultValue) {
    try {
      return Double.parseDouble(attr.getValue());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public void writeState(@NotNull FileEditorState editorState, @NotNull Project project, @NotNull Element element) {
    saveUmlState(editorState, element);
  }

  private static class SimpleElement extends Element {
    public SimpleElement(final String name, final String... params) {
      super(name);
      if (params.length % 2 != 0) throw new IllegalArgumentException("Number of parameters should be even");

      for (int i = 0; i < params.length; i += 2) {
        setAttribute(params[i], params[i+1]);
      }
    }
  }

  @NotNull
  @NonNls
  public String getEditorTypeId() {
    return "UmlEditorProvider";
  }

  @NotNull
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }


  public static void saveUmlState(@NotNull FileEditorState editorState, @NotNull Element root) {
    if (editorState instanceof UmlPresentationBase) {
      UmlPresentationBase state = (UmlPresentationBase)editorState;
      Element id = new Element(ID);
      id.setText(state.getProviderID());
      root.addContent(id);

      Element originalFQN = new Element(ORIGINAL_ELEMENT);
      originalFQN.setText(state.getOriginalFQN());
      root.addContent(originalFQN);

      Element fqns = new Element(NODES);
      for (String fqn : state.getFQNs()) {
        final SimpleElement child = new SimpleElement(NODE, X, state.getNodeX(fqn), Y, state.getNodeY(fqn));
        child.setText(fqn);
        fqns.addContent(child);
      }
      root.addContent(fqns);

      Element edges = new Element(EDGES);
      for (EdgeInfo edgeInfo : state.getEdgeInfos()) {
        final SimpleElement edge = new SimpleElement(EDGE,
                                                     SOURCE, edgeInfo.getSrc(),
                                                     TARGET, edgeInfo.getTrg());
        for (Pair<Double, Double> p : edgeInfo.getPoints()) {
          edge.addContent(new SimpleElement(POINT,
                                            X, p.getFirst().toString(),
                                            Y, p.getSecond().toString()));
        }
        edges.addContent(edge);
      }
      root.addContent(edges);


      Element settings = new SimpleElement(SETTINGS,
        IS_CAMEL, String.valueOf(state.isCamel()),
        IS_COLOR_MANAGER_ENABLED, String.valueOf(state.isColorManagerEnabled()),
        IS_SHOW_DEPENDENCIES, String.valueOf(state.isShowDependencies()),
        IS_VCS_FILTER_ENABLED, String.valueOf(state.isVcsFilterEnabled()),
        LAYOUT, state.getLayout().getPresentableName(),
        ZOOM, String.valueOf(state.getZoom()),
        X, String.valueOf(state.getCenter().getX()),
        Y, String.valueOf(state.getCenter().getY()));
      root.addContent(settings);
      Element selectedNodes = new Element(SELECTED_NODES);
      for (String fqn : state.getSelectedNodes()) {
        final Element child = new Element(NODE);
        child.setText(fqn);
        selectedNodes.addContent(child);
      }
      root.addContent(selectedNodes);

      Element categories = new Element(CATEGORIES);
      for (UmlCategory category : state.getEnabledCategories()) {
        final Element cat = new Element(CATEGORY);
        cat.setText(category.getName());
        categories.addContent(cat);
      }
      root.addContent(categories);
    }
  }

  @NotNull
  public static UmlPresentationBase readUmlState(@NotNull Element root) {
    UmlPresentationBase state = new UmlPresentationBase();
    final Element providerID = root.getChild(ID);
    final String id = providerID.getText();
    final UmlProvider provider = id == null ? null : UmlProvider.findByID(id);
    if (provider != null) {
      state.setProviderID(providerID == null ? null : provider.getID());
    }

    final Element originalFQN = root.getChild(ORIGINAL_ELEMENT);
    if (originalFQN != null) {
      state.setOriginalFQN(originalFQN.getText());
    }

    Element nodes = root.getChild(NODES);
    if (nodes != null) {
      for (Object e : nodes.getChildren()) {
        if (e instanceof Element) {
          String fqn = ((Element)e).getText();
          if (fqn != null) {
            state.addFQN(fqn);
            try {
              final double x = Double.parseDouble(((Element)e).getAttributeValue(X));
              final double y = Double.parseDouble(((Element)e).getAttributeValue(Y));
              state.setNodeCoord(fqn, x, y);
            } catch (Exception ex){//
            }
          }
        }
      }
    }

    Element settings = root.getChild(SETTINGS);
    if (settings != null) {
      state.setCamel(getBoolean(settings.getAttribute(IS_CAMEL)));
      state.setColorManagerEnabled(getBoolean(settings.getAttribute(IS_COLOR_MANAGER_ENABLED)));
      state.setVcsFilterEnabled(getBoolean(settings.getAttribute(IS_VCS_FILTER_ENABLED)));
      state.setShowDependencies(getBoolean(settings.getAttribute(IS_SHOW_DEPENDENCIES)));
      state.setLayout(UmlLayout.fromString(settings.getAttributeValue(LAYOUT)));
      state.setZoom(getDouble(settings.getAttribute(ZOOM), 1.0));
      Point p = new Point();
      p.setLocation(getDouble(settings.getAttribute(X), 0.0), getDouble(settings.getAttribute(Y), 0.0));
      state.setCenter(p);
    }
    Element selectedNodes = root.getChild(SELECTED_NODES);
    if (selectedNodes != null) {
      List<String> selNodes = new ArrayList<String>();
      for (Object child : selectedNodes.getChildren()) {
        if (child instanceof Element) {
          String fqn = ((Element)child).getText();
          if (fqn != null) selNodes.add(fqn);
        }
      }
      state.setSelectedNodes(selNodes);
    }

    Element edges = root.getChild(EDGES);
    if (edges != null) {
      for (Object e : edges.getChildren()) {
        if (e instanceof Element) {
          Element edge = (Element)e;
          final String src = edge.getAttributeValue(SOURCE);
          final String trg = edge.getAttributeValue(TARGET);
          if (src != null && trg != null) {
            final List<Pair<Double, Double>> points = new ArrayList<Pair<Double, Double>>();
            for (Object point : edge.getChildren()) {
              if (point instanceof Element) {
                try {
                  final double x = Double.parseDouble(((Element)point).getAttributeValue(X));
                  final double y = Double.parseDouble(((Element)point).getAttributeValue(Y));
                  points.add(new Pair<Double, Double>(x,y));
                } catch (Exception exc) {//
                }
              }
            }
            if (points.size() > 1) {
              state.setEdgesCoord(src, trg, points);
            }
          }
        }
      }
    }

    Element categories = root.getChild(CATEGORIES);
    if (categories != null && provider != null) {
      final List<String> names = new ArrayList<String>();
      for (Object child : categories.getChildren(CATEGORY)) {
        if (child instanceof Element) {
          Element node = (Element)child;
          if (node != null) {
            names.add(node.getText());
          }
        }
      }
      Set<UmlCategory> cats = new HashSet<UmlCategory>();
      for (UmlCategory category : provider.getNodeContentManager().getContentCategories()) {
        if (names.contains(category.getName())) {
          cats.add(category);
        }
      }
      state.setCategories(cats);
    }

    return state;
  }
}
