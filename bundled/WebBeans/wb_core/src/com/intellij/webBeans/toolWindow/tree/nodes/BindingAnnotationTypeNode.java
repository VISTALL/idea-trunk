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

package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class BindingAnnotationTypeNode extends AnnotationTypeNode {

  public BindingAnnotationTypeNode(@NotNull Module module, @NotNull PsiClass annoClass, SimpleNode parent) {
    super(module, annoClass, parent);
  }

  @Override
  public SimpleNode[] getChildren() {
    Collection<PsiMemberSimpleNode> children = createSortedList();

    for (PsiClass psiClass : findAnnotatedClasses()) {
       children.add(new AnnotatedMembersNode(myModule, psiClass, this));
    }

    for (PsiMethod psiMethod : findAnnotatedMethods()) {
       children.add(new AnnotatedMembersNode(myModule, psiMethod, this));
    }

    return children.toArray(new SimpleNode[children.size()]);
  }


}
