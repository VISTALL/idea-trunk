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
package org.intellij.idea.lang.javascript.intention.conditional;


import org.intellij.idea.lang.javascript.intention.JSElementPredicate;
import org.intellij.idea.lang.javascript.intention.JSIntention;
import org.intellij.idea.lang.javascript.psiutil.BoolUtils;
import org.intellij.idea.lang.javascript.psiutil.ErrorUtil;
import org.intellij.idea.lang.javascript.psiutil.JSElementFactory;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.javascript.psi.JSConditionalExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

public class JSFlipConditionalIntention extends JSIntention {
    @NotNull
    public JSElementPredicate getElementPredicate() {
        return new FlipConditionalPredicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final JSConditionalExpression exp            = (JSConditionalExpression) element;
        final JSExpression            condition      = exp.getCondition();
        final JSExpression            elseExpression = exp.getElse();
        final JSExpression            thenExpression = exp.getThen();

        assert (elseExpression != null);
        assert (thenExpression != null);

        final String newExpression = BoolUtils.getNegatedExpressionText(condition) + '?' +
                                     elseExpression.getText() + ':' + thenExpression.getText();

        JSElementFactory.replaceExpression(exp, newExpression);
    }

    private static class FlipConditionalPredicate implements JSElementPredicate {
        public boolean satisfiedBy(@NotNull PsiElement element) {
            if (!(element instanceof JSConditionalExpression)) {
                return false;
            }
            if (ErrorUtil.containsError(element)){
                return false;
            }

            final JSConditionalExpression condition = (JSConditionalExpression) element;

            return (condition.getCondition() != null &&
                    condition.getThen()      != null &&
                    condition.getElse()      != null);
        }
    }
}
