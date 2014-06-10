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

package com.intellij.seam.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.libraries.JarVersionDetectionUtil;
import com.intellij.seam.constants.SeamConstants;
import com.intellij.seam.resources.SeamBundle;
import com.intellij.ui.EnumComboBoxModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SeamFeaturesEditor extends FacetEditorTab {

  private JComboBox myVersionComboBox;
  private JPanel myMainPanel;
  private final FacetLibrariesValidator myValidator;

  public SeamFeaturesEditor(final FacetEditorContext editorContext, final FacetLibrariesValidator validator) {
    myValidator = validator;
    myVersionComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final SeamVersion version = getSelectedVersion();
        if (version != null) {
          myValidator.setDescription(new FacetLibrariesValidatorDescription("seam-" + version.getName()));
          myValidator.setRequiredLibraries(getRequiredLibraries());
        }
      }
    });
    final Module module = editorContext.getModule();
    final String version = JarVersionDetectionUtil.detectJarVersion(SeamConstants.SEAM_DETECTION_CLASS, module);
    if (version != null) {
      myVersionComboBox.setModel(new DefaultComboBoxModel(new String[] {version}));
      myVersionComboBox.getModel().setSelectedItem(version);
      myVersionComboBox.setEnabled(false);
      return;
    }
    myVersionComboBox.setModel(new EnumComboBoxModel<SeamVersion>(SeamVersion.class));
    myVersionComboBox.getModel().setSelectedItem(SeamVersion.SEAM_2_1_1);
  }

  @Nullable
  private SeamVersion getSelectedVersion() {
    final Object version = myVersionComboBox.getModel().getSelectedItem();
    return version instanceof SeamVersion ? (SeamVersion)version : null;
  }

  @Nullable
  private LibraryInfo[] getRequiredLibraries() {
    final SeamVersion version = getSelectedVersion();
    return version == null ? null : SeamLibrariesConfig.getLibraries(version);
  }

  public void onFacetInitialized(@NotNull final Facet facet) {
    myValidator.onFacetInitialized(facet);
  }

  @Nls
  public String getDisplayName() {
    return SeamBundle.message("facet.editor.name");
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
}
