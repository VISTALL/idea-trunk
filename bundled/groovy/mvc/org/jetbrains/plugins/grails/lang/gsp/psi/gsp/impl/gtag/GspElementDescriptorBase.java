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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.CommonProcessors;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author peter
 */
public abstract class GspElementDescriptorBase implements XmlElementDescriptor {
  protected final GspNamespaceDescriptor myNsDescriptor;
  private final PsiElement myPlace;
  protected final String myLocalName;

  protected GspElementDescriptorBase(GspNamespaceDescriptor nsDescriptor, PsiElement place, @NotNull String localName) {
    myNsDescriptor = nsDescriptor;
    myPlace = place;
    myLocalName = localName;
  }

  public String getQualifiedName() {
    return myNsDescriptor.getPrefix() + ":" + myLocalName;
  }

  public String getDefaultName() {
    return getQualifiedName();
  }

  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    final XmlNSDescriptor descriptor = childTag.getNSDescriptor(childTag.getNamespace(), true);
    return descriptor == null ? null : descriptor.getElementDescriptor(childTag);
  }

  public XmlNSDescriptor getNSDescriptor() {
    return myNsDescriptor;
  }

  public int getContentType() {
    return 0;
  }

  public String getName(PsiElement context) {
    return getDefaultName();
  }

  public String getName() {
    return myLocalName;
  }

  public void init(PsiElement element) {
    throw new UnsupportedOperationException("Method init is not yet implemented in " + getClass().getName());
  }

  public Object[] getDependences() {
    throw new UnsupportedOperationException("Method getDependences is not yet implemented in " + getClass().getName());
  }

  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    final CommonProcessors.CollectProcessor<XmlElementDescriptor> processor = new CommonProcessors.CollectProcessor<XmlElementDescriptor>();
    myNsDescriptor.processElementDescriptors(null, myPlace, processor);
    final Collection<XmlElementDescriptor> results = processor.getResults();
    return results.toArray(new XmlElementDescriptor[results.size()]);
  }

  public PsiElement getDeclaration() {
    return myPlace;
  }

  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), null);
  }
}
