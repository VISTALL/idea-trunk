/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.model.HibernatePropertiesConstants;
import com.intellij.hibernate.model.manipulators.SessionFactoryManipulator;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.persistence.database.DatabaseConnectionInfo;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.manipulators.AbstractPersistenceManipulator;
import com.intellij.persistence.model.manipulators.PersistenceAction;
import com.intellij.persistence.model.manipulators.PersistenceUnitManipulator;
import com.intellij.psi.*;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.GenericValueUtil;
import com.intellij.openapi.util.text.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class SpringBeanPersistenceUnitManipulator extends AbstractPersistenceManipulator<SpringBeanPersistenceUnit> implements
                                                                                                                    PersistenceUnitManipulator<SpringBeanPersistenceUnit> {
  public SpringBeanPersistenceUnitManipulator(final SpringBeanPersistenceUnit target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {
    return SessionFactoryManipulator.getCreateActionsDefault(this, new ArrayList<PersistenceAction>());
  }

  public void ensureMappingIncluded(final PersistenceMappings mappings) {
    if (mappings instanceof DomElement && !GenericValueUtil.containsValue(getManipulatorTarget().getModelHelper().getMappingFiles(PersistenceMappings.class), mappings)) {
      final SpringBean bean = getManipulatorTarget().getBean();
      SpringPropertyDefinition propertyDef = SpringUtils.findPropertyByName(bean, SpringBeanPersistenceUnit.MAPPING_LOCATIONS, false);
      propertyDef = propertyDef != null? propertyDef : SpringUtils.findPropertyByName(bean, SpringBeanPersistenceUnit.CACHEABLE_MAPPING_LOCATIONS, false);
      propertyDef = propertyDef != null? propertyDef : SpringUtils.findPropertyByName(bean, SpringBeanPersistenceUnit.MAPPING_RESOURCES, false);
      final String fileReferenceString = ResourceResolverUtils.getResourceFileReferenceString(mappings.getContainingFile());
      IntegrationUtil.setBeanProperty(bean, propertyDef, SpringBeanPersistenceUnit.MAPPING_LOCATIONS, fileReferenceString, true);
    }
  }

  public void ensureClassIncluded(final PsiClass psiClass) {
    if (!getManipulatorTarget().supportsAnnotations()) return;
    if (!GenericValueUtil.containsValue(getManipulatorTarget().getModelHelper().getClasses(), psiClass)) {
      final String packageName = ((PsiJavaFile)psiClass.getContainingFile()).getPackageName();
      final PsiManager manager = PsiManager.getInstance(psiClass.getProject());
      final PsiPackage psiPackage = JavaPsiFacade.getInstance(manager.getProject()).findPackage(packageName);
      for (GenericValue<PsiPackage> pkgValue : getManipulatorTarget().getModelHelper().getPackages()) {
        if (manager.areElementsEquivalent(psiPackage, pkgValue.getValue())) {
          return;
        }
      }
      final SpringBean bean = getManipulatorTarget().getBean();
      IntegrationUtil.setBeanProperty(bean, SpringBeanPersistenceUnit.ANNOTATED_CLASSES, null, psiClass.getQualifiedName(), true);
    }
  }

  public void setConnectionProperties(final DatabaseConnectionInfo info) {
    final SpringBeanPersistenceUnit unit = getManipulatorTarget();
    final SpringBean bean = unit.getBean();
    SpringPropertyDefinition propertyDef = SpringUtils.findPropertyByName(bean, SpringBeanPersistenceUnit.HIBERNATE_PROPERTIES, false);
    if (!(propertyDef instanceof SpringProperty)) {
      if (propertyDef != null) propertyDef.undefine();
      final SpringProperty property = bean.addProperty();
      property.getName().setStringValue(SpringBeanPersistenceUnit.HIBERNATE_PROPERTIES);
      propertyDef = property;
    }
    final SpringProperty hibernateProperties = (SpringProperty)propertyDef;

    IntegrationUtil.setBeanProperty(hibernateProperties.getProps(), HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.URL), HibernatePropertiesConstants.URL, info.getUrl());
    IntegrationUtil.setBeanProperty(hibernateProperties.getProps(), HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.DRIVER), HibernatePropertiesConstants.DRIVER, info.getDriverClass());
    IntegrationUtil.setBeanProperty(hibernateProperties.getProps(), HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.USER), HibernatePropertiesConstants.USER, info.getUsername());
    IntegrationUtil.setBeanProperty(hibernateProperties.getProps(), HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.PASS), HibernatePropertiesConstants.PASS, info.getPassword());

    Prop dialect = IntegrationUtil.findProp(hibernateProperties.getProps(), HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.DIALECT), HibernatePropertiesConstants.DIALECT);
    if (dialect == null) {
      dialect = hibernateProperties.getProps().addProp();
      final String dialectValue = HibernateUtil.getDefaultDialectValue(dialect.getXmlElement(), info.getUrl());
      if (StringUtil.isNotEmpty(dialectValue)) {
        dialect.getKey().setStringValue(HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.DIALECT));
        dialect.setStringValue(dialectValue);
      }
      else {
        dialect.undefine();
      }
    }
  }
}
