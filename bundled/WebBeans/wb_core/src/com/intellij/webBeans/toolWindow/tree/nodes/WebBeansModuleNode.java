package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.toolWindow.tree.WebBeansTreeStructure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WebBeansModuleNode extends SimpleNode {
  private Module myModule;
  private WebBeansTreeStructure myTreeStructure;

  public WebBeansModuleNode(@NotNull Module module, WebBeansTreeStructure treeStructure, WebBeansTreeRootNode webBeansTreeRootNode) {
    super(webBeansTreeRootNode);
    myModule = module;
    myTreeStructure = treeStructure;
    setUniformIcon(WebBeansIcons.WEB_BEANS_ICON);
    setPlainText(myModule.getName());
  }

  public SimpleNode[] getChildren() {
    List<SimpleNode> children = new ArrayList<SimpleNode>();

    addIfNeeded(children, new BindingsNode(myModule, this));
    addIfNeeded(children, new DeploymentsNode(myModule, this));
    addIfNeeded(children, new ScopeNode(myModule, this));
    addIfNeeded(children, new InterceptorNode(myModule, this));

    return children.toArray(new SimpleNode[children.size()]);
  }

  private void addIfNeeded(List<SimpleNode> children, AbstractWebBeansTypeNode node) {
    if (myTreeStructure.getNodesConfig().isShow(node.getType())) {
      children.add(node);
    }
  }

  @Override
  public Object[] getEqualityObjects() {
    return new Object[] {myModule};
  }
}
