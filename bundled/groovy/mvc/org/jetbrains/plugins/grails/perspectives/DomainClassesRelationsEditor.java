/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.perspectives;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.Module;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.util.GrailsUtils;

import javax.swing.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 06.08.2007
 */
public class DomainClassesRelationsEditor extends PerspectiveFileEditor {
  private PerspectiveFileEditorComponent myComponent;
  private final VirtualFile domainDirectory;

  protected DomainClassesRelationsEditor(Project project, VirtualFile virtualFile) {
    super(project, virtualFile);

    final Module module = ModuleUtil.findModuleForFile(virtualFile, project);
    domainDirectory = GrailsUtils.findDomainClassDirectory(module);
  }

  @Nullable
  protected DomElement getSelectedDomElement() {
    return null;
  }

  protected void setSelectedDomElement(DomElement domElement) {
  }

  @NotNull
  protected JComponent createCustomComponent() {
    return getDependenciesComponent();
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return null;
  }

  @NonNls
  @NotNull
  public String getName() {
    return GrailsBundle.message("domain.classes.dependencies");
  }

  public void commit() {
  }

  public void reset() {
    myComponent.reset();
  }

  public PerspectiveFileEditorComponent getDependenciesComponent() {
    if (myComponent == null) {
      myComponent = new PerspectiveFileEditorComponent(domainDirectory, getProject());
      Disposer.register(this, myComponent);
    }

    return myComponent;
  }

  public StructureViewBuilder getStructureViewBuilder() {
    return GraphViewUtil.createStructureViewBuilder(getDependenciesComponent().getOverview());
  }
}
