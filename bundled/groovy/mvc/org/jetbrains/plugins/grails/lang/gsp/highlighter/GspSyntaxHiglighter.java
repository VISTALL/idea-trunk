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

package org.jetbrains.plugins.grails.lang.gsp.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.JspHighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspFlexLexer;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.groovy.highlighter.DefaultHighlighter;

/**
 * @author ilyas
 */
public class GspSyntaxHiglighter extends SyntaxHighlighterBase implements GspTokenTypesEx {

  private final GspDirectiveHighlighter myDirectiveHighlighter = new GspDirectiveHighlighter();

  @NotNull
  public Lexer getHighlightingLexer() {
    return new GspFlexLexer();
  }

  static final TokenSet tGSP_SEPARATORS_NOT_DIRECT = TokenSet.create(
          JSCRIPT_BEGIN,
          JDECLAR_BEGIN,
          JDECLAR_END,
          JEXPR_BEGIN,
          JSCRIPT_END,
          JEXPR_END,
          GEXPR_BEGIN,
          GEXPR_END,
          GSCRIPT_BEGIN,
          GSCRIPT_END,
          GDECLAR_BEGIN,
          GDECLAR_END);


  static final TokenSet tGSP_DIRECT = TokenSet.create(GSP_DIRECTIVE);

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    if (tGSP_SEPARATORS_NOT_DIRECT.contains(tokenType)) {
      return pack(SyntaxHighlighterColors.KEYWORD, JspHighlighterColors.JSP_SCRIPTING_BACKGROUND);
    }
    if (GspTokenTypesEx.GSP_COMMENTS.contains(tokenType)) {
      return pack(DefaultHighlighter.BLOCK_COMMENT);
    }
    if (myDirectiveHighlighter.getTokenHighlights(tokenType).length > 0) {
      return pack(myDirectiveHighlighter.getTokenHighlights(tokenType), JspHighlighterColors.JSP_ACTION_AND_DIRECTIVE_BACKGROUND);
    }
    return pack(null);
  }
}
