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

package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.directive;

import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirectiveAttributeValue;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirectiveAttribute;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.reference.attribute.GspContentTypeAttributeValueReference;

/**
 * @author ilyas
 */
public class GspDirectiveAttributeValueImpl extends XmlAttributeValueImpl implements GspDirectiveAttributeValue {

  public IElementType getElementType() {
    return GspElementTypes.GSP_DIRECTIVE_ATTRIBUTE_VALUE;
  }

  @NotNull
  public PsiReference[] getReferences() {
    PsiReference[] refs = super.getReferences();
    if (refs.length == 0) {
      GspDirectiveAttribute attribute = getContainingAttribute();
      if (attribute != null && "contentType".equals(attribute.getName())) {
        GspContentTypeAttributeValueReference ref = new GspContentTypeAttributeValueReference(this);
        return new PsiReference[]{ref};
      }
      return refs;
    }
    else {
    return refs;
    }
  }

  public PsiReference getReference() {
    return super.getReference();
  }

  private GspDirectiveAttribute getContainingAttribute() {
    PsiElement parent = getParent();
    GspDirectiveAttribute attribute;
    if (parent instanceof GspDirectiveAttribute) {
      attribute = (GspDirectiveAttribute) parent;
    } else {
      attribute = null;
    }
    return attribute;
  }

  @NotNull
  public Language getLanguage() {
    return StdLanguages.XML;
  }

  public String toString() {
    return "GSP directive attribute value";
  }
}