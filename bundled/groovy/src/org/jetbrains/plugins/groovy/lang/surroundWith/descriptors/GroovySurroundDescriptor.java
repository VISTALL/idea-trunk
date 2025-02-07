package org.jetbrains.plugins.groovy.lang.surroundWith.descriptors;

import com.intellij.lang.ASTNode;
import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 22.05.2007
 */
public abstract class GroovySurroundDescriptor implements SurroundDescriptor {
  @NotNull
  public PsiElement[] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
    GrStatement[] statements = findStatementsInRange(file, startOffset, endOffset);

    if (statements == null) return PsiElement.EMPTY_ARRAY;
    return statements;
  }

  @Nullable
  private GrStatement[] findStatementsInRange(PsiFile file, int startOffset, int endOffset) {

    GrStatement statement;
    int endOffsetLocal = endOffset;
    int startOffsetLocal = startOffset;

    List<GrStatement> statements = new ArrayList<GrStatement>();
    do {
      PsiElement element1 = file.findElementAt(startOffsetLocal);
      PsiElement element2 = file.findElementAt(endOffsetLocal - 1);

      if (element1 == null) break;
      ASTNode node1 = element1.getNode();
      assert node1 != null;
      if (element1 instanceof PsiWhiteSpace || TokenSets.WHITE_SPACE_TOKEN_SET.contains(node1.getElementType()) || GroovyTokenTypes.mNLS.equals(node1.getElementType())) {
        startOffsetLocal = element1.getTextRange().getEndOffset();
      }

      if (element2 == null) break;
      ASTNode node2 = element2.getNode();
      assert node2 != null;
      if (element2 instanceof PsiWhiteSpace || TokenSets.WHITE_SPACE_TOKEN_SET.contains(node2.getElementType()) || GroovyTokenTypes.mNLS.equals(node2.getElementType())) {
        endOffsetLocal = element2.getTextRange().getStartOffset();
      }

      if (";".equals(element2.getText())) endOffsetLocal = endOffsetLocal - 1;

      statement = PsiTreeUtil.findElementOfClassAtRange(file, startOffsetLocal, endOffsetLocal, GrStatement.class);

      if (statement == null) break;
      statements.add(statement);

      startOffsetLocal = statement.getTextRange().getEndOffset();
      final PsiElement endSemicolon = file.findElementAt(startOffsetLocal);

      if (endSemicolon != null && ";".equals(endSemicolon.getText())) startOffsetLocal = startOffsetLocal + 1;
    } while (true);

    return statements.toArray(new GrStatement[0]);
  }
}
