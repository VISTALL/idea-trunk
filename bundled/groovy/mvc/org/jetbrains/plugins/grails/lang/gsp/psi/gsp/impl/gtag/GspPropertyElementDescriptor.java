/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author peter
 */
public class GspPropertyElementDescriptor extends GspElementDescriptorBase {
  final PsiElement myNavigateElement;

  public GspPropertyElementDescriptor(GspNamespaceDescriptor nsDescriptor, PsiElement place, String localName) {
    super(nsDescriptor, place, localName);
    myNavigateElement = place.getNavigationElement();
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable final XmlTag context) {
    if (GspTagLibUtil.DEFAULT_TAGLIB_PREFIX.equals(myNsDescriptor.getPrefix())) {
      List<String> descriptors = new ArrayList<String>();

      if (myNavigateElement instanceof GrField) {
        for (Set<String> namedParametersSet : ((GrField) myNavigateElement).getNamedParametersArray()) {
          for (String namedPapameter : namedParametersSet) {
            if ("tagName".equals(namedPapameter)) continue;
            if ("type".equals(namedPapameter)) continue;
            if ("remove".equals(namedPapameter)) continue;

            descriptors.add(namedPapameter);
          }
        }
      }

      final XmlElementDescriptor tldElementDescriptor = GspTagLibUtil.getTldElementDescriptor(getName(), getDeclaration().getProject());
      if (tldElementDescriptor != null) {
        final XmlAttributeDescriptor[] attributeDescriptors = tldElementDescriptor.getAttributesDescriptors(null);
        for (XmlAttributeDescriptor attributeDescriptor : attributeDescriptors) {
          final String tldAttrName = attributeDescriptor.getName();
          if (descriptors.contains(tldAttrName)) continue;

          descriptors.add(tldAttrName);
        }
      }

      GspAttributeDescriptor[] gspAttributeDescriptors = new GspAttributeDescriptor[descriptors.size()];
      for (int i = 0, descriptorsSize = descriptors.size(); i < descriptorsSize; i++) {
        gspAttributeDescriptors[i] = new GspAttributeDescriptor(descriptors.get(i));
      }
      return gspAttributeDescriptors;
    }
    return XmlAttributeDescriptor.EMPTY;
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    if (GspTagLibUtil.DEFAULT_TAGLIB_PREFIX.equals(myNsDescriptor.getPrefix())) {
      final XmlElementDescriptor descriptor = GspTagLibUtil.getTldElementDescriptor(getName(), getDeclaration().getProject());
      if (descriptor != null) {
        return descriptor.getAttributeDescriptor(attributeName, context);
      } else {
        return new GspAttributeDescriptor(attributeName);
      }
    }
    return new AnyXmlAttributeDescriptor(attributeName);
  }

  class GspAttributeDescriptor implements XmlAttributeDescriptor {
    private final String myName;

    GspAttributeDescriptor(String attrName) {
      myName = attrName;
    }

    public boolean isRequired() {
      return false;
    }

    public boolean isFixed() {
      return false;
    }

    public boolean hasIdType() {
      return false;
    }

    public boolean hasIdRefType() {
      return false;
    }

    public String getDefaultValue() {
      return null;
    }

    public boolean isEnumerated() {
      return false;
    }

    public String[] getEnumeratedValues() {
      return new String[0];
    }

    public String validateValue(XmlElement context, String value) {
      return null;
    }

    public PsiElement getDeclaration() {
      return null;
    }

    public String getName(PsiElement context) {
      return myName;
    }

    public String getName() {
      return myName;
    }

    public void init(PsiElement element) {
    }

    public Object[] getDependences() {
      return new Object[0];
    }
  }
}
