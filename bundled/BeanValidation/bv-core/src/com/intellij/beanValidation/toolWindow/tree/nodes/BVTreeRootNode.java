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
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.beanValidation.toolWindow.tree.BVTreeStructure;
import com.intellij.beanValidation.utils.BVUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class BVTreeRootNode extends SimpleNode {
  private Project myProject;
  private BVTreeStructure myTreeStructure;

  public BVTreeRootNode(@NotNull Project project, @NotNull BVTreeStructure structure) {
    myProject = project;
    myTreeStructure = structure;
  }

  public SimpleNode[] getChildren() {
    List<SimpleNode> children = new ArrayList<SimpleNode>();
    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      if (BVUtils.isBeanValidationFacetDefined(module)) {
        children.add(new BVModuleNode(module, myTreeStructure, this));
      }
    }

    return children.toArray(new SimpleNode[children.size()]);
  }

  public boolean isAutoExpandNode() {
    return true;
  }
}
