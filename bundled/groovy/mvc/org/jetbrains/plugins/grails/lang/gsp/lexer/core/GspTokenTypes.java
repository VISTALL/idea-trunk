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

package org.jetbrains.plugins.grails.lang.gsp.lexer.core;

import com.intellij.psi.tree.CustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.IGspElementType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.chameleons.GroovyDeclarationElement;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.chameleons.GroovyExpressionInjectionElement;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.chameleons.GroovyMapAttributeValue;
import org.jetbrains.plugins.grails.lang.gsp.parsing.gsp.chameleons.GspDirectiveElement;
import org.jetbrains.plugins.groovy.GroovyFileType;

/**
 * @author ilyas
 */
public interface GspTokenTypes {

  IElementType JSCRIPT_BEGIN = new IGspElementType("java scriptlet begin");
  IElementType JSCRIPT_END = new IGspElementType("java scriptlet end");
  IElementType JEXPR_BEGIN = new IGspElementType("java expression begin");
  IElementType JEXPR_END = new IGspElementType("java expression end");
  IElementType JDECLAR_BEGIN = new IGspElementType("java declaration begin");
  IElementType JDECLAR_END = new IGspElementType("java declaration begin");
  IElementType JDIRECT_BEGIN = new IGspElementType("java directive begin");
  IElementType JDIRECT_END = new IGspElementType("java directive end");

  IElementType GEXPR_BEGIN = new IGspElementType("groovy expression begin");
  IElementType GEXPR_END = new IGspElementType("groovy expression end");
  IElementType GSCRIPT_BEGIN = new IGspElementType("groovy scriptlet begin");
  IElementType GSCRIPT_END = new IGspElementType("groovy scriptlet end");
  IElementType GDIRECT_BEGIN = new IGspElementType("groovy directive begin");
  IElementType GDIRECT_END = new IGspElementType("groovy directive end");
  IElementType GDECLAR_BEGIN = new IGspElementType("groovy declaration begin");
  IElementType GDECLAR_END = new IGspElementType("groovy declaration end");

  IElementType GSP_STYLE_COMMENT = new IGspElementType("GSP style comment");
  IElementType JSP_STYLE_COMMENT = new IGspElementType("JSP style commnet");

  IElementType GROOVY_CODE = new IElementType("groovy code", GroovyFileType.GROOVY_FILE_TYPE.getLanguage());
  ILazyParseableElementType GROOVY_EXPR_CODE = new GroovyExpressionInjectionElement("GROOVY EXPRESSION INJECTION");
  ILazyParseableElementType GROOVY_DECLARATION = new GroovyDeclarationElement("GROOVY DECLARATION");

  ///////////////////////////////////////////// Gtags & Directive lexems ///////////////////////////////////////////////

  CustomParsingType GSP_DIRECTIVE = new GspDirectiveElement("GROOVY DIRECTIVE");

  IElementType GSP_WHITE_SPACE = new IGspElementType("GSP whitespace");
  IElementType GSP_TAG_NAME = XmlTokenType.XML_TAG_NAME;
  IElementType GSP_ATTR_NAME = new IGspElementType("GSP attribute name");
  IElementType GSP_BAD_CHARACTER = XmlTokenType.XML_BAD_CHARACTER;
  IElementType GSP_EQ = new IGspElementType("GSP attribute equality token");
  IElementType GSP_ATTR_VALUE_START_DELIMITER = XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER;
  IElementType GSP_ATTR_VALUE_END_DELIMITER = XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER;
  IElementType GSP_ATTRIBUTE_VALUE_TOKEN = new IGspElementType("GSP attribute value token");

  IElementType GTAG_START_TAG_START = XmlTokenType.XML_START_TAG_START;
  IElementType GTAG_TAG_END = XmlTokenType.XML_TAG_END;
  IElementType GTAG_END_TAG_START = XmlTokenType.XML_END_TAG_START;
  IElementType GTAG_START_TAG_END = XmlTokenType.XML_EMPTY_ELEMENT_END;

  CustomParsingType GSP_MAP_ATTR_VALUE = new GroovyMapAttributeValue("GSP MAP ATTRIBUTE VALUE");

}
