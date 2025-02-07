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
import org.intellij.idea.lang.javascript.psiutil.ErrorUtil;
import org.intellij.idea.lang.javascript.psiutil.JSElementFactory;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.javascript.psi.JSIfStatement;
import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

public class JSSplitElseIfIntention extends JSIntention {
    @NotNull
    public JSElementPredicate getElementPredicate() {
        return new SplitElseIfPredicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final JSIfStatement parentStatement = (JSIfStatement) element.getParent();

        assert (parentStatement != null);

        final JSStatement elseBranch   = parentStatement.getElse();
        final String      newStatement = '{' + elseBranch.getText() + '}';

        JSElementFactory.replaceStatement(elseBranch, newStatement);
    }

    private static class SplitElseIfPredicate implements JSElementPredicate {
        public boolean satisfiedBy(@NotNull PsiElement element) {
            final PsiElement parent = element.getParent();

            if (!(parent instanceof JSIfStatement)) {
                return false;
            }

            final JSIfStatement ifStatement = (JSIfStatement) parent;

            if (ErrorUtil.containsError(ifStatement)) {
                return false;
            }
            final JSStatement thenBranch = ifStatement.getThen();
            final JSStatement elseBranch = ifStatement.getElse();

            return (thenBranch != null && elseBranch != null &&
                    elseBranch instanceof JSIfStatement);
        }
    }
}