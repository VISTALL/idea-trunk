/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
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

package com.intellij.struts.core;

import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts.StrutsIcons;
import com.intellij.struts.dom.FormProperty;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: DAvdeev
 * Date: 10.11.2005
 * Time: 15:08:37
 * To change this template use File | Settings | File Templates.
 */
public class PsiBeanPropertyImpl implements PsiBeanProperty, Comparable<PsiBeanProperty> {

  private final static Icon PROPERTY_READ = StrutsIcons.getIcon("propertyRead.png");
  private final static Icon PROPERTY_READ_STATIC = StrutsIcons.getIcon("propertyReadStatic.png");
  private final static Icon PROPERTY_READ_WRITE = StrutsIcons.getIcon("propertyReadWrite.png");
  private final static Icon PROPERTY_READ_WRITE_STATIC = StrutsIcons.getIcon("propertyReadWriteStatic.png");
  private final static Icon PROPERTY_WRITE = StrutsIcons.getIcon("propertyWrite.png");
  private final static Icon PROPERTY_WRITE_STATIC = StrutsIcons.getIcon("propertyWriteStatic.png");

  private final PsiElement[] myPsiElements;
  private final String myName;
  private final String myType;
  private final Icon myIcon;

  private final boolean myHasSetter;
  private final boolean myHasGetter;

  private PsiMethod myGetter;

  public PsiBeanPropertyImpl(@NonNls final String name, @NonNls final String type) {
    myPsiElements = null;
    myName = name;
    myType = type;
    myIcon = PROPERTY_READ_WRITE;
    myHasSetter = true;
    myHasGetter = true;
  }

  @Nullable
  public static PsiBeanProperty create(final FormProperty formProperty) {
    final String name = formProperty.getName().getStringValue();
    return name == null ? null : new PsiBeanPropertyImpl(formProperty);
  }

  private PsiBeanPropertyImpl(final FormProperty formProperty) {
    myName = formProperty.getName().getStringValue();
    final PsiType type = formProperty.getType().getValue();
    if (type != null) {
      myType = type.getCanonicalText();
    }
    else {
      myType = null;
    }
    final XmlTag xmlTag = formProperty.getName().getXmlTag();
    myPsiElements = xmlTag == null ? null : new PsiElement[]{xmlTag};
    myIcon = PROPERTY_READ_WRITE;
    myHasSetter = true;
    myHasGetter = true;
  }

  public PsiBeanPropertyImpl(final PsiClass clazz, final String name, @Nullable final PsiField field) {
    myName = name;
    String myType = null;
    myGetter = PropertyUtil.findPropertyGetter(clazz, name, false, true);
    PsiMethod setter = PropertyUtil.findPropertySetter(clazz, name, false, true);
    if (myGetter != null || setter != null) {
      if (myGetter != null && setter != null) {
        myIcon = PROPERTY_READ_WRITE;
        myHasSetter = true;
        myHasGetter = true;
      }
      else if (myGetter != null) {
        myIcon = PROPERTY_READ;
        myHasGetter = true;
        myHasSetter = false;
      }
      else {
        myIcon = PROPERTY_WRITE;
        myHasSetter = true;
        myHasGetter = false;
      }
    }
    else {
      myGetter = PropertyUtil.findPropertyGetter(clazz, name, true, true);
      setter = PropertyUtil.findPropertySetter(clazz, name, true, true);
      if (myGetter != null && setter != null) {
        myIcon = PROPERTY_READ_WRITE_STATIC;
        myHasSetter = true;
        myHasGetter = true;
      }
      else if (myGetter != null) {
        myIcon = PROPERTY_READ_STATIC;
        myHasGetter = true;
        myHasSetter = false;
      }
      else {
        myIcon = PROPERTY_WRITE_STATIC;
        myHasSetter = true;
        myHasGetter = false;
      }
    }
    final ArrayList<PsiElement> elements = new ArrayList<PsiElement>();
    if (field != null) {
      elements.add(field);
      myType = field.getType().getPresentableText();
    }
    if (myGetter != null) {
      elements.add(myGetter);
      final PsiType returnType = myGetter.getReturnType();
      assert returnType != null;
      myType = returnType.getPresentableText();
    }
    if (setter != null) {
      elements.add(setter);
      myType = setter.getParameterList().getParameters()[0].getType().getPresentableText();
    }
    myPsiElements = elements.toArray(new PsiElement[elements.size()]);
    this.myType = myType;
  }


  public PsiElement[] getPsiElements() {
    return myPsiElements;
  }

  public Icon getIcon() {
    return myIcon;
  }

  public String getName() {
    return myName;
  }

  public String getType() {
    return myType;
  }

  public boolean hasGetter() {
    return myHasGetter;
  }

  public PsiMethod getGetter() {
    return myGetter;
  }

  public boolean hasSetter() {
    return myHasSetter;
  }

  public String toString() {
    return myName + " (" + myType + ")";
  }

  public int compareTo(final PsiBeanProperty o) {
    return myName.compareTo(o.getName());
  }
}
