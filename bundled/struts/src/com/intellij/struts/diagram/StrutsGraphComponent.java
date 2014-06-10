/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package com.intellij.struts.diagram;

import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.components.BasicGraphComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomEventAdapter;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.ElementChangedEvent;

/**
 * @author Dmitry Avdeev
 */
public class StrutsGraphComponent extends BasicGraphComponent<StrutsObject, StrutsObject> {

  public StrutsGraphComponent(final Project project, final GraphBuilder<StrutsObject, StrutsObject> builder) {

    super(builder);

    DomManager.getDomManager(project).addDomEventListener(new DomEventAdapter() {
      public void elementChanged(final ElementChangedEvent event) {
        if (getComponent().isShowing()) {
          builder.updateGraph();
        }
      }
    }, this);
  }

  @Override
  protected DefaultActionGroup createToolbar(final GraphBuilder<StrutsObject, StrutsObject> builder) {
    final DefaultActionGroup group = super.createToolbar(builder);
    final DefaultActionGroup action = new DefaultActionGroup();
    action.add(new ContextHelpAction("reference.struts.webflow"));
    group.add(action);
    return group;
  }
}
