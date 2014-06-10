/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.tomcat;

import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.facet.pointers.FacetPointersManager;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.Factory;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactPointerManager;
import com.intellij.packaging.artifacts.ArtifactPointer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

public class TomcatDeploymentSettingsEditor extends SettingsEditor<DeploymentModel> {
  private JPanel myPanel;
  private JComboBox myContextPath;

  public TomcatDeploymentSettingsEditor(final CommonModel configuration, final JavaeeFacet facet) {
    super(new Factory<DeploymentModel>() {
      public TomcatModuleDeploymentModel create() {
        final FacetPointersManager manager = FacetPointersManager.getInstance(facet.getModule().getProject());
        return new TomcatModuleDeploymentModel(configuration, manager.create(facet));
      }
    });
  }

  public TomcatDeploymentSettingsEditor(final CommonModel configuration, final Artifact artifact) {
    super(new Factory<DeploymentModel>() {
      public DeploymentModel create() {
        final ArtifactPointer pointer = ArtifactPointerManager.getInstance(configuration.getProject()).create(artifact);
        return new TomcatModuleDeploymentModel(configuration, pointer);
      }
    });
  }

  public void resetEditorFrom(DeploymentModel settings) {
    TomcatModel configuration = (TomcatModel)settings.getServerModel();
    updateContextPaths(configuration);
    setSelectedContextPath(((TomcatModuleDeploymentModel)settings).CONTEXT_PATH, true);
  }

  public void applyEditorTo(DeploymentModel settings) throws ConfigurationException {
    ((TomcatModuleDeploymentModel)settings).CONTEXT_PATH = getSelectedContextPath();
  }

  @NotNull
  public JComponent createEditor() {
    return myPanel;
  }

  public void disposeEditor() {
  }

  private String getSelectedContextPath() {
    final String item = (String)myContextPath.getEditor().getItem();
    return (item != null) ? item : "";
  }

  private void setSelectedContextPath(String contextPath, boolean addIfNotFound) {
    int itemCount = myContextPath.getItemCount();
    for (int idx = 0; idx < itemCount; idx++) {
      String path = (String)myContextPath.getItemAt(idx);
      if (contextPath.equals(path)) {
        myContextPath.setSelectedIndex(idx);
        return;
      }
    }
    if (addIfNotFound) {
      myContextPath.addItem(contextPath);
      myContextPath.setSelectedItem(contextPath);
    }
  }

  private void updateContextPaths(TomcatModel configuration) {
    final String selectedContextPath = getSelectedContextPath();
    myContextPath.removeAllItems();
    try {
      Collection<String> configuredContextPaths = TomcatUtil.getConfiguredContextPaths(configuration);
      for (String path : configuredContextPaths) {
        myContextPath.addItem(path);
      }
    }
    catch (RuntimeConfigurationException e) {
    }
    setSelectedContextPath(selectedContextPath, true);
  }
}
