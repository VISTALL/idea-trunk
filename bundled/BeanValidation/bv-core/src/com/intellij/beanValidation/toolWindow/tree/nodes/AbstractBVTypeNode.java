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
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public abstract class AbstractBVTypeNode extends AbstractBVNode {
  private Module myModule;
  private BVNodeTypes myType;
  private String myNodeName;

  protected AbstractBVTypeNode(BVModuleNode moduleNode, @NotNull Module module, @NotNull BVNodeTypes type,
                                     String nodeName) {
    super(moduleNode);
    myModule = module;
    myType = type;
    myNodeName = nodeName;
  }

  @Override
  protected void doUpdate() {
    setPlainText(myNodeName);
  }

  public Module getModule() {
    return myModule;
  }

  public BVNodeTypes getType() {
    return myType;
  }

  @Override
  public Object[] getEqualityObjects() {
    return new Object[]{myType};
  }
}
