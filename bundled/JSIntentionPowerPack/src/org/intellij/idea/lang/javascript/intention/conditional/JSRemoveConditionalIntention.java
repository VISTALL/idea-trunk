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
import org.intellij.idea.lang.javascript.psiutil.ParenthesesUtils;
import org.intellij.idea.lang.javascript.psiutil.JSElementFactory;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.javascript.psi.JSConditionalExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

public class JSRemoveConditionalIntention extends JSIntention {
    @NotNull
    public JSElementPredicate getElementPredicate() {
        return new RemoveConditionalPredicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final JSConditionalExpression exp            = (JSConditionalExpression) element;
        final JSExpression            condition      = exp.getCondition();
        final JSExpression            thenExpression = exp.getThen();

        assert (thenExpression != null);

        final String thenExpressionText = thenExpression.getText();
        final String newExpression;

        newExpression = (thenExpressionText.equals(BoolUtils.TRUE)
                              ? condition.getText()
                              : BoolUtils.getNegatedExpressionText(condition));
        JSElementFactory.replaceExpression(exp, newExpression);
    }

    private static class RemoveConditionalPredicate implements JSElementPredicate {
        public boolean satisfiedBy(@NotNull PsiElement element) {
            if (!(element instanceof JSConditionalExpression)) {
                return false;
            }
            if (ErrorUtil.containsError(element)) {
                return false;
            }

            final JSConditionalExpression condition      = (JSConditionalExpression) element;
            final JSExpression            thenExpression = ParenthesesUtils.stripParentheses(condition.getThen());
            final JSExpression            elseExpression = ParenthesesUtils.stripParentheses(condition.getElse());

            if (condition.getCondition() == null ||
                thenExpression           == null ||
                elseExpression           == null) {
                return false;
            }

            final String thenText = thenExpression.getText();
            final String elseText = elseExpression.getText();

            return  ((BoolUtils.TRUE.equals(elseText) && BoolUtils.FALSE.equals(thenText)) ||
                     (BoolUtils.TRUE.equals(thenText) && BoolUtils.FALSE.equals(elseText)));
        }
    }
}
