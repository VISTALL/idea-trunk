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
package org.intellij.idea.lang.javascript.intention.loop;

import org.intellij.idea.lang.javascript.intention.JSElementPredicate;
import org.intellij.idea.lang.javascript.intention.JSIntention;
import org.intellij.idea.lang.javascript.psiutil.ControlFlowUtils;
import org.intellij.idea.lang.javascript.psiutil.EquivalenceChecker;
import org.intellij.idea.lang.javascript.psiutil.ErrorUtil;
import org.intellij.idea.lang.javascript.psiutil.JSElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSForStatement;
import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

public class JSMergeParallelForLoopsIntention extends JSIntention {
    @NonNls private static final String FOR_STATEMENT_PREFIX = "for (";

    @NotNull
    public JSElementPredicate getElementPredicate() {
        return new MergeParallelForLoopsPredicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final PsiElement  nextElement = JSElementFactory.getNonWhiteSpaceSibling(element, true);

        assert (nextElement != null);

        final JSForStatement firstStatement  = (JSForStatement) element;
        final JSForStatement secondStatement = (JSForStatement) nextElement;
        final StringBuilder  statementBuffer = new StringBuilder();

        this.mergeForStatements(statementBuffer, firstStatement, secondStatement);
        JSElementFactory.replaceStatement(firstStatement, statementBuffer.toString());
        JSElementFactory.removeElement(secondStatement);
    }

    private void mergeForStatements(StringBuilder  statementBuffer,
                                    JSForStatement firstStatement,
                                    JSForStatement secondStatement) {
        final JSExpression   initialization = firstStatement.getInitialization();
        final JSVarStatement varStatement   = firstStatement.getVarDeclaration();
        final JSExpression   condition      = firstStatement.getCondition();
        final JSExpression   update         = firstStatement.getUpdate();
        final JSStatement    firstBody      = firstStatement .getBody();
        final JSStatement    secondBody     = secondStatement.getBody();

        statementBuffer.append(FOR_STATEMENT_PREFIX)
                       .append((initialization == null) ? varStatement.getText() : initialization.getText())
                       .append(';')
                       .append(condition.getText())
                       .append(';')
                       .append(update.getText())
                       .append(')');
        ControlFlowUtils.appendStatementsInSequence(statementBuffer, firstBody, secondBody);
    }

    private static class MergeParallelForLoopsPredicate implements JSElementPredicate {
        public boolean satisfiedBy(@NotNull PsiElement element) {
            if (!(element instanceof JSForStatement) || ErrorUtil.containsError(element)) {
                return false;
            }

            final PsiElement nextStatement = JSElementFactory.getNonWhiteSpaceSibling(element, true);

            if (!(nextStatement instanceof JSForStatement) ||  ErrorUtil.containsError(nextStatement)) {
                return false;
            }

            return forStatementsCanBeMerged((JSForStatement) element,
                                            (JSForStatement) nextStatement);
        }

        public static boolean forStatementsCanBeMerged(JSForStatement statement1,
                                                       JSForStatement statement2) {
            final JSExpression firstInitialization  = statement1.getInitialization();
            final JSExpression secondInitialization = statement2.getInitialization();
            if (!EquivalenceChecker.expressionsAreEquivalent(firstInitialization,
                                                             secondInitialization)) {
                return false;
            }

            final JSVarStatement firstVarStatement  = statement1.getVarDeclaration();
            final JSVarStatement secondVarStatement = statement2.getVarDeclaration();
            if (!EquivalenceChecker.statementsAreEquivalent(firstVarStatement,
                                                            secondVarStatement)) {
                return false;
            }

            final JSExpression firstCondition  = statement1.getCondition();
            final JSExpression secondCondition = statement2.getCondition();
            if (!EquivalenceChecker.expressionsAreEquivalent(firstCondition,
                                                             secondCondition)) {
                return false;
            }

            final JSExpression firstUpdate  = statement1.getUpdate();
            final JSExpression secondUpdate = statement2.getUpdate();
            if (!EquivalenceChecker.expressionsAreEquivalent(firstUpdate,
                                                             secondUpdate)) {
                return false;
            }

            final JSStatement firstBody  = statement1.getBody();
            final JSStatement secondBody = statement2.getBody();
            return (firstBody == null || secondBody == null ||
                    ControlFlowUtils.canBeMerged(firstBody, secondBody));
        }
     }
 }
