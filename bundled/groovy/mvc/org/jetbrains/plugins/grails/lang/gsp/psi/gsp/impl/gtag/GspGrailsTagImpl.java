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

package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;

/**
 * @author ilyas
 */
public class GspGrailsTagImpl extends XmlTagImpl implements GspGrailsTag {

  public GspGrailsTagImpl() {
    super(GspElementTypes.GRAILS_TAG);
  }

  public String toString() {
    return "Grails tag";
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor) visitor).visitXmlTag(this);
    } else {
      visitor.visitElement(this);
    }
  }

  public boolean endsByError() {
    return endsByError(this);
  }

  private static boolean endsByError(PsiElement elem) {
    if (elem instanceof PsiErrorElement) return true;
    PsiElement[] children = elem.getChildren();
    return children.length > 0 && endsByError(children[children.length - 1]);
  }

  public PsiElement getNameElement() {
    for (ASTNode e = getFirstChildNode(); e != null; e = e.getTreeNext()) {
      if (e instanceof XmlTokenImpl) {
        XmlTokenImpl xmlToken = (XmlTokenImpl) e;

        if (xmlToken.getTokenType() == XmlTokenType.XML_TAG_NAME) return xmlToken;
      }
    }

    return null;
  }


}
