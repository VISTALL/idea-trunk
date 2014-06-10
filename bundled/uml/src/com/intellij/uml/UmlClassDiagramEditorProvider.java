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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.uml.model.UmlRelationship;
import com.intellij.uml.presentation.VisibilityLevel;
import com.intellij.uml.settings.UmlLayout;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.uml.utils.VcsUtils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * @author Konstantin Bulenkov
 */
public class UmlClassDiagramEditorProvider implements FileEditorProvider {
  @NonNls private static final String CLASSES = "classes";
  @NonNls private static final String CLASS = "class";
  @NonNls private static final String FQN = "fqn";
  @NonNls private static final String PACKAGES = "packages";
  @NonNls private static final String PACKAGE = "package";
  @NonNls private static final String SETTINGS = "settings";
  @NonNls private static final String EDGES = "edges";
  @NonNls private static final String SELECTED_NODES = "SelectedNodes";
  @NonNls private static final String IS_CAMEL = "isCamel";
  @NonNls private static final String IS_COLOR_MANAGER_ENABLED = "isColorManagerEnabled";
  @NonNls private static final String IS_FIELDS_VISIBLE = "isFieldsVisible";
  @NonNls private static final String IS_CONSTRUCTORS_VISIBLE = "isConstructorsVisible";
  @NonNls private static final String IS_METHODS_VISIBLE = "isMethodsVisible";
  @NonNls private static final String IS_SHOW_DEPENDENCIES = "isShowDependencies";
  @NonNls private static final String IS_SHOW_INNER_CLASSES = "isShowInnerClasses";
  @NonNls private static final String IS_VCS_FILTER_ENABLED = "isVcsFilterEnabled";
  @NonNls private static final String LAYOUT = "layout";
  @NonNls private static final String VISIBILITY_LEVEL = "defaultVisibility";
  @NonNls private static final String IS_PROPERTIES_VISIBLE = "isPropertiesVisible";
  @NonNls private static final String ZOOM = "zoom";
  @NonNls private static final String EDGE = "edge";
  @NonNls private static final String X = "x";
  @NonNls private static final String Y = "y";
  @NonNls private static final String SOURCE = "source";
  @NonNls private static final String TARGET = "target";
  @NonNls private static final String RELATIONSHIP = "relationship";
  @NonNls private static final String POINT = "point";
  @NonNls private static final String NODE = "node";

  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    if ("uml".equalsIgnoreCase(file.getExtension()) && isOldUmlFormat(file)) return true;
    return isNameAsRealFQN(project, file) || VcsUtils.isShowChangesFile(file);
  }

  private boolean isOldUmlFormat(VirtualFile file) {
    byte[] bytes = new byte[100];
    try {
      file.getInputStream().read(bytes, 0, 100);
      return new String(bytes).contains("<ClassDiagramm>");
    } catch (IOException e) {//
    }

    return false;
  }

  private static boolean isNameAsRealFQN(Project project, VirtualFile file) {
    if (!(file.getFileSystem() instanceof UmlVirtualFileSystem)) return false;

    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    return facade.findPackage(file.getName()) != null || facade.findClass(file.getName(), scope) != null;
  }

  @NotNull
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    if (file.getFileSystem() instanceof UmlVirtualFileSystem) {
      return createEditorFromPsiElement(project, file);
    } else {
      return new UmlClassDiagramFileEditor(null, false, file, project);
    }
  }

  private static FileEditor createEditorFromPsiElement(@NotNull Project project, @NotNull VirtualFile file) {
    final String packageName = file.getName();
    boolean initialized = UmlVirtualFileSystem.isInitialized(file);

    final PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
    if (psiPackage != null) {
      return new UmlClassDiagramFileEditor(psiPackage, initialized, file, project);
    }
    final PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(file.getName(), GlobalSearchScope.allScope(project));
    return new UmlClassDiagramFileEditor(psiClass, initialized, file, project);
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
    return "UmlClassDiagramEditorProvider";
  }

  @NotNull
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }


  public static void saveUmlState(@NotNull FileEditorState editorState, @NotNull Element root) {
    if (editorState instanceof UmlDiagramState) {
      UmlDiagramState state = (UmlDiagramState)editorState;
      Element classes = new Element(CLASSES);
      for (String fqn : state.classes) {
        classes.addContent(new SimpleElement(CLASS,
                                             FQN, fqn,
                                             X, state.getNodeX(fqn),
                                             Y, state.getNodeY(fqn)));
      }
      root.addContent(classes);

      Element packages = new Element(PACKAGES);
      for (String fqn : state.packages) {
        packages.addContent(new SimpleElement(PACKAGE,
                                              FQN, fqn,
                                              X, state.getNodeX(fqn),
                                              Y, state.getNodeY(fqn)));
      }
      root.addContent(packages);

      Element edges = new Element(EDGES);
      for (UmlDiagramState.EdgeInfo edgeInfo : state.getEdgesCoord()) {
        final SimpleElement edge = new SimpleElement(EDGE,
                                                     SOURCE, edgeInfo.getSrc(),
                                                     TARGET, edgeInfo.getTrg(),
                                                     RELATIONSHIP, edgeInfo.getRelationship().toString());
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
        IS_FIELDS_VISIBLE, String.valueOf(state.isFieldsVisible()),
        IS_CONSTRUCTORS_VISIBLE, String.valueOf(state.isConstructorsVisible()),
        IS_METHODS_VISIBLE, String.valueOf(state.isMethodsVisible()),
        IS_SHOW_DEPENDENCIES, String.valueOf(state.isShowDependencies()),
        IS_SHOW_INNER_CLASSES, String.valueOf(state.isShowInnerClasses()),
        IS_VCS_FILTER_ENABLED, String.valueOf(state.isVcsFilterEnabled()),
        VISIBILITY_LEVEL, state.getVisibilityLevel().toString().toLowerCase(),
        LAYOUT, state.getLayout().getPresentableName(),
        IS_PROPERTIES_VISIBLE, String.valueOf(state.isPropertiesVisible()),
        ZOOM, String.valueOf(state.getZoom()),
        X, String.valueOf(state.getCenter().getX()),
        Y, String.valueOf(state.getCenter().getY()));
      root.addContent(settings);
      Element selectedNodes = new Element(SELECTED_NODES);
      for (String fqn : state.getSelectedNodes()) {
        selectedNodes.addContent(new SimpleElement(NODE, FQN, fqn));
      }
      root.addContent(selectedNodes);
    }
  }

  @NotNull
  public static UmlDiagramState readUmlState(@NotNull Element sourceElement) {
    UmlDiagramState state = new UmlDiagramState();

    Element classes = sourceElement.getChild(CLASSES);
    if (classes != null) {
      for (Object e : classes.getChildren()) {
        if (e instanceof Element) {
          String fqn = ((Element)e).getAttributeValue(FQN);
          if (fqn != null) {
            state.classes.add(fqn);
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

    Element packages = sourceElement.getChild(PACKAGES);
    if (packages != null) {
      for (Object e : packages.getChildren()) {
        if (e instanceof Element) {
          String fqn = ((Element)e).getAttributeValue(FQN);
          if (fqn != null) {
            state.packages.add(fqn);
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

    Element edges = sourceElement.getChild(EDGES);
    if (edges != null) {
      for (Object e : edges.getChildren()) {
        if (e instanceof Element) {
          Element edge = (Element)e;
          final String src = edge.getAttributeValue(SOURCE);
          final String trg = edge.getAttributeValue(TARGET);
          final UmlRelationship relationship = UmlUtils.findRelationship(edge.getAttributeValue(RELATIONSHIP));
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
              state.setEdgesCoord(src, trg, relationship, points);
            }
          }
        }
      }
    }

    Element settings = sourceElement.getChild(SETTINGS);
    if (settings != null) {
      state.setCamel(getBoolean(settings.getAttribute(IS_CAMEL)));
      state.setColorManagerEnabled(getBoolean(settings.getAttribute(IS_COLOR_MANAGER_ENABLED)));
      state.setFieldsVisible(getBoolean(settings.getAttribute(IS_FIELDS_VISIBLE)));
      state.setConstructorVisible(getBoolean(settings.getAttribute(IS_CONSTRUCTORS_VISIBLE)));
      state.setMethodsVisible(getBoolean(settings.getAttribute(IS_METHODS_VISIBLE)));
      state.setShowInnerClasses(getBoolean(settings.getAttribute(IS_SHOW_INNER_CLASSES)));
      state.setVcsFilterEnabled(getBoolean(settings.getAttribute(IS_VCS_FILTER_ENABLED)));
      state.setShowDependencies(getBoolean(settings.getAttribute(IS_SHOW_DEPENDENCIES)));
      state.setLayout(UmlLayout.fromString(settings.getAttributeValue(LAYOUT)));
      state.setVisibilityLevel(VisibilityLevel.fromString(settings.getAttributeValue(VISIBILITY_LEVEL)));
      state.setPropertiesVisible(getBoolean(settings.getAttribute(IS_PROPERTIES_VISIBLE)));
      state.setZoom(getDouble(settings.getAttribute(ZOOM), 1.0));
      Point p = new Point();
      p.setLocation(getDouble(settings.getAttribute(X), 0.0), getDouble(settings.getAttribute(Y), 0.0));
      state.setCenter(p);
    }
    Element selectedNodes = sourceElement.getChild(SELECTED_NODES);
    if (selectedNodes != null) {
      List<String> selNodes = new ArrayList<String>();
      for (Object child : selectedNodes.getChildren()) {
        if (child instanceof Element) {
          String fqn = ((Element)child).getAttributeValue(FQN);
          if (fqn != null) selNodes.add(fqn);
        }
      }
      state.setSelectedNodes(selNodes);
    }
    return state;
  }
}
