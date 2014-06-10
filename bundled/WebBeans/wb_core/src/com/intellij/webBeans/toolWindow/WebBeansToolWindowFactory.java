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

package com.intellij.webBeans.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.WebBeansProjectComponent;
import com.intellij.webBeans.resources.WebBeansBundle;

import javax.swing.*;
import java.awt.*;


public class WebBeansToolWindowFactory implements ToolWindowFactory {

  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    toolWindow.setIcon(WebBeansIcons.WEB_BEANS_ICON);
    toolWindow.setAvailable(true, null);
    toolWindow.setToHideOnEmptyContent(true);
    toolWindow.setTitle(WebBeansBundle.message("tool.window.name"));

    WebBeansView view = WebBeansProjectComponent.getInstance(project).getWebBeansView();

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
}