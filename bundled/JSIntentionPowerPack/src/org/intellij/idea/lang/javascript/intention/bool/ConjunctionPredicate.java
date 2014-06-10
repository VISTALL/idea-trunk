/*
 * Copyright 2005-2006 Olivier Descout
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.idea.lang.javascript.intention.bool;

import org.intellij.idea.lang.javascript.intention.JSElementPredicate;
import org.intellij.idea.lang.javascript.psiutil.ErrorUtil;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;

class ConjunctionPredicate implements JSElementPredicate {
    public boolean satisfiedBy(@NotNull PsiElement element) {
        if (!(element instanceof JSBinaryExpression)) {
            return false;
        }
        if (ErrorUtil.containsError(element)) {
            return false;
        }

        final JSBinaryExpression expression = (JSBinaryExpression) element;
        final IElementType       sign       = expression.getOperationSign();

        return (sign.equals(JSTokenTypes.ANDAND) || sign.equals(JSTokenTypes.OROR));
    }
}
