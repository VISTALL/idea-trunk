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

package org.jetbrains.plugins.grails.lang.gsp.parsing.gsp.lexer;

import com.intellij.lexer.DelegateLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspFlexLexer;
import static org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx.GSP_GROOVY_CODE;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;

/**
 * @author ilyas
 */
public class GspLexer extends DelegateLexer implements GspTokenTypes, XmlTokenType {
  public GspLexer() {
    super(new GspFlexLexer());
  }

  public IElementType getTokenType() {
    return convertToken(super.getTokenType());
  }

  /**
   * Converts token for GSP representation
   *
   * @param tokenType
   * @return
   */
  private static IElementType convertToken(IElementType tokenType) {
    if (GROOVY_EXPR_CODE.equals(tokenType) ||
        GSP_MAP_ATTR_VALUE.equals(tokenType) ||
        GROOVY_CODE.equals(tokenType) ||
        GROOVY_DECLARATION.equals(tokenType)) {
      return GSP_GROOVY_CODE;
    }
    return tokenType;
  }
}
