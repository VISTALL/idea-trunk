package org.jetbrains.plugins.groovy.lang.surroundWith.surrounders.surroundersImpl.blocks.open;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrCondition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.surroundWith.surrounders.GroovyManyStatementsSurrounder;

/**
 * User: Dmitry.Krasilschikov
 * Date: 25.05.2007
 */
public class GroovyWithWhileSurrounder extends GroovyManyStatementsSurrounder {
  protected GroovyPsiElement doSurroundElements(PsiElement[] elements) throws IncorrectOperationException {
    GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(elements[0].getProject());
    GrWhileStatement whileStatement = (GrWhileStatement) factory.createTopElementFromText("while(a){\n}");
    addStatements(((GrBlockStatement) whileStatement.getBody()).getBlock(), elements);
    return whileStatement;
  }

  protected TextRange getSurroundSelectionRange(GroovyPsiElement element) {
    assert element instanceof GrWhileStatement;
    GrCondition condition = ((GrWhileStatement) element).getCondition();

    int endOffset = element.getTextRange().getEndOffset();
    if (condition != null) {
      endOffset = condition.getTextRange().getStartOffset();
      condition.getParent().getNode().removeChild(condition.getNode());
    }
    return new TextRange(endOffset, endOffset);
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

  public String getTemplateDescription() {
    return "while () {...}";
  }
}
