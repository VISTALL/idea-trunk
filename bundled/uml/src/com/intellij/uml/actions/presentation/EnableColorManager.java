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

package com.intellij.uml.actions.presentation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.uml.actions.UmlAction;

/**
 * @author Konstantin Bulenkov
 */
public class EnableColorManager extends UmlAction {
  public void actionPerformed(AnActionEvent e) {
    final UmlDiagramPresentation presentation = getPresentation(e);
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    if (presentation == null || builder == null) return;

    presentation.setColorManagerEnabled(!presentation.isColorManagerEnabled());
    builder.updateView();
  }

  @Override
  public boolean isSelected(AnActionEvent e, GraphBuilder<UmlNode, UmlEdge> b) {
    return UmlUtils.getPresentation(b).isColorManagerEnabled();
  }

  @Override
  public boolean isEnabled(AnActionEvent e, GraphBuilder<UmlNode, UmlEdge> b) {
    return !UmlUtils.getPresentation(b).isVcsFilterEnabled();
  }
}
