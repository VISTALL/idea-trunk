package com.intellij.webBeans.toolWindow.tree;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.webBeans.toolWindow.tree.nodes.WebBeansTreeRootNode;
import com.intellij.webBeans.toolWindow.tree.nodes.WebBeansNodesConfig;

public class WebBeansTreeStructure extends SimpleTreeStructure {
  private final SimpleNode myRoot;
  private WebBeansNodesConfig myNodesConfig;

  public WebBeansTreeStructure(Project project, WebBeansNodesConfig nodesConfig) {
    myNodesConfig = nodesConfig;
    myRoot = new WebBeansTreeRootNode(project, this);

  }

  public Object getRootElement() {
    return myRoot;
  }

  public WebBeansNodesConfig getNodesConfig() {
    return myNodesConfig;
  }
}
