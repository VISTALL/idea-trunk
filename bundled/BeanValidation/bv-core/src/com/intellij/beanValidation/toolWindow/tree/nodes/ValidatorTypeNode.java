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

import com.intellij.beanValidation.BVIcons;
import com.intellij.beanValidation.utils.BVUtils;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class ValidatorTypeNode extends PsiMemberSimpleNode<PsiClass> {
  protected ValidatorTypeNode(@NotNull Module module, @NotNull PsiClass validatorClass, SimpleNode parent) {
    super(parent, validatorClass);
    setPlainText(validatorClass.getName());
    setUniformIcon(BVUtils.isInLibrary(validatorClass) ? BVIcons.LIBRARY_CONSTRAINT_VALIDATION_ICON : BVIcons.CONSTRAINT_VALIDATOR_TYPE);
  }

  public SimpleNode[] getChildren() {
    return NO_CHILDREN;
  }
}
