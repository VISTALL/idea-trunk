/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.xml.mapping.HbmClassBase;
import com.intellij.hibernate.model.xml.mapping.HbmHibernateMapping;
import com.intellij.javaee.model.xml.persistence.mapping.EntityNameConverter;
import com.intellij.persistence.model.PersistentEntity;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericValue;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 */
public class HbmClassEntityNameConverter extends EntityNameConverter {

  @Nullable
  protected String getEntityName(final PersistentEntity entity) {
    HbmClassBase classBase = (HbmClassBase)entity;
    final HbmHibernateMapping mapping = classBase.getParentOfType(HbmHibernateMapping.class, true);
    if (mapping != null && Boolean.FALSE.equals(mapping.getAutoImport().getValue())) {
      final GenericValue<PsiClass> classValue = entity.getClazz();
      final PsiClass aClass = classValue.getValue();
      return aClass != null? aClass.getQualifiedName() : classValue.getStringValue();
    }
    else {
      return super.getEntityName(entity);
    }
  }

}