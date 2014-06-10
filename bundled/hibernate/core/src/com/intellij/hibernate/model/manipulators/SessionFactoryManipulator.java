/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.manipulators;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.hibernate.model.HibernatePropertiesConstants;
import com.intellij.hibernate.model.xml.config.Property;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.model.xml.mapping.HbmHibernateMapping;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.jpa.model.manipulators.JpaUnitManipulator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.database.DatabaseConnectionInfo;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.manipulators.AbstractPersistenceManipulator;
import com.intellij.persistence.model.manipulators.PersistenceAction;
import com.intellij.persistence.model.manipulators.PersistenceUnitManipulator;
import com.intellij.psi.*;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.GenericValueUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class SessionFactoryManipulator extends AbstractPersistenceManipulator<SessionFactory> implements PersistenceUnitManipulator<SessionFactory> {
  public SessionFactoryManipulator(final SessionFactory target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {
    final ArrayList<PersistenceAction> result = new ArrayList<PersistenceAction>();
    return getCreateActionsDefault(this, result);
  }

  public static List<PersistenceAction> getCreateActionsDefault(final PersistenceUnitManipulator unitManipulator, final ArrayList<PersistenceAction> result) {
    result.add(new JpaUnitManipulator.MyMappingsAction(unitManipulator, HibernateMessages.message("action.name.create.mapping"),
                                                       HibernateMessages.message("type.hibernate.mappings"),
                                                       HibernateDescriptorsConstants.HIBERNATE_MAPPING_META_DATA.getDefaultVersion().getTemplateName(),
                                                       HbmHibernateMapping.class));
    return JpaUnitManipulator.getCreateActionsDefault(unitManipulator, result);
  }

  public void ensureMappingIncluded(final PersistenceMappings mappings) {
    if (mappings instanceof DomElement && !GenericValueUtil.containsValue(getManipulatorTarget().getModelHelper().getMappingFiles(PersistenceMappings.class), mappings)) {
      getManipulatorTarget().addMapping().getResource().setValue(mappings.getContainingFile());
    }
  }

  public void ensureClassIncluded(final PsiClass psiClass) {
    if (!GenericValueUtil.containsValue(getManipulatorTarget().getModelHelper().getClasses(), psiClass)) {
      final String packageName = ((PsiJavaFile)psiClass.getContainingFile()).getPackageName();
      final PsiManager manager = PsiManager.getInstance(psiClass.getProject());
      final PsiPackage psiPackage = JavaPsiFacade.getInstance(manager.getProject()).findPackage(packageName);
      for (GenericValue<PsiPackage> pkgValue : getManipulatorTarget().getModelHelper().getPackages()) {
        if (manager.areElementsEquivalent(psiPackage, pkgValue.getValue())) {
          return;
        }
      }
      getManipulatorTarget().addMapping().getClazz().setValue(psiClass);
    }
  }

  public void setConnectionProperties(final DatabaseConnectionInfo info) {
    final SessionFactory factory = getManipulatorTarget();

    HibernateUtil.setSessionFactoryProperty(factory, HibernatePropertiesConstants.URL, info.getUrl());
    HibernateUtil.setSessionFactoryProperty(factory, HibernatePropertiesConstants.DRIVER, info.getDriverClass());
    HibernateUtil.setSessionFactoryProperty(factory, HibernatePropertiesConstants.USER, info.getUsername());
    HibernateUtil.setSessionFactoryProperty(factory, HibernatePropertiesConstants.PASS, info.getPassword());

    Property<Object> dialect = HibernateUtil.findSessionFactoryProperty(factory, HibernatePropertiesConstants.DIALECT);
    if (dialect == null) {
      dialect = factory.addProperty();
      final String dialectValue = HibernateUtil.getDefaultDialectValue(dialect.getXmlElement(), info.getUrl());
      if (StringUtil.isNotEmpty(dialectValue)) {
        dialect.getName().setStringValue(HibernatePropertiesConstants.DIALECT);
        HibernateUtil.setSessionFactoryProperty(factory, HibernatePropertiesConstants.DIALECT, dialectValue);
      }
      else {
        dialect.undefine();
      }
    }
  }

}
