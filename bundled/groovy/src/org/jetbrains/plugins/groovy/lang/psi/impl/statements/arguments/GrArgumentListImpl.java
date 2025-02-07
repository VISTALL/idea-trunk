/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.psi.impl.statements.arguments;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import static org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes.mCOMMA;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementImpl;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

/**
 * @author ilyas
 */
public class GrArgumentListImpl extends GroovyPsiElementImpl implements GrArgumentList {

  public GrArgumentListImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitArgumentList(this);
  }

  public String toString() {
    return "Arguments";
  }

  @NotNull
  public GrNamedArgument[] getNamedArguments() {
    return findChildrenByClass(GrNamedArgument.class);
  }

  @NotNull
  public GrExpression[] getExpressionArguments() {
    return findChildrenByClass(GrExpression.class);
  }

  public GrArgumentList replaceWithArgumentList(GrArgumentList newArgList) throws IncorrectOperationException {
    if (this.getParent() == null || this.getParent().getNode() == null) {
      throw new IncorrectOperationException();
    }
    ASTNode parentNode = this.getParent().getNode();
    ASTNode newNode = newArgList.getNode();
    assert parentNode != null;
    parentNode.replaceChild(this.getNode(), newNode);
    if (!(newNode.getPsi() instanceof GrArgumentList)) {
      throw new IncorrectOperationException();
    }
    return ((GrArgumentList)newNode.getPsi());
  }

  public boolean isIndexPropertiesList() {
    PsiElement firstChild = getFirstChild();
    if (firstChild == null) return false;
    ASTNode node = firstChild.getNode();
    assert node != null;
    return node.getElementType() == GroovyTokenTypes.mLBRACK;
  }

  @Nullable
  public PsiElement getLeftParen() {
    ASTNode paren = getNode().findChildByType(GroovyTokenTypes.mLPAREN);
    return paren != null ? paren.getPsi() : null;
  }

  @Nullable
  public PsiElement getRightParen() {
    ASTNode paren = getNode().findChildByType(GroovyTokenTypes.mRPAREN);
    return paren != null ? paren.getPsi() : null;
  }

  public int getExpressionArgumentIndex(final GrExpression arg) {
    for (int i = 0; i < getExpressionArguments().length; i++) {
      GrExpression expression = getExpressionArguments()[i];
      if (expression == arg) return i;
    }
    return -1;
  }

  @Nullable
  public GrExpression removeArgument(final int argNumber) {
    for (int i = 0; i < getExpressionArguments().length; i++) {
      GrExpression expression = getExpressionArguments()[i];
      if (i == argNumber) {
        final ASTNode exprNode = expression.getNode();
        final PsiElement prevElem = PsiUtil.getPrevNonSpace(expression);
        final PsiElement nextElem = PsiUtil.getNextNonSpace(expression);
        getNode().removeChild(exprNode);
        if (nextElem != null && nextElem.getNode() != null && (nextElem.getNode().getElementType() == mCOMMA)) {
          final PsiElement n = nextElem.getNextSibling();
          getNode().removeChild(nextElem.getNode());
          if (n != null && n instanceof PsiWhiteSpace) {
            getNode().removeChild(n.getNode());
          }
        } else if (prevElem != null) {
          final ASTNode prev = prevElem.getNode();
          if (prev != null && prev.getElementType() == mCOMMA) {
            final PsiElement p = prevElem.getPrevSibling();
            getNode().removeChild(prev);
            getNode().removeChild(prevElem.getNode());
            if (p != null && p instanceof PsiWhiteSpace) {
              getNode().removeChild(p.getNode());
            }
          }
        }
        return expression;
      }
    }
    return null;
  }

  public GrNamedArgument addNamedArgument(final GrNamedArgument namedArgument) {
    final GrNamedArgument[] namedArguments = getNamedArguments();
    final GrExpression[] args = getExpressionArguments();
    final ASTNode newNode = namedArgument.getNode();
    PsiElement anchor = null;
    final int namedCount = namedArguments.length;
    final int exprCount = args.length;
    if (namedCount > 0) {
      anchor = namedArguments[namedCount - 1];
    } else if (exprCount > 0) {
      anchor = args[exprCount - 1];
    }

    if (anchor != null) {
      anchor = PsiUtil.getNextNonSpace(anchor);
    } else {
      anchor = getRightParen();
    }

    if (anchor != null) {
      final ASTNode astNode = anchor.getNode();
      getNode().addChild(newNode, astNode);
    } else {
      getNode().addChild(newNode);
    }

    if (namedCount + exprCount > 0) {
      getNode().addLeaf(mCOMMA, ",", newNode);
    }
    return namedArgument;
  }

  @Override
  public PsiElement addAfter(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
    GrExpression[] params = getExpressionArguments();
    final ASTNode astNode = getNode();

    if (anchor == null) anchor = getLeftParen();

    PsiElement result;
    if (params.length == 0) {
      result = super.addAfter(element, anchor);
    }
    else {
      result = super.addAfter(element, anchor);
      astNode.addLeaf(mCOMMA, ",", result.getNode());
    }

    CodeStyleManager.getInstance(getManager().getProject()).reformat(this);
    return result;
  }

  public void deleteChildInternal(@NotNull ASTNode child) {
    ASTNode next = TreeUtil.skipElements(child.getTreeNext(), GroovyTokenTypes.WHITE_SPACES_OR_COMMENTS);
    if (next != null && next.getElementType() == mCOMMA) {
      deleteChildInternal(next);
    }
    else {
      ASTNode prev = TreeUtil.skipElementsBack(child.getTreePrev(), GroovyTokenTypes.WHITE_SPACES_OR_COMMENTS);
      if (prev != null && prev.getElementType() == mCOMMA) {
        deleteChildInternal(prev);
      }
    }
    super.deleteChildInternal(child);
  }
}