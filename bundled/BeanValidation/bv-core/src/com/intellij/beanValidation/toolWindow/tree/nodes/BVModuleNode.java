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

package com.intellij.beanValidation.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.beanValidation.BVIcons;
import com.intellij.beanValidation.toolWindow.tree.BVTreeStructure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class BVModuleNode extends SimpleNode {
  private Module myModule;
  private BVTreeStructure myTreeStructure;

  public BVModuleNode(@NotNull Module module, BVTreeStructure treeStructure, BVTreeRootNode treeRootNode) {
    super(treeRootNode);
    myModule = module;
    myTreeStructure = treeStructure;
    setUniformIcon(BVIcons.BEAN_VALIDATION_ICON);
    setPlainText(myModule.getName());
  }

  public SimpleNode[] getChildren() {
    List<SimpleNode> children = new ArrayList<SimpleNode>();
    addIfNeeded(children, new ConstraintsNode(myModule, this));
    addIfNeeded(children, new ValidatorsNode(myModule, this));
    return children.toArray(new SimpleNode[children.size()]);
  }

  private void addIfNeeded(List<SimpleNode> children, AbstractBVTypeNode node) {
    if (myTreeStructure.getNodesConfig().isShow(node.getType())) {
      children.add(node);
    }
  }

  @Override
  public Object[] getEqualityObjects() {
    return new Object[] {myModule};
  }
}
