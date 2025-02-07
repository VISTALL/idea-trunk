package org.jetbrains.plugins.groovy.lang.surroundWith.surrounders.surroundersImpl.blocks.open;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrCondition;
import org.jetbrains.plugins.groovy.lang.surroundWith.surrounders.GroovyManyStatementsSurrounder;
import org.jetbrains.annotations.NotNull;

/**
 * User: Dmitry.Krasilschikov
 * Date: 23.05.2007
 */
public class GroovyWithIfSurrounder extends GroovyManyStatementsSurrounder {
  protected GroovyPsiElement doSurroundElements(PsiElement[] elements) throws IncorrectOperationException {
    GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(elements[0].getProject());
    GrIfStatement ifStatement = (GrIfStatement) factory.createTopElementFromText("if (a) {\n}");
    addStatements(((GrBlockStatement)ifStatement.getThenBranch()).getBlock(), elements);
    return ifStatement;
  }

  protected TextRange getSurroundSelectionRange(GroovyPsiElement element) {
    assert element instanceof GrIfStatement;
    GrCondition condition = ((GrIfStatement) element).getCondition();

    int endOffset = element.getTextRange().getEndOffset();
    if (condition != null) {
      PsiElement child = condition.getFirstChild();
      assert child != null;

      endOffset = child.getTextRange().getStartOffset();
      condition.getParent().getNode().removeChild(condition.getNode());
    }
    return new TextRange(endOffset, endOffset);
  }

  public String getTemplateDescription() {
    return "if () {...}";
  }

  public boolean isApplicable(@NotNull PsiElement[] elements) {
    if (!super.isApplicable(elements)) return false;

    if (elements.length == 1 && elements[0] instanceof GrStatement) {
      if (elements[0] instanceof GrExpression) {
        PsiType type = ((GrExpression) elements[0]).getType();
        return type == null || !((PsiPrimitiveType) PsiType.BOOLEAN).getBoxedTypeName().equals(type.getCanonicalText());
      }
    }
    return true;
  }
}
