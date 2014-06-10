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
package org.intellij.idea.lang.javascript.intention.switchtoif;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.intellij.idea.lang.javascript.psiutil.EquivalenceChecker;
import org.intellij.idea.lang.javascript.psiutil.ExpressionUtil;
import org.intellij.idea.lang.javascript.psiutil.ParenthesesUtils;
import org.intellij.idea.lang.javascript.psiutil.SideEffectChecker;

import java.util.ArrayList;
import java.util.List;

class CaseUtil {

    private CaseUtil() {}

    private static boolean canBeCaseLabel(JSExpression expression) {
        if (expression == null) {
            return false;
        }
        return ExpressionUtil.isConstantExpression(expression);
    }

    public static boolean containsHiddenBreak(JSCaseClause caseClause) {
        for (JSStatement statement : caseClause.getStatements()) {
            if (CaseUtil.containsHiddenBreak(statement, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsHiddenBreak(List<PsiElement> elements) {
        for (final PsiElement element : elements) {
            if (element instanceof JSStatement &&
                containsHiddenBreak((JSStatement) element, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsHiddenBreak(JSStatement statement) {
        return containsHiddenBreak(statement, true);
    }

    private static boolean containsHiddenBreak(JSStatement statement,
                                               boolean     isTopLevel) {
        if (statement instanceof JSBlockStatement) {
            final JSStatement[] statements = ((JSBlockStatement) statement).getStatements();
            for (final JSStatement childStatement : statements) {
                if (containsHiddenBreak(childStatement, false)) {
                    return true;
                }
            }
        } else if (statement instanceof JSIfStatement) {
            final JSIfStatement ifStatement = (JSIfStatement) statement;

            return (containsHiddenBreak(ifStatement.getThen(), false) ||
                    containsHiddenBreak(ifStatement.getElse(), false));
        } else if (statement instanceof JSBreakStatement) {
            if (isTopLevel) {
                return false;
            }

            final String identifier = ((JSBreakStatement) statement).getLabel();

            return (identifier == null || identifier.length() == 0);
        }

        return false;
    }

    public static boolean isUsedByStatementList(JSVariable      var,
                                                List<PsiElement> elements) {
        for (PsiElement element : elements) {
            if (element instanceof JSElement &&
                isUsedByStatement(var, (JSElement) element)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUsedByStatement(JSVariable var,
                                             JSElement  statement) {
        final LocalVariableUsageVisitor visitor = new LocalVariableUsageVisitor(var);
        statement.accept(visitor);
        return visitor.isUsed();
    }

    public static String findUniqueLabel(JSStatement statement,
                                         String      baseName) {
        JSElement ancestor = statement;

        while (ancestor.getParent() != null) {
            if (ancestor instanceof PsiFile) {
                break;
            }
            ancestor = (JSElement) ancestor.getParent();
        }

        if (!checkForLabel(baseName, ancestor)) {
            return baseName;
        }

        int val = 1;

        while (true) {
            final String name = baseName + val;

            if (!checkForLabel(name, ancestor)) {
                return name;
            }
            val++;
        }
    }

    private static boolean checkForLabel(String name, JSElement ancestor) {
        final LabelSearchVisitor visitor = new LabelSearchVisitor(name);
        ancestor.accept(visitor);
        return visitor.isUsed();
    }

    public static JSExpression getCaseExpression(JSIfStatement statement) {
        final JSExpression       condition               = statement.getCondition();
        final List<JSExpression> possibleCaseExpressions = determinePossibleCaseExpressions(condition);

        if (possibleCaseExpressions != null) {
            for (final JSExpression caseExpression : possibleCaseExpressions) {
                if (!SideEffectChecker.mayHaveSideEffects(caseExpression)) {
                    JSIfStatement statementToCheck = statement;

                    while (canBeMadeIntoCase(statementToCheck.getCondition(), caseExpression)) {
                        final JSStatement elseBranch = statementToCheck.getElse();

                        if (elseBranch == null || !(elseBranch instanceof JSIfStatement)) {
                            return caseExpression;
                        }
                        statementToCheck = (JSIfStatement) elseBranch;
                    }
                }
            }
        }
        return null;
    }

    private static List<JSExpression> determinePossibleCaseExpressions(JSExpression exp) {
        final JSExpression       expToCheck = ParenthesesUtils.stripParentheses(exp);
        final List<JSExpression> out        = new ArrayList<JSExpression>(10);

        if (expToCheck instanceof JSBinaryExpression) {
            final JSBinaryExpression binaryExp = (JSBinaryExpression) expToCheck;
            final IElementType       operation = binaryExp.getOperationSign();
            final JSExpression       lhs       = binaryExp.getLOperand();
            final JSExpression       rhs       = binaryExp.getROperand();

            if (operation.equals(JSTokenTypes.OROR)) {
                return determinePossibleCaseExpressions(lhs);
            } else if (operation.equals(JSTokenTypes.EQEQ)) {
                if (canBeCaseLabel(lhs) && rhs != null) {
                    out.add(rhs);
                }
                if (canBeCaseLabel(rhs)) {
                    out.add(lhs);
                }
            }
        }
        return out;
    }

    private static boolean canBeMadeIntoCase(JSExpression exp,
                                             JSExpression caseExpression) {
        final JSExpression expToCheck = ParenthesesUtils.stripParentheses(exp);

        if (!(expToCheck instanceof JSBinaryExpression)) {
            return false;
        }

        final JSBinaryExpression binaryExp    = (JSBinaryExpression) expToCheck;
        final IElementType       operation    = binaryExp.getOperationSign();
        final JSExpression       leftOperand  = binaryExp.getLOperand();
        final JSExpression       rightOperand = binaryExp.getROperand();

        if (operation.equals(JSTokenTypes.OROR)) {
            return (canBeMadeIntoCase(leftOperand,  caseExpression) &&
                    canBeMadeIntoCase(rightOperand, caseExpression));
        } else if (operation.equals(JSTokenTypes.EQEQ)) {
            return ((canBeCaseLabel(leftOperand)  && EquivalenceChecker.expressionsAreEquivalent(caseExpression, rightOperand)) ||
                    (canBeCaseLabel(rightOperand) && EquivalenceChecker.expressionsAreEquivalent(caseExpression, leftOperand)));
        } else {
            return false;
        }
    }
}
