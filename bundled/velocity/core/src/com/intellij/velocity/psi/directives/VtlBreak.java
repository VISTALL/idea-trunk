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

package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.psi.VtlCompositeElement;
import com.intellij.velocity.psi.VtlElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlBreak extends VtlCompositeElement {
    public VtlBreak(@NotNull final ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public PsiReference getReference() {
        final ASTNode foreachNode = findForeachNode();
        if (foreachNode == null) {
            return null;
        }
        return new PsiReferenceBase<PsiElement>(this) {
            public PsiElement resolve() {
                return foreachNode.getLastChildNode().getPsi();
            }

            public Object[] getVariants() {
                return EMPTY_ARRAY;
            }
        };
    }

    @NotNull
    public PsiReference[] getReferences() {
        final PsiReference ref = getReference();
        return ref == null ? PsiReference.EMPTY_ARRAY : new PsiReference[]{ref};
    }

    @Nullable
    public ASTNode findForeachNode() {
        return TreeUtil.findParent(getNode(), VtlElementTypes.DIRECTIVE_FOREACH);
    }

    public static class Manipulator extends AbstractElementManipulator<VtlBreak> {
        public VtlBreak handleContentChange(final VtlBreak element, final TextRange range, final String newContent)
                throws IncorrectOperationException {
            return element;
        }

        public TextRange getRangeInElement(final VtlBreak element) {
            return new TextRange(0, VtlElementTypes.SHARP_BREAK.toString().length());
        }
    }
}
