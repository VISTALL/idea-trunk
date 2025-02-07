package org.jetbrains.plugins.groovy.lang.completion.smartEnter.fixers;

import org.jetbrains.plugins.groovy.lang.completion.smartEnter.fixers.GrFixer;
import org.jetbrains.plugins.groovy.lang.completion.smartEnter.GroovySmartEnterProcessor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrIfStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrCondition;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * User: Dmitry.Krasilschikov
 * Date: 31.07.2008
 */
public class GrIfConditionFixer implements GrFixer {
  public void apply(Editor editor, GroovySmartEnterProcessor processor, PsiElement psiElement) throws IncorrectOperationException {
    if (psiElement instanceof GrIfStatement) {
      final Document doc = editor.getDocument();
      final GrIfStatement ifStatement = (GrIfStatement) psiElement;
      final PsiElement rParen = ifStatement.getRParenth();
      final PsiElement lParen = ifStatement.getLParenth();
      final GrCondition condition = ifStatement.getCondition();

      if (condition == null) {
        if (lParen == null || rParen == null) {
          int stopOffset = doc.getLineEndOffset(doc.getLineNumber(ifStatement.getTextRange().getStartOffset()));
          final GrStatement then = ifStatement.getThenBranch();
          if (then != null) {
            stopOffset = Math.min(stopOffset, then.getTextRange().getStartOffset());
          }
          stopOffset = Math.min(stopOffset, ifStatement.getTextRange().getEndOffset());

          doc.replaceString(ifStatement.getTextRange().getStartOffset(), stopOffset, "if ()");

          processor.registerUnresolvedError(ifStatement.getTextRange().getStartOffset() + "if (".length());
        } else {
          processor.registerUnresolvedError(lParen.getTextRange().getEndOffset());
        }
      } else if (rParen == null) {
        doc.insertString(condition.getTextRange().getEndOffset(), ")");
      }
    }
  }
}
