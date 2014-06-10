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

package com.intellij.uml.core.actions.visibility;

import com.intellij.uml.UmlEdge;
import com.intellij.uml.UmlNode;
import com.intellij.uml.UmlVisibilityManager;
import com.intellij.uml.VisibilityLevel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.uml.Utils;
import com.intellij.uml.utils.UmlIcons;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class ChangeVisibilityLevelAction extends AnAction {
  private final UmlVisibilityManager myVisibilityManager;
  private final VisibilityLevel myLevel;
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;

  public ChangeVisibilityLevelAction(UmlVisibilityManager visibilityManager, VisibilityLevel level, GraphBuilder<UmlNode, UmlEdge> builder) {
    super(level.getDisplayName(), "", getActionIcon(visibilityManager, level));
    myVisibilityManager = visibilityManager;
    myLevel = level;
    myBuilder = builder;
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setIcon(getActionIcon(myVisibilityManager, myLevel));
  }

  private static Icon getActionIcon(UmlVisibilityManager mgr, VisibilityLevel level) {
    return level == mgr.getCurrentVisibilityLevel() ? UmlIcons.SELECTED : UmlIcons.DESELECTED;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    myVisibilityManager.setCurrentVisibilityLevel(myLevel);
    Utils.updateGraph(myBuilder);
  }
}
