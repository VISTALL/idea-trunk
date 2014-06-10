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

package com.intellij.uml.presentation;

import com.intellij.openapi.graph.base.Graph;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.uml.settings.UmlLayout;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin Bulenkov
 */
public class UmlDiagramPresentationImpl implements UmlDiagramPresentation {
  private final UmlClassDiagramPresentationModel model;
  private boolean showCamel = false;
  private boolean showFields = false;
  private boolean showConstructors = false;
  private boolean showMethods = false;
  private String myHighlightedPackage;
  private boolean myColorMgrEnabled = true;
  private boolean showDependencies = false;
  private boolean showInnerClasses = false;
  private boolean enableEdgeCreation = false;
  private VisibilityLevel visLevel = VisibilityLevel.PRIVATE;
  private boolean showProperties = false;
  private boolean vcsFilterEnabled = false;
  private UmlLayout layout = UmlLayout.HIERARCHIC_GROUP;
  private boolean fitContentAfterLayout = false;

  public UmlDiagramPresentationImpl(UmlClassDiagramPresentationModel m, UmlDiagramPresentation presentation) {
    model = m;
    if (presentation != null) {
      fillFields(presentation);
    }
  }

  private void fillFields(UmlDiagramPresentation presentation) {
    showCamel = presentation.isCamel();
    showFields = presentation.isFieldsVisible();
    showConstructors = presentation.isConstructorsVisible();
    showMethods = presentation.isMethodsVisible();
    myHighlightedPackage = presentation.getHighlightedPackage();
    myColorMgrEnabled = presentation.isColorManagerEnabled();
    enableEdgeCreation = presentation.isEdgeCreationMode();
    showInnerClasses = presentation.isShowInnerClasses();
    showDependencies = presentation.isShowDependencies();
    showProperties = presentation.isPropertiesVisible();
    visLevel = presentation.getVisibilityLevel();
    vcsFilterEnabled = presentation.isVcsFilterEnabled();
    layout = presentation.getLayout();
    fitContentAfterLayout = presentation.isFitContentAfterLayout();
  }

  public boolean isCamel() {
    return showCamel;
  }

  public void setCamel(final boolean camel) {
    if (camel != showCamel) {
      showCamel = camel;
      model.update();
    }
  }

  public boolean isFieldsVisible() {
    return showFields;
  }

  public void setFieldsVisible(final boolean visible) {
    if (visible != showFields) {
      showFields = visible;
      model.update();
    }
  }

  public boolean isConstructorsVisible() {
    return showConstructors;
  }

  public void setConstructorVisible(final boolean visible) {
    if (visible != showConstructors) {
      showConstructors = visible;
      model.update();
    }
  }

  public boolean isMethodsVisible() {
    return showMethods;
  }

  public void setMethodsVisible(final boolean visible) {
    if (visible != showMethods) {
      showMethods = visible;
      model.update();
    }
  }

  public void setHighlightedPackage(String packageName) {
    if (! UmlUtils.isEqual(packageName, myHighlightedPackage)) {
      myHighlightedPackage = packageName;
      model.repaint();
    }
  }

  @Nullable
  public String getHighlightedPackage() {
    return myHighlightedPackage;
  }

  public boolean isColorManagerEnabled() {
    return myColorMgrEnabled;
  }

  public void setColorManagerEnabled(final boolean enabled) {
    if (enabled != myColorMgrEnabled) {
      myColorMgrEnabled = enabled;            
    }
  }

  public boolean isShowDependencies() {
    return showDependencies;
  }

  public void setShowDependencies(final boolean show) {
    if (showDependencies != show) {
      showDependencies = show;
      UmlUtils.getDataModel(model.getGraphBuilder()).showDependencies(show);
      model.update();
    }
  }

  public boolean isShowInnerClasses() {
    return showInnerClasses;
  }

  public void setShowInnerClasses(final boolean visible) {
    if (showInnerClasses != visible) {
      showInnerClasses = visible;
      UmlUtils.getDataModel(model.getGraphBuilder()).setUseInnerClasses(showInnerClasses);
      model.update();
    }
  }

  public boolean isEdgeCreationMode() {
    return enableEdgeCreation;
  }

  public void setEdgeCreationMode(final boolean enable) {
    if (enableEdgeCreation != enable) {
      enableEdgeCreation = enable;
      if (model.getEditMode() != null) {
        model.getEditMode().allowEdgeCreation(enable);
      }
    }
  }

  public void setVisibilityLevel(@NotNull VisibilityLevel level) {
    if (visLevel != level) {
      visLevel = level;
      model.update();
    }
  }
  
  public @NotNull VisibilityLevel getVisibilityLevel() {
    return visLevel;
  }

  public void setPropertiesVisible(boolean visible) {
    if (showProperties != visible) {
      showProperties = visible;
      model.update();
    }
  }

  public boolean isPropertiesVisible() {
    return showProperties;
  }

  public boolean isVcsFilterEnabled() {
    return vcsFilterEnabled;
  }

  public void setVcsFilterEnabled(boolean enabled) {
    if (vcsFilterEnabled != enabled) {
      vcsFilterEnabled = enabled;
      model.update();
    }
  }

  public UmlLayout getLayout() {
    return layout;
  }

  public void setLayout(UmlLayout layout) {
    this.layout = layout;
  }

  public boolean isFitContentAfterLayout() {
    return fitContentAfterLayout;
  }

  public void setFitContentAfterLayout(boolean enabled) {
    if (fitContentAfterLayout != enabled) {
      final Project project = model.getGraphBuilder().getProject();
      final Graph graph = model.getGraph();
      GraphSettingsProvider.getInstance(project).getSettings(graph).setFitContentAfterLayout(enabled);
    }
    fitContentAfterLayout = enabled;
  }
}
