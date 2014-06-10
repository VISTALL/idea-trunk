/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.hibernate;

import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.hibernate.model.xml.impl.mapping.*;
import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.highlighting.HibernateDomAnnotator;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.util.xml.DomFileDescription;
import com.intellij.util.xml.highlighting.DomElementsAnnotator;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.psi.xml.XmlFile;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
*/
public class HibernateMappingDomFileDescription extends DomFileDescription<HbmHibernateMapping> {
  public HibernateMappingDomFileDescription() {
    super(HbmHibernateMapping.class, HibernateConstants.HBM_XML_ROOT_TAG);
  }

  protected void initializeFileDescription() {
    registerImplementation(CommonDomModelElement.class, BaseImpl.class);
    registerImplementation(HbmHibernateMapping.class, HbmHibernateMappingImpl.class);
    registerImplementation(HbmPersistentObjectBase.class, HbmPersistentObjectBaseImpl.class);
    registerImplementation(HbmClassBase.class, HbmClassBaseImpl.class);
    registerImplementation(HbmAttributeBase.class, HbmAttributeBaseImpl.class);
    registerImplementation(HbmEmbeddedAttributeBase.class, HbmEmbeddedAttributeBaseImpl.class);
    registerImplementation(HbmCompositeElement.class, HbmCompositeElementImpl.class);
    registerImplementation(HbmCollectionAttributeBase.class, HbmCollectionAttributeBaseImpl.class);
    registerImplementation(HbmContainer.class, HbmContainerImpl.class);
    registerImplementation(HbmRelationAttributeBase.AnyToManyBase.class, HbmAnyToManyImpl.class);
    registerImplementation(HbmRelationAttributeBase.AnyToOneBase.class, HbmAnyToOneImpl.class);
    registerImplementation(HbmGenerator.class, HbmGeneratorImpl.class);
    registerImplementation(HbmQuery.class, HbmQueryImpl.class);
    registerImplementation(HbmSqlQuery.class, HbmSqlQueryImpl.class);
  }

  public DomElementsAnnotator createAnnotator() {
    return new HibernateDomAnnotator();
  }

  public boolean isMyFile(@NotNull XmlFile file, final Module module) {
    return HibernateUtil.isHibernateMapping(file, module);
  }

  public boolean isAutomaticHighlightingEnabled() {
    return false;
  }
}
