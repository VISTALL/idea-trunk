/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.manipulators;

import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.jpa.model.manipulators.AttributeManipulatorBase;
import com.intellij.persistence.database.DatabaseColumnInfo;
import com.intellij.persistence.database.DatabaseTableInfo;
import com.intellij.persistence.model.manipulators.PersistentRelationshipAttributeManipulator;
import com.intellij.util.xml.DomElement;

import java.util.Collections;
import java.util.Map;

/**
 * @author Gregory.Shrago
 */
public class HibernateRelationshipAttributeManipulator extends AttributeManipulatorBase<HbmRelationAttributeBase> implements
                                                                                                                  PersistentRelationshipAttributeManipulator<HbmRelationAttributeBase> {
  public HibernateRelationshipAttributeManipulator(final HbmRelationAttributeBase target) {
    super(target);
  }

  public void setMappedByAndInverse(final String name, final boolean inverse) {
    final HbmRelationAttributeBase attribute = getManipulatorTarget();
    final HbmContainer attributeHolder = attribute.getParentOfType(HbmContainer.class, false);
    if (attributeHolder != null &&  !(attributeHolder instanceof HbmPrimitiveArray || attributeHolder instanceof HbmIdbag)) {
      attributeHolder.getInverse().setValue(inverse);
    }
    if (attribute instanceof HbmRelationAttributeBase.NonOneToManyBase) {
      ((HbmRelationAttributeBase.NonOneToManyBase)attribute).getPropertyRef().setStringValue(name);
    }
  }

  public void setMapKeyColumn(final String attributeName, final DatabaseColumnInfo columnInfo, final boolean inverse) {
    assert getManipulatorTarget().getParent() instanceof HbmMap;
    final HbmMap hbmMap = (HbmMap)getManipulatorTarget().getParent();
    assert hbmMap != null;
    HibernateObjectManipulator.setColumns(hbmMap.getMapKey(), Collections.singletonList(columnInfo), false, true);
  }

  public void setJoinTable(final boolean inverse, final DatabaseTableInfo joinTable, final Map<DatabaseColumnInfo, DatabaseColumnInfo> sourceColumnsMap,
                           final Map<DatabaseColumnInfo, DatabaseColumnInfo> targetColumnsMap) {
    final HbmRelationAttributeBase target = getManipulatorTarget();
    final DomElement parent = target.getParent();
    if (parent instanceof HbmContainer) {
      final HbmContainer hbmContainer = (HbmContainer)parent;
      hbmContainer.getTableName().setValue(joinTable.getName());
      hbmContainer.getSchema().setValue(joinTable.getSchema());
      hbmContainer.getCatalog().setValue(joinTable.getCatalog());
      HibernateObjectManipulator.setColumns(hbmContainer.getKey(), sourceColumnsMap.keySet(), false, true);
      if (target instanceof HbmColumnsHolderBase) {
        HibernateObjectManipulator.setColumns((HbmColumnsHolderBase)target, targetColumnsMap.keySet(), false, true);
      }
    }
    else {
      // omg! hibernate allows it in a wierd way
      assert parent instanceof HbmClass;
      final HbmJoin join = ((HbmClass)parent).addJoin();
      final HbmManyToOne toOne = join.addManyToOne();
      join.getOptional().setValue(Boolean.TRUE);
      toOne.getTargetMember().setStringValue(target.getTargetMember().getStringValue());
      toOne.getCascade().setStringValue(target.getCascade().getStringValue());
      toOne.getLazy().setStringValue(target.getLazy().getStringValue());
      toOne.getClazz().setStringValue(target.getClazz().getStringValue());
      join.getTableName().setValue(joinTable.getName());
      join.getSchema().setValue(joinTable.getSchema());
      join.getCatalog().setValue(joinTable.getCatalog());
      HibernateObjectManipulator.setColumns(join.getKey(), sourceColumnsMap.keySet(), false, true);
      if (target instanceof HbmColumnsHolderBase) {
        HibernateObjectManipulator.setColumns((HbmColumnsHolderBase)target, targetColumnsMap.keySet(), false, true);
      }
      target.undefine();
    }
  }

  public void setJoinColumns(final boolean inverse, final Map<DatabaseColumnInfo, DatabaseColumnInfo> columnsMap) {
    final DomElement parent = getManipulatorTarget().getParent();
    if (parent instanceof HbmContainer) {
      final HbmContainer hbmContainer = (HbmContainer)parent;
      HibernateObjectManipulator.setColumns(hbmContainer.getKey(), columnsMap.values(), false, true);
    }
    else if (getManipulatorTarget() instanceof HbmColumnsHolderBase) {
      HibernateObjectManipulator.setColumns((HbmColumnsHolderBase)getManipulatorTarget(), columnsMap.keySet(), false,
                                            true);
    }
  }
}
