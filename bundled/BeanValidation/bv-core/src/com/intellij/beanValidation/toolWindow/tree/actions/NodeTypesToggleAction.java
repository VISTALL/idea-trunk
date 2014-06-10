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

package com.intellij.beanValidation.toolWindow.tree.actions;

import com.intellij.beanValidation.toolWindow.tree.nodes.BVNodeTypes;
import com.intellij.beanValidation.toolWindow.tree.nodes.BVNodesConfig;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class NodeTypesToggleAction extends ToggleAction {
  private AbstractTreeBuilder myBuilder;
  private BVNodesConfig myNodesConfig;
  private BVNodeTypes myType;
  private String myText;
  private Icon myIcon;

  public NodeTypesToggleAction(AbstractTreeBuilder builder, BVNodesConfig nodesConfig, BVNodeTypes type, String text, Icon icon) {
    myBuilder = builder;
    myNodesConfig = nodesConfig;
    myType = type;
    myText = text;
    myIcon = icon;
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    e.getPresentation().setIcon(myIcon);
    e.getPresentation().setText(myText);
  }

  public boolean isSelected(AnActionEvent e) {
    return myNodesConfig.isShow(myType);
  }

  public void setSelected(AnActionEvent e, boolean state) {
    if (state) {
      myNodesConfig.show(myType);
    } else {
      myNodesConfig.hide(myType);
    }

    myBuilder.updateFromRoot(); 
  }
}
