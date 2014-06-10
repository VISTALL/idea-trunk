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

package com.intellij.beanValidation;

import com.intellij.beanValidation.facet.BeanValidationFacet;
import com.intellij.beanValidation.toolWindow.BVToolWindowFactory;
import com.intellij.beanValidation.toolWindow.BeanValidationView;
import com.intellij.facet.ProjectWideFacetAdapter;
import com.intellij.facet.ProjectWideFacetListenersRegistry;
import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationProjectComponent extends AbstractProjectComponent {
  private BeanValidationView myView;

  private static final String TOOL_WINDOW_ID = "Bean Validation";
  private Map<LibraryVersionInfo, List<LibraryInfo>> myLibraries;


  public static BeanValidationProjectComponent getInstance(@NotNull Project project) {
    return project.getComponent(BeanValidationProjectComponent.class);
  }

  public BeanValidationProjectComponent(final Project project, final ReferenceProvidersRegistry referenceProvidersRegistry) {
    super(project);
    registerReferenceProviders(referenceProvidersRegistry);
  }

  private static void registerReferenceProviders(final ReferenceProvidersRegistry referenceProvidersRegistry) {
  }

  public void projectOpened() {
    configureToolWindow();
  }

  private void configureToolWindow() {
    ProjectWideFacetListenersRegistry.getInstance(myProject)
      .registerListener(BeanValidationFacet.FACET_TYPE_ID, new ProjectWideFacetAdapter<BeanValidationFacet>() {
        @Override
        public void firstFacetAdded() {
          if (ToolWindowManager.getInstance(myProject).getToolWindow(TOOL_WINDOW_ID) == null) {
            initToolWindow();
          }
        }

        @Override
        public void allFacetsRemoved() {
          if (ToolWindowManager.getInstance(myProject).getToolWindow(TOOL_WINDOW_ID) != null) {
            deactivateToolWindow();
          }
        }
      });
  }

  private void initToolWindow() {
    ToolWindow window =
      ToolWindowManager.getInstance(myProject).registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.RIGHT, myProject);

    BVToolWindowFactory toolWindowFactory = new BVToolWindowFactory();
    toolWindowFactory.createToolWindowContent(myProject, window);
  }

  private void deactivateToolWindow() {
    ToolWindowManager.getInstance(myProject).unregisterToolWindow(TOOL_WINDOW_ID);

    Disposer.dispose(myView);
    myView =null;
  }

  public BeanValidationView getView() {
    if (myView == null) {
      myView = new BeanValidationView(myProject);
    }
    return myView;
  }
}
