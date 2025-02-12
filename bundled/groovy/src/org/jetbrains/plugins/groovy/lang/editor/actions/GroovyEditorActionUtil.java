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

package org.jetbrains.plugins.groovy.lang.editor.actions;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrString;

/**
 * @author ilyas
 */
public abstract class GroovyEditorActionUtil {

  public static void insertSpacesByGroovyContinuationIndent(Editor editor, Project project) {
    int indentSize = CodeStyleSettingsManager.getSettings(project).getContinuationIndentSize(GroovyFileType.GROOVY_FILE_TYPE);
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < indentSize; i++) {
      buffer.append(" ");
    }
    EditorModificationUtil.insertStringAtCaret(editor, buffer.toString());
  }

  public static void insertSpacesByGroovyIndent(Editor editor, Project project) {
    int indentSize = CodeStyleSettingsManager.getSettings(project).getIndentSize(GroovyFileType.GROOVY_FILE_TYPE);
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < indentSize; i++) {
      buffer.append(" ");
    }
    EditorModificationUtil.insertStringAtCaret(editor, buffer.toString());
  }

  public static boolean isWhiteSpace(String text, int i) {
    return text.charAt(i) == ' ' ||
        text.charAt(i) == '\t' ||
        text.charAt(i) == '\r' ||
        text.charAt(i) == '\n';
  }

  public static boolean isPlainStringLiteral(ASTNode node) {
    if (!(node.getPsi() instanceof GrLiteral)) {
      return false;
    }
    String text = node.getText();
    return text.length() < 3 && text.equals("''") || text.length() > 3 && !text.substring(0, 3).equals("'''");
  }

  public static boolean isPlainGString(ASTNode node) {
    if (!(node.getPsi() instanceof GrLiteral)) {
      return false;
    }
    String text = node.getText();
    return text.length() < 3 && text.equals("\"\"") || text.length() > 3 && !text.substring(0, 3).equals("\"\"\"");
  }

  public static boolean isMultilineStringElement(ASTNode node) {
    PsiElement element = node.getPsi();
    if (element instanceof GrLiteral) {
      if (element instanceof GrString) return !((GrString) element).isPlainString();
      return isSimpleStringLiteral(((GrLiteral) element)) && !isPlainStringLiteral(node) ||
          isSimpleGStringLiteral(((GrLiteral) element)) && !isPlainGString(node);
    }
    return false;
  }

  public static boolean isSimpleStringLiteral(GrLiteral literal) {
    PsiElement child = literal.getFirstChild();
    if (child != null && child.getNode() != null) {
      ASTNode node = child.getNode();
      assert node != null;
      return node.getElementType() == GroovyTokenTypes.mSTRING_LITERAL ||
          node.getElementType() == GroovyTokenTypes.mWRONG_STRING_LITERAL;
    }
    return false;
  }

  public static boolean isSimpleGStringLiteral(GrLiteral literal) {
    PsiElement child = literal.getFirstChild();
    if (child != null && child.getNode() != null) {
      ASTNode node = child.getNode();
      assert node != null;
      return node.getElementType() == GroovyTokenTypes.mGSTRING_LITERAL;
    }
    return false;
  }

  public static TokenSet GSTRING_TOKENS = TokenSet.create(
      GroovyTokenTypes.mGSTRING_SINGLE_BEGIN,
      GroovyTokenTypes.mGSTRING_SINGLE_CONTENT,
      GroovyTokenTypes.mGSTRING_SINGLE_END,
      GroovyTokenTypes.mGSTRING_LITERAL
  );

  public static TokenSet GSTRING_TOKENS_INNER = TokenSet.create(
      GroovyTokenTypes.mGSTRING_SINGLE_BEGIN,
      GroovyTokenTypes.mGSTRING_SINGLE_CONTENT,
      GroovyTokenTypes.mGSTRING_SINGLE_END
  );
}
