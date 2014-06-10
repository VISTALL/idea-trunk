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

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.groovy.lang.editor.HandlerUtils;

/**
 * @author ilyas
 */
public class GspEnterHandler implements EnterHandlerDelegate {

  public Result preprocessEnter(PsiFile file,
                                Editor editor,
                                Ref<Integer> caretOffset,
                                Ref<Integer> caretAdvance,
                                DataContext dataContext,
                                EditorActionHandler originalHandler) {
    if (handleEnter(editor, dataContext, file.getProject(), originalHandler)) {
      return Result.Stop;
    }
    return Result.Continue;
  }

  protected static boolean handleEnter(Editor editor, DataContext dataContext, @NotNull Project project, EditorActionHandler originalHandler) {
    if (!HandlerUtils.canBeInvoked(editor, project)) {
      return false;
    }
    PsiFile file = HandlerUtils.getPsiFile(editor, project);
    if (!(file instanceof GspFile)) return false;
    int caretOffset = editor.getCaretModel().getOffset();
    if (caretOffset < 1) return false;

    if (handleJspLikeScriptlet(editor, caretOffset, dataContext, project, originalHandler)) {
      return true;
    }

    return false;

  }

  private static boolean handleJspLikeScriptlet(Editor editor, int caret, DataContext dataContext, Project project, EditorActionHandler originalHandler) {

    final EditorHighlighter highlighter = ((EditorEx) editor).getHighlighter();
    HighlighterIterator iterator = highlighter.createIterator(caret - 1);
    IElementType tokenType = iterator.getTokenType();
    if (tokenType != GspTokenTypesEx.JSCRIPT_BEGIN &&
        tokenType != GspTokenTypesEx.GSCRIPT_BEGIN) {
      return false;
    }
    String text = editor.getDocument().getText();
    if (caret < 2 || text.length() < Math.min(caret - 2, 2)) {
      return false;
    }
    if (text.charAt(caret - 1) == '%' && text.charAt(caret - 2) == '<' ||
        text.charAt(caret - 1) == '{' && text.charAt(caret - 2) == '%') {
      boolean isJLike = tokenType == GspTokenTypesEx.JSCRIPT_BEGIN;
      if (!GspEditorActionsUtil.areSciptletSeparatorsUnbalanced(iterator, isJLike)) {
        originalHandler.execute(editor, dataContext);
      } else {
        EditorModificationUtil.insertStringAtCaret(editor, isJLike ? "%>" : "}%");
        editor.getCaretModel().moveCaretRelatively(-2, 0, false, false, true);
        originalHandler.execute(editor, dataContext);
        if (isJLike) {
          originalHandler.execute(editor, dataContext);
          editor.getCaretModel().moveCaretRelatively(0, -1, false, false, true);
        }
      }
      GspEditorActionsUtil.insertSpacesByGspIndent(editor, project);
      return true;
    } else {
      return false;
    }
  }

}
