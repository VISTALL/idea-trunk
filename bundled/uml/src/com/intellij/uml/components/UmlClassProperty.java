/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.components;

import com.intellij.ide.structureView.impl.java.PropertyGroup;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.uml.utils.UmlIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlClassProperty {
  private final PsiClass myClass;
  private final PsiMethod property;
  private final boolean isStatic;

  public UmlClassProperty(final PsiClass psiClass, final PsiMethod property) {
    myClass = psiClass;
    this.property = property;
    isStatic = property.hasModifierProperty(PsiModifier.STATIC);
  }

  @Nullable
  public PsiType getType() {
    return PropertyUtil.getPropertyType(property);
  }

  public String getName() {
    final String propertyName = PropertyUtil.getPropertyName(property);
    return propertyName == null ? "" : propertyName;
  }

  public Icon getIcon() {
    final String name = getName();
    boolean read = PropertyUtil.findPropertyGetter(myClass, name, isStatic, true) != null;
    boolean write = PropertyUtil.findPropertySetter(myClass, name, isStatic, true) != null;

    if (isStatic & read & write) return PropertyGroup.PROPERTY_READ_WRITE_STATIC_ICON;
    if (isStatic & read & !write) return PropertyGroup.PROPERTY_READ_STATIC_ICON;
    if (isStatic & !read & write) return PropertyGroup.PROPERTY_WRITE_STATIC_ICON;
    if (!isStatic & read & write) return PropertyGroup.PROPERTY_READ_WRITE_ICON;
    if (!isStatic & read & !write) return PropertyGroup.PROPERTY_READ_ICON;
    if (!isStatic & !read & write) return PropertyGroup.PROPERTY_WRITE_ICON;

    return UmlIcons.PROPERTY;
  }

  public boolean isDeprecated() {
    return property.isDeprecated();
  }
}
