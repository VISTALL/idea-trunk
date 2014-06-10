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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;

/**
 * @author ilyas
 */
public class GspEditorActionsUtil {
  public static void insertSpacesByGspIndent(Editor editor, Project project) {
    int indentSize = CodeStyleSettingsManager.getSettings(project).getIndentSize(GspFileType.GSP_FILE_TYPE);
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < indentSize; i++) {
      buffer.append(" ");
    }
    EditorModificationUtil.insertStringAtCaret(editor, buffer.toString());
  }

  public static boolean areSciptletSeparatorsUnbalanced(HighlighterIterator iterator, boolean jLike) {
    int balance = 0;
    if (jLike) {
      while (!iterator.atEnd()) {
        if (GspTokenTypesEx.JSCRIPT_BEGIN == iterator.getTokenType() ||
                GspTokenTypesEx.JEXPR_BEGIN == iterator.getTokenType() ||
                GspTokenTypesEx.JDIRECT_BEGIN == iterator.getTokenType() ||
                GspTokenTypesEx.JDECLAR_BEGIN == iterator.getTokenType()) balance++;

        if (GspTokenTypesEx.JSCRIPT_END == iterator.getTokenType() ||
                GspTokenTypesEx.JEXPR_END == iterator.getTokenType() ||
                GspTokenTypesEx.JDIRECT_END == iterator.getTokenType() ||
                GspTokenTypesEx.JDECLAR_END == iterator.getTokenType()) balance--;
        iterator.advance();
      }
    } else {
      while (!iterator.atEnd()) {
        if (GspTokenTypesEx.GSCRIPT_BEGIN == iterator.getTokenType()) balance++;

        if (GspTokenTypesEx.GSCRIPT_END == iterator.getTokenType()) balance--;
        iterator.advance();
      }

    }
    return balance >= 0;
  }
}
