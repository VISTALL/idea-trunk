/*
 * Copyright 2000-2008 JetBrains s.r.o.
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
package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlExpression;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlSet extends VtlAssignment {

    public VtlSet(final ASTNode node) {
        super(node, "set", false);
    }

    @Override
    @Nullable
    public PsiType getAssignedVariableElementType() {
        final VtlReferenceExpression element = getAssignedVariableElement();
        if(element == null) {
            return null;
        }
        PsiElement wouldBeRhsExpression = element.getNextSibling();
        while (wouldBeRhsExpression != null) {
            if (wouldBeRhsExpression instanceof VtlExpression) {
                return PsiUtil.getBoxedType(((VtlExpression) wouldBeRhsExpression).getPsiType(), this);
            }
            wouldBeRhsExpression = wouldBeRhsExpression.getNextSibling();
        }
        return null;
    }
}