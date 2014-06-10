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

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspDirectiveFlexLexer;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;

/**
 * @author ilyas
 */
class GspDirectiveHighlightingLexer extends GspDirectiveFlexLexer {
  public IElementType getTokenType() {
    IElementType type = super.getTokenType();
    if (type == XmlTokenType.XML_TAG_NAME) return GspTokenTypesEx.GSP_TAG_NAME;
    if (type == XmlTokenType.XML_NAME) return GspTokenTypesEx.GSP_ATTR_NAME;
    if (type == XmlTokenType.XML_WHITE_SPACE) return GspTokenTypesEx.GSP_WHITE_SPACE;
    if (type == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) return GspTokenTypesEx.GSP_ATTRIBUTE_VALUE_TOKEN;
    if (type == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER)
      return GspTokenTypesEx.GSP_ATTR_VALUE_START_DELIMITER;
    if (type == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) return GspTokenTypesEx.GSP_ATTR_VALUE_END_DELIMITER;
    if (type == XmlTokenType.XML_EQ) return GspTokenTypesEx.GSP_EQ;
    return type;
  }
}
