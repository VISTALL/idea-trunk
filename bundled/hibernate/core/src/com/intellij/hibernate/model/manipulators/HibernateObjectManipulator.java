/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.manipulators;

import com.intellij.hibernate.model.enums.CascadeType;
import com.intellij.hibernate.model.enums.HibernateAttributeType;
import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.hibernate.model.enums.NotFoundType;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.javaee.model.common.persistence.mapping.AttributeType;
import com.intellij.jpa.model.manipulators.ObjectManipulatorBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.persistence.database.DatabaseColumnInfo;
import com.intellij.persistence.database.DatabaseTableInfo;
import com.intellij.persistence.model.*;
import com.intellij.persistence.model.manipulators.PersistenceAction;
import com.intellij.persistence.model.manipulators.PersistentObjectManipulator;
import com.intellij.persistence.model.manipulators.PersistentRelationshipAttributeManipulator;
import com.intellij.persistence.util.JavaContainerType;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class HibernateObjectManipulator extends ObjectManipulatorBase<HbmPersistentObjectBase> {
  public HibernateObjectManipulator(final HbmPersistentObjectBase target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {
    final ArrayList<PersistenceAction> result = new ArrayList<PersistenceAction>();
    for (AttributeType type : AttributeType.values()) {
      if (type instanceof HibernateAttributeType &&
          ReflectionCache.isAssignable(DomElement.class, type.getAttributeClass())) {
        result.add(new MyDomAttributeAction(this, (HibernateAttributeType)type));
      }
    }
    return result;
  }

  public void setTable(final DatabaseTableInfo tableInfo) throws IncorrectOperationException {
    final HbmPersistentObjectBase target = getManipulatorTarget();
    assert target instanceof HbmTableInfoProvider;
    final HbmTableInfoProvider tableInfoProvider = (HbmTableInfoProvider)target;
    tableInfoProvider.getTableName().setStringValue(tableInfo.getName());
    tableInfoProvider.getSchema().setStringValue(tableInfo.getSchema());
    if (StringUtil.isNotEmpty(tableInfo.getCatalog())) {
      tableInfoProvider.getCatalog().setStringValue(tableInfo.getCatalog());
    }
  }

  public PersistentEmbeddedAttribute addEmbeddedAttribute(final PersistentEmbeddable embeddable, final String attributeName, final PropertyMemberType accessMode)
    throws IncorrectOperationException {
    final HbmPersistentObjectBase object = getManipulatorTarget();
    assert object instanceof HbmClassBase;
    final HbmComponent component = ((HbmClassBase)object).addComponent();
    component.getName().setValue(attributeName);
    component.getClazz().setValue(embeddable.getClazz().getValue());

    if (ensureClassExists() == null) return component;
    final PersistentObjectManipulator<PersistentEmbeddable> manipulator =
      PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(embeddable, PersistentObjectManipulator.class);
    final PsiClass embeddableClass;
    if (manipulator == null || (embeddableClass = manipulator.ensureClassExists()) == null) return component;
    component.getClazz().setValue(embeddable.getClazz().getValue());
    ensurePropertyExists(getManipulatorTarget().getClazz().getValue(), attributeName,
                         JavaPsiFacade.getInstance(embeddableClass.getProject()).getElementFactory().createType(embeddableClass),
                         accessMode, PsiAnnotation.EMPTY_ARRAY);
    return component;
  }

  public PersistentRelationshipAttribute addRelationshipAttribute(final PersistentEntityBase entity, final RelationshipType relationshipType,
                                                                  final JavaContainerType containerType, final String attributeName, final String targetAttributeName,
                                                                  final boolean inverse,
                                                                  final boolean optional,
                                                                  final String fetchType,
                                                                  final Collection<String> cascadeVariants,
                                                                  final PropertyMemberType accessMode) throws IncorrectOperationException {
    final HbmPersistentObjectBase object = getManipulatorTarget();
    assert object instanceof HbmClassBase;
    final HbmClassBase thisEntity = (HbmClassBase)object;
    final HbmRelationAttributeBase attrBase;
    switch (relationshipType) {
      case MANY_TO_MANY:
        attrBase = addContainer(thisEntity, containerType, false).getManyToMany();
        break;
      case MANY_TO_ONE:
        attrBase = thisEntity.addManyToOne();
        break;
      case ONE_TO_MANY:
        attrBase = addContainer(thisEntity, containerType, false).getOneToMany();
        break;
      case ONE_TO_ONE:
        attrBase = thisEntity.addOneToOne();
        break;
      default: throw new AssertionError();
    }
    attrBase.getName().setValue(attributeName);
    final List<CascadeType> cascadeTypesValue = new ArrayList<CascadeType>();
    for (CascadeType type : CascadeType.values()) {
      if (cascadeVariants.contains(type.getDisplayName())) {
        cascadeTypesValue.add(type);
      }
    }
    if (!cascadeTypesValue.isEmpty()) {
      attrBase.getCascade().setValue(cascadeTypesValue);
    }
    if (fetchType != null) {
      for (LazyType type : LazyType.values()) {
        if (type.getDisplayName().equals(fetchType)) {
          attrBase.getLazy().setValue(type);
          break;
        }
      }
    }
    final boolean sameAttr = thisEntity == entity && attributeName.equals(targetAttributeName);
    if (optional && attrBase instanceof HbmRelationAttributeBase.AnyToManyBase) {
      ((HbmRelationAttributeBase.AnyToManyBase)attrBase).getNotFound().setValue(optional? NotFoundType.IGNORE : NotFoundType.EXCEPTION);
    }
    //if ((sameAttr || inverse) && targetAttributeName != null && attrBase instanceof HbmRelationAttributeBase.NonOneToManyBase) {
    //  ((HbmRelationAttributeBase.NonOneToManyBase)attrBase).getPropertyRef().setStringValue(targetAttributeName);
    //}
    if (inverse && !sameAttr) {
      final DomElement parent = attrBase.getParent();
      if (parent instanceof HbmContainer && !(parent instanceof HbmPrimitiveArray || parent instanceof HbmIdbag)) {
        ((HbmContainer)parent).getInverse().setValue(Boolean.TRUE);
      }
    }

    final PsiClass curClass;
    if ((curClass = ensureClassExists()) == null) return attrBase;
    final PersistentObjectManipulator<PersistentEntityBase> manipulator =
      PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(entity, PersistentObjectManipulator.class);
    final PsiClass entityClass;
    if (manipulator == null || (entityClass = manipulator.ensureClassExists()) == null) return attrBase;
    attrBase.getTargetEntityClass().setValue(entityClass);

    final PsiClassType entityClassType = JavaPsiFacade.getInstance(curClass.getProject()).getElementFactory().createType(entityClass);
    final PsiType propertyType = relationshipType.isMany(false) ? containerType.createCollectionType(curClass, entityClassType, null) : entityClassType;

    ensurePropertyExists(curClass, attributeName, propertyType,
                         accessMode, PsiAnnotation.EMPTY_ARRAY);
    return attrBase;
  }

  @NotNull
  private static HbmContainer addContainer(final HbmPersistentObjectBaseEx thisEntity, final JavaContainerType containerType, final boolean isPrimitive) {
    switch (containerType) {
      case ARRAY:
        return isPrimitive? thisEntity.addPrimitiveArray() : thisEntity.addArray();
      case COLLECTION:
        return thisEntity.addSet();
      case LIST:
        return thisEntity.addList();
      case MAP:
        return thisEntity.addMap();
      case SET:
        return thisEntity.addSet();
    }
    throw new AssertionError(containerType);
  }

  public void addCascadeVariants(final Collection<String> cascadeVariants) {
    for (CascadeType type : CascadeType.values()) {
      cascadeVariants.add(type.getValue());
    }
  }

  public void addFetchVariants(final Collection<String> fetchVariants) {
    for (LazyType type : LazyType.values()) {
      fetchVariants.add(type.getDisplayName());
    }
  }

  public PersistentAttribute addAttribute(final String name, final PsiType attributeType, final PropertyMemberType accessType,
                                          final Collection<? extends DatabaseColumnInfo> columns) throws IncorrectOperationException {
    final HbmPersistentObjectBase object = getManipulatorTarget();
    assert object instanceof HbmClassBase;
    final HbmClassBase thisEntity = (HbmClassBase)object;
    final HbmProperty attrBase = thisEntity.addProperty();
    attrBase.getName().setValue(name);
    setColumns(attrBase, columns, true, isGenerateColumnProperties());

    final PsiClass curClass;
    if ((curClass = ensureClassExists()) == null) return attrBase;

    ensurePropertyExists(curClass, name, attributeType, accessType, PsiAnnotation.EMPTY_ARRAY);
    return attrBase;
  }

  public PersistentAttribute addIdAttribute(final boolean compositeId, final String name, final PsiType attributeType, final PropertyMemberType accessType,
                                            final Collection<? extends DatabaseColumnInfo> columns) throws IncorrectOperationException {
    final HbmPersistentObjectBase object = getManipulatorTarget();
    assert object instanceof HbmClass;
    final HbmClass thisEntity = (HbmClass)object;
    final HbmPropertyBase attrBase = compositeId? thisEntity.getCompositeId().addProperty() : thisEntity.getId();
    if (compositeId) {
      thisEntity.getCompositeId().getMapped().setValue(Boolean.TRUE);
    }
    attrBase.getName().setValue(name);
    setColumns(attrBase, columns, true, isGenerateColumnProperties());

    final PsiClass curClass;
    if ((curClass = ensureClassExists()) == null) return attrBase;

    ensurePropertyExists(curClass, name, attributeType, accessType, PsiAnnotation.EMPTY_ARRAY);
    return attrBase;
  }

  static void setColumns(final HbmColumnsHolderBase columnsHolder, final Collection<? extends DatabaseColumnInfo> columns,
                         final boolean insertSqlType, final boolean columnProperties) {
    if (columns.size() == 1 && !columnProperties) {
      columnsHolder.getColumn().setValue(columns.iterator().next().getName());
    }
    else {
      columnsHolder.getColumn().undefine();
      for (DatabaseColumnInfo columnInfo : columns) {
        final HbmColumn column = columnsHolder.addColumn();
        column.getName().setValue(columnInfo.getName());
        if (columnProperties) {
          if (insertSqlType) {
            final String sqlType = columnInfo.getSqlType();
            column.getSqlType().setValue(sqlType == null ? null : sqlType.toLowerCase());
            final int length = columnInfo.getLength();
            final int precision = columnInfo.getPrecision();
            if (length > 0 && length != 255) column.getLength().setValue(length);
            if (precision != 0) column.getPrecision().setValue(precision);
          }
          //column.getInsertable().setValue(columnInfo.isInsertable());
          final boolean notNull = !columnInfo.isNullable();
          if (notNull) column.getNotNull().setValue(notNull);
          //column.getUpdatable().setValue(columnInfo.isUpdatable());
        }
      }
    }
  }


  public void setIdClass(final String qualifiedName) throws IncorrectOperationException {
    final HbmPersistentObjectBase target = getManipulatorTarget();
    assert target instanceof HbmClass;
    ((HbmClass)target).getCompositeId().getClazz().setStringValue(qualifiedName);
  }

  public void addNamedQuery(final String queryName, final String queryText) {
    final HbmPersistentObjectBase target = getManipulatorTarget();
    assert target instanceof HbmClassBase;
    final HbmQuery query = ((HbmClassBase)target).addQuery();
    query.getName().setValue(queryName);
    query.setValue(queryText);
  }

  public static class MyDomAttributeAction extends MyAttributeAction<HibernateObjectManipulator> {

    public MyDomAttributeAction(final HibernateObjectManipulator manipulator, final HibernateAttributeType attributeType) {
      super(manipulator, attributeType);
    }

    protected RelationshipType getRelationshipType() {
      return myAttributeType.getRelationshipType();
    }

    public void update(final AnActionEvent e) {
      super.update(e);
      if (getPresentation().isEnabled()) {
        getPresentation().setEnabled(acceptedBy(getManipulator().getManipulatorTarget()));
      }
    }

    public Object getActionKey() {
      return myAttributeType == HibernateAttributeType.ELEMENTS ||
             myAttributeType == HibernateAttributeType.COMPOSITE_ELEMENTS ||
             myAttributeType == HibernateAttributeType.TIMESTAMP?
             myAttributeType : super.getActionKey();
    }

    private boolean acceptedBy(PersistentObject object) {
      if (myAttributeType == HibernateAttributeType.MANY_TO_ONE ||
          myAttributeType == HibernateAttributeType.PROPERTY) {
        return object instanceof HbmPersistentObjectBase;
      }
      else if (myAttributeType == HibernateAttributeType.ELEMENTS ||
               myAttributeType == HibernateAttributeType.COMPOSITE_ELEMENTS ||
               myAttributeType == HibernateAttributeType.COMPONENT ||
               myAttributeType == HibernateAttributeType.DYNAMIC_COMPONENT ||
               myAttributeType == HibernateAttributeType.ANY ||
               myAttributeType == HibernateAttributeType.ONE_TO_MANY ||
               myAttributeType == HibernateAttributeType.ONE_TO_ONE ||
               myAttributeType == HibernateAttributeType.MANY_TO_MANY) {
        return object instanceof HbmPersistentObjectBaseEx;
      }
      else if (myAttributeType == HibernateAttributeType.ID ||
               myAttributeType == HibernateAttributeType.TIMESTAMP ||
               myAttributeType == HibernateAttributeType.VERSION ||
               myAttributeType == HibernateAttributeType.COMPOSITE_ID) {
        return object instanceof HbmClass;
      }
      else {
        throw new AssertionError();
      }
    }

    public void invokeAction(@NotNull final Collection<PsiElement> result) throws IncorrectOperationException {
      final HbmPersistentObjectBase object = getManipulator().getManipulatorTarget();
      final Pair<JavaContainerType,PsiType> collectionType = PersistenceCommonUtil.getContainerType(myInfo.type);
      final PsiClass attributeClass = myInfo.type instanceof PsiClassType? ((PsiClassType)myInfo.type).resolve() : null;
      final PsiClass elementClass = collectionType.getSecond() instanceof PsiClassType? ((PsiClassType)collectionType.getSecond()).resolve() : null;

      final HbmContainer attributeContainer;
      if (myAttributeType.isContainer()) {
        assert collectionType.getFirst() != null;
        final boolean isPrimitive = collectionType.getSecond() != null && collectionType.getSecond() instanceof PsiPrimitiveType;
        attributeContainer = addContainer((HbmPersistentObjectBaseEx)object, collectionType.getFirst(), isPrimitive);
      }
      else {
        attributeContainer = null;
      }

      final HbmAttributeBase attribute;
      if (myAttributeType == HibernateAttributeType.ANY) {
        attribute = ((HbmPersistentObjectBaseEx)object).addAny();
      }
      else if (myAttributeType == HibernateAttributeType.COMPONENT) {
        final HbmComponent component = ((HbmPersistentObjectBaseEx)object).addComponent();
        component.getClazz().setValue(attributeClass);
        attribute = component;
      }
      else if (myAttributeType == HibernateAttributeType.DYNAMIC_COMPONENT) {
        attribute = ((HbmPersistentObjectBaseEx)object).addDynamicComponent();
      }
      else if (myAttributeType == HibernateAttributeType.COMPOSITE_ELEMENTS) {
        assert attributeContainer != null;
        final HbmCompositeElement compositeElement = attributeContainer.getCompositeElement();
        compositeElement.getClazz().setValue(elementClass);
        attribute = compositeElement;
      }
      else if (myAttributeType == HibernateAttributeType.COMPOSITE_ID) {
        final HbmCompositeId compositeId = ((HbmClass)object).getCompositeId();
        compositeId.getClazz().setValue(elementClass);
        attribute = compositeId;
      }
      else if (myAttributeType == HibernateAttributeType.ELEMENTS) {
        assert attributeContainer != null;
        final HbmElement element = attributeContainer.getElement();
        element.getTypeAttr().setValue(collectionType.getSecond());
        attribute = element;
      }
      else if (myAttributeType == HibernateAttributeType.ID) {
        attribute = ((HbmClass)object).getId();
      }
      else if (myAttributeType == HibernateAttributeType.MANY_TO_MANY) {
        assert attributeContainer != null;
        attribute = attributeContainer.getManyToMany();
      }
      else if (myAttributeType == HibernateAttributeType.MANY_TO_ONE) {
        attribute = object.addManyToOne();
      }
      else if (myAttributeType == HibernateAttributeType.ONE_TO_MANY) {
        assert attributeContainer != null;
        attribute = attributeContainer.getOneToMany();
      }
      else if (myAttributeType == HibernateAttributeType.ONE_TO_ONE) {
        attribute = ((HbmPersistentObjectBaseEx)object).addOneToOne();
      }
      else if (myAttributeType == HibernateAttributeType.PROPERTY) {
        attribute = object.addProperty();
      }
      else if (myAttributeType == HibernateAttributeType.TIMESTAMP) {
        attribute = ((HbmClass)object).getTimestamp();
      }
      else if (myAttributeType == HibernateAttributeType.VERSION) {
        attribute = ((HbmClass)object).getVersion();
      }
      else {
        throw new AssertionError("invalid type: " + myAttributeType);
      }
      attribute.getName().setValue(myInfo.name);
      if (getRelationshipType() != null) {
        ((HbmRelationAttributeBase)attribute).getTargetEntityClass().setValue(myAttributeType.isContainer()? elementClass : attributeClass);
        final PersistentRelationshipAttributeManipulator manipulator = PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(attribute, PersistentRelationshipAttributeManipulator.class);
        if (manipulator != null) {
          manipulator.setMappedByAndInverse(myInfo.targetAttribute, myInfo.inverse);
        }
      }
      super.invokeAction(result);
    }
  }
}
