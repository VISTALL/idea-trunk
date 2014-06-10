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

package org.jetbrains.plugins.grails.lang.gsp.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.xml.IXmlElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.parsing.composite.GspCompositeElementType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.html.elements.GspXmlDocument;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.GspDeclarationTagImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.GspExprTagImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.GspScriptletTagImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.directive.GspDirectiveAttributeImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.directive.GspDirectiveAttributeValueImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.GspAttributeImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.GspAttributeValueImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.GspGrailsTagImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.GspXmlRootTagImpl;

/**
 * @author ilyas
 */
public interface GspElementTypes extends GspTokenTypesEx {

  /*
  "Pure" GSP elements
   */
  IFileElementType GSP_FILE = new IFileElementType("GSP File", GspFileType.GSP_FILE_TYPE.getLanguage());

  IXmlElementType GSP_ROOT_TAG = new GspCompositeElementType("GSP root tag") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspXmlRootTagImpl();
    }
  };

  IXmlElementType GSP_SCRIPTLET_TAG = new GspCompositeElementType("GSP SCRIPTLET TAG") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspScriptletTagImpl();
    }
  };

  IXmlElementType GSP_XML_DOCUMENT = new GspCompositeElementType("GSP XML DOCUMENT") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspXmlDocument();
    }
  };

  IXmlElementType GSP_EXPR_TAG = new GspCompositeElementType("GSP EXPRESSION TAG") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspExprTagImpl();
    }
  };

  IXmlElementType GSP_DECLARATION_TAG = new GspCompositeElementType("GSP DECLARATION TAG") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspDeclarationTagImpl();
    }
  };

  IXmlElementType GRAILS_TAG = new GspCompositeElementType("GRAILS TAG") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspGrailsTagImpl();
    }
  };

  IXmlElementType GRAILS_TAG_ATTRIBUTE = new GspCompositeElementType("GRAILS TAG ATTRIBUTE") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspAttributeImpl();
    }
  };

  IXmlElementType GRAILS_TAG_ATTRIBUTE_VALUE = new GspCompositeElementType("GRAILS TAG ATTRIBUTE VALUE") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspAttributeValueImpl();
    }
  };

  // Composite directive elements
  IXmlElementType GSP_DIRECTIVE_ATTRIBUTE = new GspCompositeElementType("GSP directive attribute") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspDirectiveAttributeImpl();
    }
  };

  IXmlElementType GSP_DIRECTIVE_ATTRIBUTE_VALUE = new GspCompositeElementType("GSP attribute value") {
    @NotNull
    public ASTNode createCompositeNode() {
      return new GspDirectiveAttributeValueImpl();
    }
  };

}
