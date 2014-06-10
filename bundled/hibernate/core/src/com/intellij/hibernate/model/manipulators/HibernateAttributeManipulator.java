/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.manipulators;

import com.intellij.hibernate.model.xml.mapping.HbmAttributeBase;
import com.intellij.jpa.model.manipulators.AttributeManipulatorBase;

/**
 * @author Gregory.Shrago
 */
public class HibernateAttributeManipulator extends AttributeManipulatorBase<HbmAttributeBase> {
  public HibernateAttributeManipulator(final HbmAttributeBase target) {
    super(target);
  }

}