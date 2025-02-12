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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Sep 4, 2009
 * Time: 7:34:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidViewClassTagDescriptor implements XmlElementDescriptor {
  private final XmlElementDescriptor myParentDescriptor;
  private final PsiClass myViewClass;

  public AndroidViewClassTagDescriptor(PsiClass viewClass, XmlElementDescriptor parentDescriptor) {
    myParentDescriptor = parentDescriptor;
    myViewClass = viewClass;
  }

  public String getQualifiedName() {
    return getDefaultName();
  }

  public String getDefaultName() {
    String qualifiedName = myViewClass.getQualifiedName();
    return qualifiedName != null ? qualifiedName : myViewClass.getName();
  }

  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return myParentDescriptor.getElementsDescriptors(context);
  }

  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return myParentDescriptor.getElementDescriptor(childTag, contextTag);
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return myParentDescriptor.getAttributesDescriptors(context);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return myParentDescriptor.getAttributeDescriptor(attributeName, context);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return myParentDescriptor.getAttributeDescriptor(attribute);
  }

  public XmlNSDescriptor getNSDescriptor() {
    return myParentDescriptor.getNSDescriptor();
  }

  public int getContentType() {
    return myParentDescriptor.getContentType();
  }

  public PsiElement getDeclaration() {
    return myViewClass;
  }

  public String getName(PsiElement context) {
    return getDefaultName();
  }

  public String getName() {
    return getDefaultName();
  }

  public void init(PsiElement element) {
    myParentDescriptor.init(element);
  }

  public Object[] getDependences() {
    return myParentDescriptor.getDependences();
  }
}
