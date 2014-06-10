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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlChildRole;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirectiveAttribute;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.GspElementDescriptorBase;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.GspNamespaceDescriptor;
import org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;

/**
 * @author ilyas
 */
public class GspDirectiveImpl extends XmlTagImpl implements GspDirective {
  @NonNls private static final String TAGLIB_DIRECTIVE = "taglib";
  @NonNls private static final String PREFIX = "prefix";
  @NonNls private static final String URI = "uri";

  public GspDirectiveImpl() {
    super(GspTokenTypes.GSP_DIRECTIVE);
  }

  public String toString() {
    return "GSP directive";
  }

  @Override
  public String[] knownNamespaces() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @NotNull
  public String getNamespace() {
    return GspTagLibUtil.DEFAULT_TAGLIB_PREFIX;
  }

  public boolean isTaglibDirective() {
    return TAGLIB_DIRECTIVE.equals(getName());
  }

  public String getPrefixAttributeValue() {
    return getAttributeValue(PREFIX);
  }

  public String getUriAttributeValue() {
    return getAttributeValue(URI);
  }

  @Override
  public XmlElementDescriptor getDescriptor() {
    return new GspElementDescriptorBase(GspNamespaceDescriptor.getDefaultNsDescriptor(GspDirectiveImpl.this), GspDirectiveImpl.this, getLocalName()) {

      @Override
      public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
        return null;
      }

      @Override
      public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
        return EMPTY_ARRAY;
      }

      public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
        if (isTaglibDirective()) {
          return new XmlAttributeDescriptor[]{new AnyXmlAttributeDescriptor(PREFIX), new AnyXmlAttributeDescriptor(URI)};
        }

        return new XmlAttributeDescriptor[]{new AnyXmlAttributeDescriptor("import"), new AnyXmlAttributeDescriptor("contentType")};
      }

      public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
        for (final XmlAttributeDescriptor descriptor : getAttributesDescriptors(context)) {
          if (descriptor.getName().equals(attributeName)) {
            return descriptor;
          }
        }
        return null;
      }

    };
  }

  public boolean addOrReplaceAttribute(@NotNull GspDirectiveAttribute attribute) {
    // only if not exists
    GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(getProject());
    GspDirectiveAttribute oldAttribute = (GspDirectiveAttribute) getAttribute(attribute.getName());
    if (oldAttribute == null) {
      ASTNode startTagName = XmlChildRole.START_TAG_NAME_FINDER.findChild(this);
      if (startTagName == null) return false;
      PsiElement element = startTagName.getPsi();
      assert element != null;
      try {
        addAfter(attribute, element);
      } catch (IncorrectOperationException e) {
        return false;
      }
      return true;
    } else {
      ASTNode oldChild = oldAttribute.getNode();
      ASTNode newChild = attribute.getNode();
      assert oldChild != null && newChild != null;
      replaceChild(oldChild, newChild);
      return true;
    }
  }
}
