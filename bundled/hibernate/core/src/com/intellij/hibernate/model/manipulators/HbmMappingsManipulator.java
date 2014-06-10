/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.manipulators;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.hibernate.view.HibernateIcons;
import com.intellij.jpa.model.manipulators.MappingsManipulatorBase;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.model.PersistentEmbeddable;
import com.intellij.persistence.model.PersistentEntity;
import com.intellij.persistence.model.PersistentSuperclass;
import com.intellij.persistence.model.manipulators.PersistenceAction;
import com.intellij.persistence.model.manipulators.PersistenceMappingsManipulator;
import com.intellij.persistence.model.manipulators.PersistenceUnitManipulator;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.IncorrectOperationException;
import com.intellij.jam.reflect.JamClassMeta;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class HbmMappingsManipulator extends MappingsManipulatorBase<HbmHibernateMapping> implements PersistenceMappingsManipulator<HbmHibernateMapping>{
  public HbmMappingsManipulator(final HbmHibernateMapping target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {
    return Arrays.<PersistenceAction>asList(
      new MyObjectAction<HbmClass>(this, HibernateMessages.message("action.name.create.hibernate.class"),
                               HibernateIcons.CLASS_ICON, HbmClass.class, null, HibernateMessages.message("type.hibernate.class")),
      new MyObjectAction<HbmSubclass>(this, HibernateMessages.message("action.name.create.hibernate.subclass"),
                               HibernateIcons.CLASS_ICON, HbmSubclass.class, null, HibernateMessages.message("type.hibernate.subclass")),
      new MyObjectAction<HbmJoinedSubclass>(this, HibernateMessages.message("action.name.create.hibernate.joined.subclass"),
                               HibernateIcons.CLASS_ICON, HbmJoinedSubclass.class, null, HibernateMessages.message("type.hibernate.joined.subclass")),
      new MyObjectAction<HbmUnionSubclass>(this, HibernateMessages.message("action.name.create.hibernate.union.subclass"),
                               HibernateIcons.CLASS_ICON, HbmUnionSubclass.class, null, HibernateMessages.message("type.hibernate.union.subclass"))
      //new MyObjectAction<HbmComponent>(this, HibernateMessages.message("action.name.create.hibernate.component"),
      //                         HibernateIcons.CLASS_ICON, HbmComponent.class, null, HibernateMessages.message("type.hibernate.component"))
      //new MyObjectAction<HbmJoin>(this, HibernateMessages.message("action.name.create.hibernate.join"),
      //                         HibernateIcons.CLASS_ICON, HbmJoin.class, null, HibernateMessages.message("type.hibernate.join"))
    );
  }


  protected <T> T getOrCreateModelObject(final PersistencePackage unit, final Class<T> aClass, final JamClassMeta<?> meta, final PsiClass psiClass) {
    final HbmPersistentObjectBase object;
    if (aClass == HbmClass.class) {
      object = getManipulatorTarget().addClass();
    }
    else if (aClass == HbmSubclass.class) {
      object = getManipulatorTarget().addSubclass();
    }
    else if (aClass == HbmJoinedSubclass.class) {
      object = getManipulatorTarget().addJoinedSubclass();
    }
    else if (aClass == HbmUnionSubclass.class) {
      object = getManipulatorTarget().addUnionSubclass();
    }
    else {
      throw new AssertionError("invalid class");
    }
    object.getClazz().setValue(psiClass);
    if (unit != null) {
      final PersistenceUnitManipulator manipulator = PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(unit, PersistenceUnitManipulator.class);
      if (manipulator != null) {
        manipulator.ensureMappingIncluded(getManipulatorTarget());
      }
    }
    return (T)object;
  }

  public PersistentEntity addEntity(final PsiDirectory directory, final PersistencePackage unit, final String shortClassName, final String entityName)
    throws IncorrectOperationException {
    return addPersistentObject(directory, unit, HbmClass.class, null, shortClassName);
  }

  public PersistentSuperclass addSuperclass(final PsiDirectory directory, final PersistencePackage unit, final String shortClassName)
    throws IncorrectOperationException {
    final HbmClass hbmClass = addPersistentObject(directory, unit, HbmClass.class, null, shortClassName);
    hbmClass.getAbstract().setValue(Boolean.TRUE);
    return hbmClass;
  }

  public PersistentEmbeddable addEmbeddable(final PsiDirectory directory, final PersistencePackage unit, final String shortClassName)
    throws IncorrectOperationException {
    throw new UnsupportedOperationException("HibernateMappings.addEmbeddable is not supported");
  }
}
