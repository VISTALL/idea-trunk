package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.webBeans.toolWindow.tree.WebBeansTreeStructure;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WebBeansTreeRootNode extends SimpleNode {
  private Project myProject;
  private WebBeansTreeStructure myTreeStructure;

  public WebBeansTreeRootNode(@NotNull Project project, @NotNull WebBeansTreeStructure webBeansTreeStructure) {
    myProject = project;
    myTreeStructure = webBeansTreeStructure;
  }

  public SimpleNode[] getChildren() {
    List<SimpleNode> children = new ArrayList<SimpleNode>();
    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      if (WebBeansCommonUtils.isWebBeansFacetDefined(module)) {
        children.add(new WebBeansModuleNode(module, myTreeStructure, this));
      }
    }

    return children.toArray(new SimpleNode[children.size()]);
  }

  public boolean isAutoExpandNode() {
    return true;
  }
}
