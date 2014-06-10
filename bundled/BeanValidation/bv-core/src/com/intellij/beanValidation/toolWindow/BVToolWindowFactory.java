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

package com.intellij.beanValidation.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.beanValidation.BVIcons;
import com.intellij.beanValidation.BeanValidationProjectComponent;
import com.intellij.beanValidation.resources.BVBundle;

import javax.swing.*;
import java.awt.*;


/**
 * @author Konstantin Bulenkov
 */
public class BVToolWindowFactory implements ToolWindowFactory {

  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    toolWindow.setIcon(BVIcons.BEAN_VALIDATION_ICON);
    toolWindow.setAvailable(true, null);
    toolWindow.setToHideOnEmptyContent(true);
    toolWindow.setTitle(BVBundle.message("tool.window.name"));

    BeanValidationView view = BeanValidationProjectComponent.getInstance(project).getView();

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