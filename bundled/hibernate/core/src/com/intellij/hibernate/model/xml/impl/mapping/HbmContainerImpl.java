/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.xml.mapping.HbmCollectionAttributeBase;
import com.intellij.hibernate.model.xml.mapping.HbmContainer;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementVisitor;
import com.intellij.util.xml.DomUtil;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmContainerImpl extends BaseImpl implements HbmContainer {
  public HbmCollectionAttributeBase getContainedAttribute() {
    final Ref<HbmCollectionAttributeBase> result = new Ref<HbmCollectionAttributeBase>();
    DomUtil.acceptAvailableChildren(this, new DomElementVisitor() {
      public void visitDomElement(final DomElement element) {
        if (result.get() == null && element instanceof HbmCollectionAttributeBase) {
          result.set((HbmCollectionAttributeBase)element);
        }
      }
    });
    return result.get();
  }
}
