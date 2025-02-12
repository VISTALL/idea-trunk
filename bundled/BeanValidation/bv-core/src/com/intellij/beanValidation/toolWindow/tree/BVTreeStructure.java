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

package com.intellij.beanValidation.toolWindow.tree;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.beanValidation.toolWindow.tree.nodes.BVTreeRootNode;
import com.intellij.beanValidation.toolWindow.tree.nodes.BVNodesConfig;

/**
 * @author Konstantin Bulenkov
 */
public class BVTreeStructure extends SimpleTreeStructure {
  private final SimpleNode myRoot;
  private BVNodesConfig myNodesConfig;

  public BVTreeStructure(Project project, BVNodesConfig nodesConfig) {
    myNodesConfig = nodesConfig;
    myRoot = new BVTreeRootNode(project, this);

  }

  public Object getRootElement() {
    return myRoot;
  }

  public BVNodesConfig getNodesConfig() {
    return myNodesConfig;
  }
}
