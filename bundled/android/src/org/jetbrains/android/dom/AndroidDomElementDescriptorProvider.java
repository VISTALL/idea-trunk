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

package org.jetbrains.android.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DefinesXml;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.dom.DomElementXmlDescriptor;
import org.jetbrains.android.dom.layout.LayoutViewElement;
import org.jetbrains.android.facet.AndroidFacet;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Sep 4, 2009
 * Time: 6:04:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidDomElementDescriptorProvider implements XmlElementDescriptorProvider {
  public XmlElementDescriptor getDescriptor(XmlTag tag) {
    final DomElement domElement = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
    if (domElement instanceof LayoutViewElement) {
      AndroidFacet facet = AndroidFacet.getInstance(domElement);
      if (facet != null) {
        Map<String, PsiClass> viewClassMap = AndroidDomExtender.getViewClassMap(facet);
        PsiClass aClass = viewClassMap.get(domElement.getXmlTag().getName());
        if (aClass != null) {
          final DefinesXml definesXml = domElement.getAnnotation(DefinesXml.class);
          if (definesXml != null) {
            return new AndroidViewClassTagDescriptor(aClass, new DomElementXmlDescriptor(domElement));
          }
          final PsiElement parent = tag.getParent();
          if (parent instanceof XmlTag) {
            final XmlElementDescriptor parentDescriptor = ((XmlTag)parent).getDescriptor();

            if (parentDescriptor != null && parentDescriptor instanceof AndroidViewClassTagDescriptor) {
              XmlElementDescriptor domDescriptor = parentDescriptor.getElementDescriptor(tag, (XmlTag)parent);
              return new AndroidViewClassTagDescriptor(aClass, domDescriptor);
            }
          }
        }
      }
    }
    return null;
  }
}
