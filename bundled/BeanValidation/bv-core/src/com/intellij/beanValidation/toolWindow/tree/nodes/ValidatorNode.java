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

import com.intellij.psi.PsiClass;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.beanValidation.BVIcons;

/**
 * @author Konstantin Bulenkov
 */
public class ValidatorNode extends PsiMemberSimpleNode<PsiClass> {
  private final PsiClass myValidator;

  protected ValidatorNode(SimpleNode parent, PsiClass validator) {
    super(parent, validator);
    myValidator = validator;
  }

  @Override
  protected void doUpdate() {
    super.doUpdate();
    clearColoredText();
    addColoredFragment(myValidator.getName(), myValidator.getQualifiedName(), getPlainAttributes());
    setUniformIcon(BVIcons.CONSTRAINT_VALIDATOR_TYPE);
  }

  public SimpleNode[] getChildren() {
    return NO_CHILDREN;
  }
}
