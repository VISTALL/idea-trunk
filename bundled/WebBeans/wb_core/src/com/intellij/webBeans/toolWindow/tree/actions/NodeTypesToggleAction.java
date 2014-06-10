package com.intellij.webBeans.toolWindow.tree.actions;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.webBeans.toolWindow.tree.nodes.WebBeansNodeTypes;
import com.intellij.webBeans.toolWindow.tree.nodes.WebBeansNodesConfig;

import javax.swing.*;

public class NodeTypesToggleAction extends ToggleAction {
  private AbstractTreeBuilder myBuilder;
  private WebBeansNodesConfig myNodesConfig;
  private WebBeansNodeTypes myType;
  private String myText;
  private Icon myIcon;

  public NodeTypesToggleAction(AbstractTreeBuilder builder, WebBeansNodesConfig nodesConfig, WebBeansNodeTypes type, String text, Icon icon) {
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
