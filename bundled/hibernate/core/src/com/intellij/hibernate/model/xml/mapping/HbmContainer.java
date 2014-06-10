/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.AttributeMemberConverter;
import com.intellij.hibernate.model.converters.CascadeTypeListConverter;
import com.intellij.hibernate.model.converters.LazyTypeConverter;
import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.enums.CascadeType;
import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.psi.PsiMember;
import com.intellij.util.xml.*;

import java.util.List;

/**
 * @author Gregory.Shrago
 */
public interface HbmContainer extends CommonModelElement, HbmTableInfoProvider {

  HbmCollectionAttributeBase getContainedAttribute();

  HbmKey getKey();

  @Required
  @NameValue
  @Attribute("name")
  @Convert(AttributeMemberConverter.class)
  GenericAttributeValue<PsiMember> getTargetMember();

  GenericAttributeValue<AccessType> getAccess();

  // skip for array & primitive array
  @Convert(LazyTypeConverter.class)
  GenericAttributeValue<LazyType> getLazy();


  // skip for primitive array
  @Convert(CascadeTypeListConverter.class)
  GenericAttributeValue<List<CascadeType>> getCascade();

  HbmElement getElement();

  // skip for PrimitiveArray
  HbmCompositeElement getCompositeElement();
  HbmManyToMany getManyToMany();
  HbmManyToAny getManyToAny();
  HbmOneToMany getOneToMany();

  // skip for PrimitiveArray and IdBag
  GenericAttributeValue<Boolean> getInverse();
}
