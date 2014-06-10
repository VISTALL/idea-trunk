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

import com.intellij.util.containers.HashSet;

import java.util.Set;

/**
 * @author Konstantin Bulenkov
 */
public class BVNodesConfig {
  @SuppressWarnings({"SetReplaceableByEnumSet"}) private Set<BVNodeTypes> myShowNodeTypes = new HashSet<BVNodeTypes>();

  public BVNodesConfig() {
    myShowNodeTypes.add(BVNodeTypes.CONSTRAINTS);
    myShowNodeTypes.add(BVNodeTypes.VALIDATORS);
  }

  synchronized public boolean isShow(BVNodeTypes nodeType) {
    return myShowNodeTypes.contains(nodeType);
  }

  synchronized public void show(BVNodeTypes nodeType) {
    myShowNodeTypes.add(nodeType);
  }

  synchronized public void hide(BVNodeTypes nodeType) {
    myShowNodeTypes.remove(nodeType);
  }
}
