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

package com.intellij.uml.components;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.dnd.ProjectViewDnDHelper;
import com.intellij.openapi.util.Disposer;
import com.intellij.uml.Utils;
import com.intellij.uml.dnd.UmlDnDSupport;
import com.intellij.uml.dnd.UmlClassDiagrammDnDSupport;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.dnd.DropTarget;

/**
 * @author Konstantin Bulenkov
 */
public class UmlGraphComponent implements Disposable {
  private final JComponent myComponent;
  private final GraphBuilder myBuilder;

  public UmlGraphComponent(@NotNull GraphBuilder builder, ActionToolbar toolbar) {
    myBuilder = builder;
    myComponent = new JPanel(new BorderLayout());
    JComponent jToolbar = toolbar.getComponent();
    jToolbar.setBackground(Color.WHITE);
    jToolbar.setBorder(new AbstractBorder() {
      @Override
      public Insets getBorderInsets(final Component c) {
        return new Insets(0,0,1,0);
      }

      @Override
      public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
        Color color = g.getColor();
        g.setColor(Color.gray);
        g.drawLine(0,height-1,width,height-1);
        g.setColor(color);
      }

      @Override
      public Insets getBorderInsets(final Component c, final Insets insets) {
        insets.left = insets.right = insets.top = 0;
        insets.bottom = 1;
        return insets;
      }
    });
    myComponent.add(jToolbar, BorderLayout.NORTH);
    myComponent.add(builder.getView().getComponent(), BorderLayout.CENTER);
    Disposer.register(this, builder);
    builder.initialize();
    addDnDSupport();    
  }

  public static ActionToolbar createToolbarPanel(DefaultActionGroup actionsGroup) {
    DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(actionsGroup);
    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
  }

  private void addDnDSupport() {
    if (Utils.isNewUml(myBuilder)) {
      new DropTarget(myBuilder.getView().getCanvasComponent(), new UmlDnDSupport(myBuilder));
    } else {
    ProjectViewDnDHelper.getInstance(myBuilder.getProject())
        .addProjectViewDnDSupport(myBuilder, new UmlClassDiagrammDnDSupport(myBuilder));
    }
  }

  public void dispose() {}

  @NotNull
  public JComponent getComponent() {
    return myComponent;
  }
}
