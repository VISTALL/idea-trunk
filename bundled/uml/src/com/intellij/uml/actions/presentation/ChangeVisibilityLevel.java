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
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.presentation.VisibilityLevel;
import com.intellij.uml.utils.UmlIcons;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.uml.actions.UmlAction;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public abstract class ChangeVisibilityLevel extends UmlAction {
  public abstract VisibilityLevel getVisibilityLevel();

  @Override
  public void update(AnActionEvent e) {
    final GraphBuilder<UmlNode,UmlEdge> builder = getBuilder(e);
    if (builder == null) {
      e.getPresentation().setEnabled(false);
      e.getPresentation().setIcon(UmlIcons.DESELECTED);
    } else {
      e.getPresentation().setEnabled(true);
      final VisibilityLevel level = UmlUtils.getPresentation(builder).getVisibilityLevel();
      final Icon icon = level == getVisibilityLevel() ? UmlIcons.SELECTED : UmlIcons.DESELECTED;
      e.getPresentation().setIcon(icon);
    }
  }

  public void actionPerformed(AnActionEvent e) {
    final GraphBuilder<UmlNode,UmlEdge> builder = getBuilder(e);
    if (builder == null) return;

    UmlUtils.getPresentation(builder).setVisibilityLevel(getVisibilityLevel());
  }

  public static class ChangeVisibilityLevelToPublic extends ChangeVisibilityLevel {
    public VisibilityLevel getVisibilityLevel() {
      return VisibilityLevel.PUBLIC;
    }
  }

  public static class ChangeVisibilityLevelToPackage extends ChangeVisibilityLevel {
    public VisibilityLevel getVisibilityLevel() {
      return VisibilityLevel.PACKAGE;
    }
  }

  public static class ChangeVisibilityLevelToProtected extends ChangeVisibilityLevel {
    public VisibilityLevel getVisibilityLevel() {
      return VisibilityLevel.PROTECTED;
    }
  }

  public static class ChangeVisibilityLevelToPrivate extends ChangeVisibilityLevel {
    public VisibilityLevel getVisibilityLevel() {
      return VisibilityLevel.PRIVATE;
    }
  }

  public static class ChangeVisibilityGroup extends DefaultActionGroup implements Toggleable {
    @Override
    public void update(AnActionEvent e) {
      final GraphBuilder<UmlNode, UmlEdge> builder = UmlAction.getBuilder(e);
      if (builder == null) {
        e.getPresentation().setEnabled(false);
        e.getPresentation().putClientProperty(SELECTED_PROPERTY, Boolean.FALSE);
      } else {
        final VisibilityLevel visibility = UmlUtils.getPresentation(builder).getVisibilityLevel();
        final Boolean selected = visibility != VisibilityLevel.PRIVATE;
        e.getPresentation().putClientProperty(SELECTED_PROPERTY, selected);
      }
    }
  }
}
