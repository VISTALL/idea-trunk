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

package com.intellij.spring.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.libraries.JarVersionDetectionUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.ui.EnumComboBoxModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Dmitry Avdeev
 */
public class SpringFeaturesEditor extends FacetEditorTab {

  private JComboBox myVersionComboBox;
  private JPanel myMainPanel;
  private final FacetLibrariesValidator myValidator;

  public SpringFeaturesEditor(final FacetEditorContext editorContext,
                              final FacetLibrariesValidator validator) {

    myValidator = validator;
    myVersionComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final SpringVersion version = getSelectedVersion();
        if (version != null) {
          validator.setRequiredLibraries(getRequiredLibraries());
          validator.setDescription(new FacetLibrariesValidatorDescription("spring-" + version.getName()));
        }
      }
    });
    final Module module = editorContext.getModule();
    final String version = JarVersionDetectionUtil.detectJarVersion(SpringConstants.SPRING_VERSION_CLASS, module);
    if (version != null) {
      myVersionComboBox.setModel(new DefaultComboBoxModel(new String[] {version}));
      myVersionComboBox.getModel().setSelectedItem(version);
      myVersionComboBox.setEnabled(false);
      return;
    }
    myVersionComboBox.setModel(new EnumComboBoxModel<SpringVersion>(SpringVersion.class));
    myVersionComboBox.getModel().setSelectedItem(SpringVersion.Spring2_0);
  }

  @Nullable
  private SpringVersion getSelectedVersion() {
    final Object version = myVersionComboBox.getModel().getSelectedItem();
    return version instanceof SpringVersion ? (SpringVersion)version : null;
  }

  @Nullable
  private LibraryInfo[] getRequiredLibraries() {
    final SpringVersion version = getSelectedVersion();
    return version == null ? null : version.getJars();
  }

  public void onFacetInitialized(@NotNull final Facet facet) {
    myValidator.onFacetInitialized(facet);
  }

  @Nls
  public String getDisplayName() {
    return SpringBundle.message("config.features.display.name");
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
