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

package com.intellij.beanValidation.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.facet.impl.ui.libraries.versions.VersionsComponent;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.beanValidation.BeanValidationApplicationComponent;
import com.intellij.beanValidation.resources.BVBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class BeanValidationFeaturesEditor extends FacetEditorTab {

  private JPanel myMainPanel;
  private JPanel myVersionsPanel;
  private FacetLibrariesValidator myValidator;
  private VersionsComponent myComponent;

  public BeanValidationFeaturesEditor(final FacetEditorContext editorContext, final FacetLibrariesValidator validator) {
    myValidator = validator;

    myComponent = new VersionsComponent(editorContext.getModule(), validator) {
      protected String getFacetDetectionClass(@NotNull String currentRI) {
        return getDetectionClass(currentRI);
      }

      @NotNull
      protected Map<LibraryVersionInfo, List<LibraryInfo>> getLibraries() {
        return BeanValidationApplicationComponent.getInstance().getLibraries();
      }
    };

    myVersionsPanel.add(myComponent.getJComponent(), BorderLayout.CENTER);
  }


  @Nullable
  public LibraryVersionInfo getCurrentLibraryVersionInfo() {
    return myComponent.getCurrentLibraryVersionInfo();
  }

  @Nullable
  private static String getDetectionClass(String currentRI) {
    for (BeanValidation_RI ri : BeanValidation_RI.values()) {
      if (currentRI.equals(ri.getName())) {
        return ri.getFacetDetectionClass();
      }
    }
    return null;
  }

  public void onFacetInitialized(@NotNull final Facet facet) {
    myValidator.onFacetInitialized(facet);
  }

  @Nls
  public String getDisplayName() {
    return BVBundle.message("facet.editor.name");
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  public boolean isModified() {
    return myValidator.isLibrariesAdded();
  }

  public void apply() throws ConfigurationException {
  }

  public void reset() {
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {

  }
}
