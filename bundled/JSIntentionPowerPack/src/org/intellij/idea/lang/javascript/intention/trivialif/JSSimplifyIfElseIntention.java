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
package org.intellij.idea.lang.javascript.intention.trivialif;

import org.intellij.idea.lang.javascript.intention.JSElementPredicate;
import org.intellij.idea.lang.javascript.intention.JSIntention;
import org.intellij.idea.lang.javascript.psiutil.ConditionalUtils;
import org.intellij.idea.lang.javascript.psiutil.ErrorUtil;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSIfStatement;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

public class JSSimplifyIfElseIntention extends JSIntention {
    @NotNull
    public JSElementPredicate getElementPredicate() {
        return new SimplifyIfElsePredicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final PsiElement statement = (element.getParent() instanceof JSIfStatement ? element.getParent() : element);

        ConditionalUtils.replaceAssignmentOrReturnIfSimplifiable((JSIfStatement) statement);
    }

    private static class SimplifyIfElsePredicate implements JSElementPredicate {
        public boolean satisfiedBy(@NotNull PsiElement element) {
            if (!(element instanceof JSElement)) {
                return false;
            }

            PsiElement parent = element.getParent();

            if (!(parent instanceof JSIfStatement)) {
                if (element instanceof JSIfStatement) {
                    parent = element;
                } else {
                    return false;
                }
            }
            if (ErrorUtil.containsError(parent)) {
                return false;
            }

            final JSIfStatement ifStatement = (JSIfStatement) parent;
            final JSExpression  condition   = ifStatement.getCondition();

            if (condition == null || !condition.isValid()) {
                return false;
            }

            return (ConditionalUtils.isSimplifiableAssignment        (ifStatement, false) ||
                    ConditionalUtils.isSimplifiableAssignment        (ifStatement, true)  ||
                    ConditionalUtils.isSimplifiableReturn            (ifStatement, false) ||
                    ConditionalUtils.isSimplifiableReturn            (ifStatement, true)  ||
                    ConditionalUtils.isSimplifiableImplicitReturn    (ifStatement, false) ||
                    ConditionalUtils.isSimplifiableImplicitReturn    (ifStatement, true)  ||
                    ConditionalUtils.isSimplifiableImplicitAssignment(ifStatement, false) ||
                    ConditionalUtils.isSimplifiableImplicitAssignment(ifStatement, true));

        }
    }
}
