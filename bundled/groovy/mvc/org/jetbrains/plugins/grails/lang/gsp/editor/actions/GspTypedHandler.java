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

package org.jetbrains.plugins.grails.lang.gsp.editor.actions;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.grails.addins.js.JavaScriptIntegrationUtil;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import static org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes.GEXPR_END;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.groovy.lang.editor.HandlerUtils;

/**
 * @author ilyas
 */
public class GspTypedHandler extends TypedHandlerDelegate {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.lang.gsp.editor.actions.GspTypedHandler");

  @Override
  public Result beforeCharTyped(char c, Project project, Editor editor, PsiFile file, FileType fileType) {
    if (handleTyping(editor, c, project)) {
      return Result.STOP;
    }
    return Result.CONTINUE;
  }

  private static boolean handleTyping(final Editor editor, final char charTyped, final Project project) {
    if (project == null || !HandlerUtils.canBeInvoked(editor, project)) {
      return false;
    }

    final PsiElement file = HandlerUtils.getPsiFile(editor, project);
    if (!(file instanceof GspFile)) return false;

    int caret = editor.getCaretModel().getOffset();
    final EditorHighlighter highlighter = ((EditorEx) editor).getHighlighter();
    if (caret < 1) return false;
    HighlighterIterator iterator = highlighter.createIterator(caret - 1);

    String text = editor.getDocument().getText();
    if (iterator.getTokenType() == GspTokenTypesEx.JSCRIPT_BEGIN) {
      if (GspEditorActionsUtil.areSciptletSeparatorsUnbalanced(iterator, true)) {
        if ('=' == charTyped) {
          return handleJspLikeEqualTyped(editor, text, caret);
        }
        if ('@' == charTyped) {
          return handleJspLikeDirectiveTyped(editor, text, caret);
        }
        if ('!' == charTyped) {
          return handleJspLikeDeclarationTyped(editor, text, caret);
        }
      }
    }
    iterator = highlighter.createIterator(caret - 1);
    if ((iterator.getTokenType() == GspTokenTypesEx.XML_DATA_CHARACTERS ||
        JavaScriptIntegrationUtil.isJSElementType(iterator.getTokenType()))
        && '{' == charTyped) {
      return handleGspLeftBraceTyped(editor, text, caret, false, project);
    }

    if (iterator.getTokenType() == GspTokenTypesEx.GSP_ATTRIBUTE_VALUE_TOKEN && '{' == charTyped) {
      return handleGspLeftBraceTyped(editor, text, caret, true, project);
    }
    if ('}' == charTyped) {
      return handleGspRightBraceTyped(editor, text, caret);
    }

    return false;
  }

  private static boolean handleGspRightBraceTyped(Editor editor, String text, int caret) {
    if (caret == 0 || text.length() < 2 || text.length() < caret + 1) {
      return false;
    }
    if (mustNotPlaceBrace(editor, caret)) {
      editor.getCaretModel().moveCaretRelatively(1, 0, false, false, true);
      return true;
    }
    return false;
  }

  private static boolean mustNotPlaceBrace(Editor editor, int caret) {
    String text = editor.getDocument().getText();
    final EditorHighlighter highlighter = ((EditorEx) editor).getHighlighter();
    if (caret < 1) return false;
    HighlighterIterator iterator = highlighter.createIterator(caret);
    return text.charAt(caret) == '}' && iterator.getTokenType() == GEXPR_END;
  }


  private static boolean handleGspLeftBraceTyped(final Editor editor, final String text, final int caret, boolean inGrailsTagArgValue,
                                                 final Project project) {
    if (caret < 1 || text.length() < Math.min(caret - 1, 1)) {
      return false;
    }
    EditorActionManager manager = EditorActionManager.getInstance();
    EditorActionHandler handler = manager.getActionHandler(IdeActions.ACTION_EDITOR_ENTER);

    LOG.assertTrue(project != null);

    if (text.charAt(caret - 1) == '$') {
      if (text.length() > caret && text.charAt(caret) == '}') {
        return false;
      }
      EditorModificationUtil.insertStringAtCaret(editor, "{}"); //GSP-like expression
      editor.getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
      return true;
    }
    if (text.charAt(caret - 1) == '@' && !inGrailsTagArgValue) { //GSP-like directive
      if (text.length() > caret && text.charAt(caret) == '}') {
        return false;
      }
      EditorModificationUtil.insertStringAtCaret(editor, "{  }");
      editor.getCaretModel().moveCaretRelatively(-2, 0, false, false, true);
      return true;
    }
    if (text.charAt(caret - 1) == '!' && !inGrailsTagArgValue) {  //GSP-like declaration
      if (text.length() > caret && text.charAt(caret) == '}') {
        return false;
      }
      EditorModificationUtil.insertStringAtCaret(editor, "{}!");
      editor.getCaretModel().moveCaretRelatively(-2, 0, false, false, true);
      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      handler.execute(editor, DataManager.getInstance().getDataContext(editor.getContentComponent()));
      GspEditorActionsUtil.insertSpacesByGspIndent(editor, project);
      return true;
    }
// removed according to GRVY-1519
/* 
    if (text.charAt(caret - 1) == '%' && !inGrailsTagArgValue) {  //GSP-like code injection
      if (text.length() > caret && text.charAt(caret) == '}') {
        return false;
      }
      EditorModificationUtil.insertStringAtCaret(editor, "{}%");
      editor.getCaretModel().moveCaretRelatively(-2, 0, false, false, true);
      PsiDocumentManager.getInstance(myProject).commitDocument(editor.getDocument());
      handler.execute(editor, dataContext);
      GspEditorActionsUtil.insertSpacesByGspIndent(editor, dataContext);
      return true;
    }
*/
    return false;
  }

  /**
   * Inserts JSP-like expression injection ending
   */
  private static boolean handleJspLikeEqualTyped(final Editor editor, final String text, final int caret) {
    if (caret < 2 || text.length() < Math.min(caret - 2, 2)) {
      return false;
    }
    if (text.charAt(caret - 1) == '%' && text.charAt(caret - 2) == '<') {
      if (text.length() > caret && text.charAt(caret) == '%') {
        return false;
      }
      EditorModificationUtil.insertStringAtCaret(editor, "=%>");
      editor.getCaretModel().moveCaretRelatively(-2, 0, false, false, true);
      return true;
    }
    return false;
  }

  /**
   * Inserts JSP-like directive ending
   */
  private static boolean handleJspLikeDirectiveTyped(final Editor editor, final String text, final int caret) {
    if (caret < 2 || text.length() < Math.min(caret - 2, 2)) {
      return false;
    }
    if (text.charAt(caret - 1) == '%' && text.charAt(caret - 2) == '<') {
      if (text.length() > caret && text.charAt(caret) == '%') {
        return false;
      }
      EditorModificationUtil.insertStringAtCaret(editor, "@  %>");
      editor.getCaretModel().moveCaretRelatively(-3, 0, false, false, true);
      return true;
    }
    return false;
  }

  /**
   * Inserts JSP-like declaration ending
   */
  private static boolean handleJspLikeDeclarationTyped(final Editor editor, final String text, final int caret) {
    if (caret < 2 || text.length() < Math.min(caret - 2, 2)) {
      return false;
    }
    if (text.charAt(caret - 1) == '%' && text.charAt(caret - 2) == '<') {
      if (text.length() > caret && text.charAt(caret) == '%') {
        return false;
      }
      EditorModificationUtil.insertStringAtCaret(editor, "!%>");
      editor.getCaretModel().moveCaretRelatively(-2, 0, false, false, true);
      return true;
    }
    return false;
  }

}
