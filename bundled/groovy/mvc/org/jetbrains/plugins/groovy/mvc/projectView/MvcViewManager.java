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

package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ProjectTopics;
import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.plugins.grails.projectView.GrailsToolWindowFactory;
import org.jetbrains.plugins.groovy.griffon.GriffonToolWindowFactory;
import org.jetbrains.plugins.groovy.mvc.MvcModuleStructureUtil;

/**
 * @author peter
 */
public class MvcViewManager extends AbstractProjectComponent {
  private static final MvcToolWindowDescriptor[] ourDescriptors = {new GrailsToolWindowFactory(), new GriffonToolWindowFactory()};

  protected MvcViewManager(Project project) {
    super(project);
  }

  public void initComponent() {
    final MessageBusConnection connection = myProject.getMessageBus().connect();
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(ModuleRootEvent event) {
        updateProjectViewVisibility();
      }
    });

  }

  @Override
  public void projectOpened() {
    super.projectOpened();
    StartupManager.getInstance(myProject).registerPostStartupActivity(new DumbAwareRunnable() {
      public void run() {
        updateProjectViewVisibility();
      }
    });
  }

  private void updateProjectViewVisibility() {
    if (!StartupManagerEx.getInstanceEx(myProject).postStartupActivityPassed()) {
      return;
    }

    for (final MvcToolWindowDescriptor descriptor : ourDescriptors) {
      final String id = descriptor.getToolWindowId();
      final boolean shouldShow = MvcModuleStructureUtil.hasModulesWithSupport(myProject, descriptor.getFramework());
      final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);

      ToolWindow toolWindow = toolWindowManager.getToolWindow(id);
      if (shouldShow && toolWindow == null) {
        toolWindow = toolWindowManager.registerToolWindow(id, true, ToolWindowAnchor.LEFT, myProject, true);
        descriptor.createToolWindowContent(myProject, toolWindow);
      }
      else if (!shouldShow && toolWindow != null) {
        toolWindowManager.unregisterToolWindow(id);
        Disposer.dispose(toolWindow.getContentManager());
      }
    }
  }

}
