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
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspAttribute;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspAttributeValue;

/**
 * @author ilyas
 */
public class GspAttributeImpl extends XmlAttributeImpl implements GspAttribute {

  public String toString() {
    return "GSP attribute";
  }

  public IElementType getElementType() {
    return GspElementTypes.GRAILS_TAG_ATTRIBUTE;
  }

  public XmlAttributeValue getValueElement() {
    ASTNode node = findChildByType(GspElementTypes.GRAILS_TAG_ATTRIBUTE_VALUE);
    if (node != null) {
      PsiElement psi = node.getPsi();
      assert psi instanceof GspAttributeValue;
      return (GspAttributeValue) psi   ;
    }
    return null;
  }

}