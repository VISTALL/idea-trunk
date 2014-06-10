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

import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;
import org.jetbrains.plugins.grails.lang.gsp.parsing.html.GspHtmlTemplateRootType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.outers.GspGroovyCodeElementType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.outers.GspInHtmlElementType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.outers.GspTemplateDataElementType;

/**
 * @author ilyas
 */
public interface GspTokenTypesEx extends XmlTokenType, XmlElementType, GspTokenTypes {

  // !(GspCode || GspSeparators)
  IGspElementType GSP_TEMPLATE_DATA = new GspTemplateDataElementType();

  // GSP fragment in HTML code
  IGspElementType GSP_FRAGMENT_IN_HTML = new GspInHtmlElementType();

  // Groovy fragment in GSP code
  IGspElementType GSP_GROOVY_CODE = new GspGroovyCodeElementType();

  // Html elements in GSP
  IFileElementType GSP_HTML_TEMPLATE_ROOT = new GspHtmlTemplateRootType("GSP HTML TEMPLATE ROOT");


  TokenSet GSP_COMMENTS = TokenSet.create(GSP_STYLE_COMMENT, JSP_STYLE_COMMENT);
  TokenSet GSP_GROOVY_SEPARATORS = TokenSet.create(
          JDECLAR_BEGIN,
          JEXPR_BEGIN,
          JDIRECT_BEGIN,
          JDIRECT_END,
          JSCRIPT_BEGIN,
          JSCRIPT_END,
          JEXPR_END,
          GEXPR_BEGIN,
          GEXPR_END,
          GSCRIPT_BEGIN,
          GSCRIPT_END,
          GDIRECT_BEGIN,
          GDIRECT_END,
          GDECLAR_BEGIN,
          GDECLAR_END
  );
}
