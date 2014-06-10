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
import com.intellij.psi.PsiClass;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.beanValidation.BVIcons;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.beanValidation.utils.BVUtils;

import java.util.Collection;

/**
 * @author Konstantin Bulenkov
 */
public class ConstraintsNode extends AbstractBVTypeNode {
  public ConstraintsNode(Module module, BVModuleNode moduleNode) {
    super(moduleNode, module, BVNodeTypes.CONSTRAINTS, BVBundle.message("actions.show.constraints"));

    setUniformIcon(BVIcons.CONSTRAINT_TYPE);
  }

  public SimpleNode[] getChildren() {
    Collection<PsiClass> deploymentTypesClasses = BVUtils.getConstraintClasses(getModule());

    Collection<PsiMemberSimpleNode> nodes = createSortedList();
    for (PsiClass psiClass : deploymentTypesClasses) {
      nodes.add(new AnnotationTypeNode(getModule(), psiClass, this));
    }
    return nodes.toArray(new SimpleNode[nodes.size()]);
  }
}
