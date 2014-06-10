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

package com.intellij.seam.structure;

import com.intellij.facet.ProjectWideFacetAdapter;
import com.intellij.facet.ProjectWideFacetListenersRegistry;
import com.intellij.javaee.JavaeeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.seam.facet.SeamFacet;
import com.intellij.seam.facet.SeamFacetType;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Gregory.Shrago
 */
public class SeamToolWindowFactory implements ToolWindowFactory, Condition<Project> {

  public static final String TOOL_WINDOW_ID = "Seam";

  public boolean value(Project project) {
    return !SeamCommonUtils.getAllSeamFacets(project).isEmpty();
  }

  public void createToolWindowContent(final Project project, final ToolWindow toolWindow) {
    toolWindow.setIcon(SeamFacetType.INSTANCE.getIcon());
    toolWindow.setAvailable(true, null);
    toolWindow.setToHideOnEmptyContent(true);
    toolWindow.setTitle(SeamFacetType.INSTANCE.getPresentableName());

    final SeamView view = new SeamView(project);

    final JPanel p = new JPanel(new BorderLayout());
    p.add(view.getJComponent(), BorderLayout.CENTER);

    final ContentManager contentManager = toolWindow.getContentManager();
    final Content content = contentManager.getFactory().createContent(p, null, false);
    content.setDisposer(view);
    content.setCloseable(false);

    content.setPreferredFocusableComponent(view.getJComponent());
    contentManager.addContent(content);

    contentManager.setSelectedContent(content, true);
  }

  public void configureToolWindow(final Project project) {
    ProjectWideFacetListenersRegistry.getInstance(project)
      .registerListener(SeamFacet.FACET_TYPE_ID, new ProjectWideFacetAdapter<SeamFacet>() {

        @Override
        public void firstFacetAdded() {
          JavaeeUtil.activateFrameworkToolWindow(project, TOOL_WINDOW_ID, SeamToolWindowFactory.this);
        }

        public void allFacetsRemoved() {
          JavaeeUtil.deactivateFrameworkToolWindow(project, TOOL_WINDOW_ID);
        }
      });
  }

  @Nullable
  public static SeamView getSeamView(final Project project) {
    final ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
    final Content content = window == null ? null : window.getContentManager().getContent(0);
    return content == null ? null : (SeamView)content.getDisposer();
  }


}