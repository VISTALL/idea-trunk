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
package org.intellij.idea.lang.javascript.psiutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 */
public class JSElementFactory {
    @NonNls private static final String  DUMMY_FILE_NAME_PREFIX = "dummy.";
    private         static final Class[] WHITESPACE_CLASS       = new Class[] { PsiWhiteSpace.class };

    private JSElementFactory() {}

    public static PsiElement addElementBefore(@NotNull PsiElement element,
                                              @NotNull PsiElement newElement) {
        final ASTNode     oldElementParentNode = element.getNode();
        final PsiElement  parentNode           = element.getParent();
        final ASTNode     newElementParentNode = parentNode.getNode();
        final ASTNode     newElementNode       = newElement.getNode();

        if (newElementParentNode == null || newElementNode == null) {
            return null;
        }
        newElementParentNode.addChild(newElementNode, oldElementParentNode);
        return newElement;
    }

    public static PsiElement addElementAfter(@NotNull PsiElement element,
                                             @NotNull PsiElement newElement) {
        final ASTNode     elementNode          = element.getNode();
        final ASTNode     oldElementParentNode = ((elementNode == null) ? null : elementNode.getTreeNext());
        final PsiElement  parentNode           = element.getParent();
        final ASTNode     newElementParentNode = parentNode.getNode();
        final ASTNode     newElementNode       = newElement.getNode();

        if (newElementParentNode == null || newElementNode == null) {
            return null;
        }
        newElementParentNode.addChild(newElementNode, oldElementParentNode);

        return newElement;
    }

    public static PsiElement addElementBefore(@NotNull PsiElement element,
                                              @NonNls @NotNull String     elementText) {
        final PsiElement newElement = createDummyFile(element.getProject(), elementText).getFirstChild();

        assert (newElement != null);
        return addElementBefore(element, newElement);
    }

    public static PsiElement addElementAfter(@NotNull PsiElement element,
                                             @NotNull String     elementText) {
        final PsiElement newElement = createDummyFile(element.getProject(), elementText).getFirstChild();

        assert (newElement != null);
        return addElementAfter(element, newElement);
    }

    public static ASTNode createElementFromText(Project project, String text) {
        final PsiElement element = createDummyFile(project, text).getFirstChild();

        assert (element != null);
        return element.getNode();
    }

    @NotNull private static PsiFile createDummyFile(Project project, String text) {
      final ParserDefinition  def            =
        LanguageParserDefinitions.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage());

        assert (def != null);
      return PsiFileFactory.getInstance(project)
        .createFileFromText(DUMMY_FILE_NAME_PREFIX + JavaScriptSupportLoader.JAVASCRIPT.getDefaultExtension(), text);
    }

    public static JSStatement replaceElementWithStatement(@NotNull JSElement      element,
                                                          @NonNls @NotNull String statementText)
        throws IncorrectOperationException {
        final ASTNode    newStatementNode       = JSChangeUtil.createStatementFromText(
          element.getProject(),
          statementText,
          JSUtils.getDialect(element.getContainingFile())
        );

        final ASTNode    oldStatementParentNode = element.getNode();
        final PsiElement parentNode             = element.getParent();
        final ASTNode    newStatementParentNode = parentNode.getNode();

        if (newStatementParentNode == null || oldStatementParentNode == null || newStatementNode == null) {
            return null;
        }
        newStatementParentNode.replaceChild(oldStatementParentNode, newStatementNode);
        reformat(parentNode);

        return (JSStatement) newStatementNode.getPsi();
    }

    public static JSExpression replaceExpression(@NotNull JSExpression   expression,
                                                 @NonNls @NotNull String text)
            throws IncorrectOperationException {
        final ASTNode newExpressionNode = JSChangeUtil.createExpressionFromText(
          expression.getProject(),
          text,
          JSUtils.getDialect(expression.getContainingFile())
        );

        return replaceExpression(expression, (JSExpression) newExpressionNode.getPsi());
    }

    public static JSExpression replaceExpression(@NotNull JSExpression expression,
                                                 @NotNull JSExpression newExpression)
        throws IncorrectOperationException {
        final ASTNode    newExpressionNode = newExpression.getNode();
        final ASTNode    oldExpressionNode = expression.getNode();
        final PsiElement parentNode        = expression.getParent();
        final ASTNode    grandParentNode   = parentNode.getNode();

        if (grandParentNode == null || oldExpressionNode == null || newExpressionNode == null) {
            return null;
        }

        grandParentNode.replaceChild(oldExpressionNode, newExpressionNode);
        reformat(parentNode);

        return (JSExpression) newExpressionNode.getPsi();
    }

    public static JSStatement replaceStatement(@NotNull JSStatement    statement,
                                               @NonNls @NotNull String text)
        throws IncorrectOperationException {
        final ASTNode    newStatementNode       = JSChangeUtil.createStatementFromText(
          statement.getProject(),
          text,
          JSUtils.getDialect(statement.getContainingFile())
        );

        final ASTNode    oldStatementParentNode = statement.getNode();
        final PsiElement parentNode             = statement.getParent();
        final ASTNode    newStatementParentNode = parentNode.getNode();

        if (newStatementParentNode == null || oldStatementParentNode == null || newStatementNode == null) {
            return null;
        }

        newStatementParentNode.replaceChild(oldStatementParentNode, newStatementNode);
        reformat(parentNode);

        return (JSStatement) newStatementNode.getPsi();
    }

    public static JSStatement addStatementBefore(@NotNull JSStatement    statement,
                                                 @NonNls @NotNull String previousStatementText)
        throws IncorrectOperationException {
        final ASTNode    newStatementNode       = JSChangeUtil.createStatementFromText(
          statement.getProject(),
          previousStatementText,
          JSUtils.getDialect(statement.getContainingFile())
        );
        final ASTNode    oldStatementParentNode = statement.getNode();
        final PsiElement parentNode             = statement.getParent();
        final ASTNode    newStatementParentNode = parentNode.getNode();

        if (newStatementParentNode == null || newStatementNode == null) {
            return null;
        }

        newStatementParentNode.addChild(newStatementNode, oldStatementParentNode);
        reformat(parentNode);

        return (JSStatement) newStatementNode.getPsi();
    }

    public static JSStatement addStatementAfter(@NotNull JSStatement    statement,
                                                @NonNls @NotNull String nextStatementText)
            throws IncorrectOperationException {
        final ASTNode     newStatementNode       = JSChangeUtil.createStatementFromText(
          statement.getProject(),
          nextStatementText,
          JSUtils.getDialect(statement.getContainingFile())
        );
        final ASTNode     statementNode          = statement.getNode();
        final ASTNode     oldStatementParentNode = ((statementNode == null) ? null : statementNode.getTreeNext());
        final PsiElement  parentNode             = statement.getParent();
        final ASTNode     newStatementParentNode = parentNode.getNode();

        if (newStatementParentNode == null || newStatementNode == null) {
            return null;
        }

        newStatementParentNode.addChild(newStatementNode, oldStatementParentNode);
        reformat(parentNode);

        return (JSStatement) newStatementNode.getPsi();
    }

    public static void addRangeBefore(JSStatement[] statements, JSStatement statement)
            throws IncorrectOperationException {
        addRangeBefore(statements, 0, statements.length, statement);
    }

    public static void addRangeAfter(JSStatement[] statements, JSStatement statement)
            throws IncorrectOperationException {
        addRangeAfter(statements, 0, statements.length, statement);
    }

    @SuppressWarnings({"ForLoopWithMissingComponent"})
    public static void addRangeBefore(JSStatement[] statements, int start, int length, JSStatement statement)
            throws IncorrectOperationException {
        for (int index = start; index < length; index++) {
            addStatementBefore(statement, statements[index].getText());
        }
    }
    @SuppressWarnings({"ForLoopWithMissingComponent"})
    public static void addRangeAfter(JSStatement[] statements, int start, int length, JSStatement statement)
            throws IncorrectOperationException {
        for (int index = length; --index >= start; ) {
            addStatementAfter(statement, statements[index].getText());
        }
    }

    public static void replaceExpressionWithNegatedExpression(@NotNull JSExpression newExpression,
                                                              @NotNull JSExpression exp)
        throws IncorrectOperationException {
        JSExpression expressionToReplace = BoolUtils.findNegation(exp);
        final String replacementString;

        if (expressionToReplace == null) {
            expressionToReplace = exp;

            if (ComparisonUtils.isComparisonOperator(newExpression)) {
                final JSBinaryExpression  binaryExpression  = (JSBinaryExpression) newExpression;
                final IElementType        operationSign     = binaryExpression.getOperationSign();
                final String              negatedComparison = ComparisonUtils.getNegatedOperatorText(operationSign);
                final JSExpression        leftOperand       = binaryExpression.getLOperand();
                final JSExpression        rightOperand      = binaryExpression.getROperand();

                assert (rightOperand != null);

                replacementString = leftOperand.getText() + negatedComparison + rightOperand.getText();
            } else {
                replacementString = '!' + ParenthesesUtils.getParenthesized(newExpression, ParenthesesUtils.PREFIX_PRECENDENCE);
            }
        } else {
            replacementString = newExpression.getText();
        }
        replaceExpression(expressionToReplace, replacementString);
    }

    public static void replaceExpressionWithNegatedExpressionString(JSExpression exp, String newExpression)
        throws IncorrectOperationException {
        assert (exp != null);

        JSExpression expressionToReplace = BoolUtils.findNegation(exp);
        String       replacementString   = newExpression;

        if (expressionToReplace == null) {
            expressionToReplace = exp;
            replacementString   = "!(" + newExpression + ')';
        }

        replaceExpression(expressionToReplace, replacementString);
    }


    public static void replaceStatementWithUnwrapping(JSStatement statement, JSStatement newBranch)
            throws IncorrectOperationException {
        if (!(newBranch instanceof JSBlockStatement)) {
            JSElementFactory.replaceStatement(statement, newBranch.getText());
            return;
        }

        final JSBlockStatement parentBlock = PsiTreeUtil.getParentOfType(newBranch, JSBlockStatement.class);

        if (parentBlock == null) {
            JSElementFactory.replaceStatement(statement, newBranch.getText());
            return;
        }

        final JSBlockStatement block = (JSBlockStatement) newBranch;

        if (ControlFlowUtils.containsConflictingDeclarations(block, parentBlock)) {
            JSElementFactory.replaceStatement(statement, newBranch.getText());
            return;
        }

        final PsiElement containingElement = statement.getParent();

        assert (containingElement instanceof JSStatement);

        JSElementFactory.addRangeBefore(block.getStatements(), statement);
        JSElementFactory.removeElement(statement);
    }

    public static void removeElement(PsiElement element) {
        final ASTNode node       = element.getNode();
        final ASTNode parentNode = element.getParent().getNode();

        if (node != null && parentNode != null) {
            parentNode.removeChild(node);
        }
    }

    public static void reformat(PsiElement statement) throws IncorrectOperationException {
        // Reformat only in .js files due to a bug in JavaScript reformatting module when doing it in JSP files
        if (statement.getContainingFile() instanceof JSFile) {
            statement.getManager().getCodeStyleManager().reformat(statement);
        }
    }

    public static PsiElement getNonWhiteSpaceSibling(PsiElement element, boolean forward) {
        return forward
               ? PsiTreeUtil.skipSiblingsForward(element, WHITESPACE_CLASS)
               : PsiTreeUtil.skipSiblingsBackward(element, WHITESPACE_CLASS);
    }

    public static boolean isFileReadOnly(Project project, PsiFile file) {
        final ReadonlyStatusHandler instance     = ReadonlyStatusHandler.getInstance(project);
        final VirtualFile virtualFile = file.getVirtualFile();

        return virtualFile != null && instance.ensureFilesWritable(virtualFile).hasReadonlyFiles();
    }
}
