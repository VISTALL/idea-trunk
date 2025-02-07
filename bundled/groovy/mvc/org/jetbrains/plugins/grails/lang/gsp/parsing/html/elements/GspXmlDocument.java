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

package org.jetbrains.plugins.grails.lang.gsp.parsing.html.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.tree.ChildRoleBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlChildRole;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlProlog;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;

/**
 * @author ilyas
 */
public class GspXmlDocument extends XmlDocumentImpl {
  public GspXmlDocument() {
    super(GspElementTypes.GSP_XML_DOCUMENT);
  }

  public int getChildRole(ASTNode child) {
    IElementType i = child.getElementType();
    if (i == XmlElementType.XML_PROLOG) {
      return XmlChildRole.XML_PROLOG;
    } else {
      return ChildRoleBase.NONE;
    }
  }

  public XmlProlog getProlog() {
    return (XmlProlog) findElementByTokenType(XmlElementType.XML_PROLOG);
  }

  public XmlTag getRootTag() {
    return (XmlTag) findElementByTokenType(GspElementTypes.GSP_ROOT_TAG);
  }

  public String toString() {
    return "PsiElement" + "(" + XmlElementType.XML_DOCUMENT.toString() + ")";
  }
}