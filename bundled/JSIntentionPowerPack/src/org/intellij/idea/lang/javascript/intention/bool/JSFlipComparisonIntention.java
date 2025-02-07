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
import org.intellij.idea.lang.javascript.intention.JSMutablyNamedIntention;
import org.intellij.idea.lang.javascript.psiutil.ComparisonUtils;
import org.intellij.idea.lang.javascript.psiutil.JSElementFactory;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;

public class JSFlipComparisonIntention extends JSMutablyNamedIntention {
    public String getTextForElement(PsiElement element) {
        final JSBinaryExpression exp                 = (JSBinaryExpression) element;
        String                   operatorText        = null;
        String                   flippedOperatorText = null;

        if (exp != null) {
            operatorText        = ComparisonUtils.getOperatorText       (exp.getOperationSign());
            flippedOperatorText = ComparisonUtils.getFlippedOperatorText(exp.getOperationSign());
        }

        if (exp == null) {
            return this.getSuffixedDisplayName("unknown");
        } else if (operatorText.equals(flippedOperatorText)) {
            return this.getSuffixedDisplayName("equals", operatorText);
        } else {
            return this.getSuffixedDisplayName("not-equals", operatorText, flippedOperatorText);
        }
    }

    @NotNull public JSElementPredicate getElementPredicate() {
        return new ComparisonPredicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final JSBinaryExpression exp  = (JSBinaryExpression) element;
        final JSExpression       lhs  = exp.getLOperand();
        final JSExpression       rhs  = exp.getROperand();
        final IElementType       sign = exp.getOperationSign();

        assert (rhs != null);

        final String expString = rhs.getText() + ComparisonUtils.getFlippedOperatorText(sign) + lhs.getText();
        JSElementFactory.replaceExpression(exp, expString);
    }
}
