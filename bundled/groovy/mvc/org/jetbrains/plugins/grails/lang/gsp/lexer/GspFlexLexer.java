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

package org.jetbrains.plugins.grails.lang.gsp.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core._GspLexer;

import java.io.Reader;

/**
 * @author ilyas
 */
public class GspFlexLexer extends MergingLexerAdapter {

  private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(GspTokenTypesEx.GSP_TEMPLATE_DATA,
      XmlTokenType.XML_WHITE_SPACE,
      GspTokenTypesEx.GROOVY_CODE,
      GspTokenTypesEx.GROOVY_DECLARATION,
      GspTokenTypesEx.GSP_STYLE_COMMENT,
      GspTokenTypesEx.JSP_STYLE_COMMENT,
      GspTokenTypesEx.GSP_DIRECTIVE,
      GspTokenTypesEx.GSP_ATTRIBUTE_VALUE_TOKEN,
      GspTokenTypesEx.GROOVY_EXPR_CODE,
      GspTokenTypesEx.GSP_BAD_CHARACTER
  );

  public GspFlexLexer() {
    super(new FlexAdapter(new _GspLexer((Reader) null)), TOKENS_TO_MERGE);
  }
}
