/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 */
public class PersistentObjectClassResolveConverter extends MappingClassResolveConverter {

  @Nullable
  protected String getBaseClassName(final ConvertContext context) {
    final DomElement domElement = context.getInvocationElement().getParent();
    if (domElement instanceof HbmClassBase) {
      final DomElement parent = domElement.getParent();
      final PsiClass superClass;
      if (parent instanceof HbmClassBase) {
        superClass = ((HbmClassBase)parent).getClazz().getValue();
      }
      else if (domElement instanceof HbmSubclass) {
        superClass = ((HbmSubclass)domElement).getExtends().getValue();
      }
      else if (domElement instanceof HbmJoinedSubclass) {
        superClass = ((HbmJoinedSubclass)domElement).getExtends().getValue();
      }
      else if (domElement instanceof HbmUnionSubclass) {
        superClass = ((HbmUnionSubclass)domElement).getExtends().getValue();
      }
      else {
        superClass = null;
      }
      if (superClass != null) return superClass.getQualifiedName();
    }
    return super.getBaseClassName(context);
  }

  public PsiClass fromString(final String s, final ConvertContext context) {
    final PsiClass psiClass = super.fromString(s, context);
    final HbmEmbeddedAttributeBase embedded;
    if (psiClass == null && (embedded = context.getInvocationElement().getParentOfType(HbmEmbeddedAttributeBase.class, true)) != null) {
      final PsiType entityType = PropertyUtil.getPropertyType(embedded.getPsiMember());
      return entityType instanceof PsiClassType ? ((PsiClassType)entityType).resolve() : null;
    }
    return psiClass;
  }
}
