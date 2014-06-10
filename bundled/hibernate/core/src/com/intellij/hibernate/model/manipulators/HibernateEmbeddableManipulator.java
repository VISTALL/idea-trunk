/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.manipulators;

import com.intellij.hibernate.model.enums.CascadeType;
import com.intellij.hibernate.model.enums.HibernateAttributeType;
import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.hibernate.model.enums.NotFoundType;
import com.intellij.hibernate.model.xml.impl.mapping.HbmEmbeddableImpl;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.javaee.model.common.persistence.mapping.AttributeType;
import com.intellij.jpa.model.manipulators.ObjectManipulatorBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.persistence.database.DatabaseColumnInfo;
import com.intellij.persistence.database.DatabaseTableInfo;
import com.intellij.persistence.model.*;
import com.intellij.persistence.model.manipulators.PersistenceAction;
import com.intellij.persistence.model.manipulators.PersistentObjectManipulator;
import com.intellij.persistence.util.JavaContainerType;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PairProcessor;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class HibernateEmbeddableManipulator extends ObjectManipulatorBase<HbmEmbeddableImpl> {
  public HibernateEmbeddableManipulator(final HbmEmbeddableImpl target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {
    final ArrayList<PersistenceAction> result = new ArrayList<PersistenceAction>();
    for (AttributeType type : AttributeType.values()) {
      if (type instanceof HibernateAttributeType &&
          ReflectionCache.isAssignable(DomElement.class, type.getAttributeClass())) {
        if (type == HibernateAttributeType.ID ||
            type == HibernateAttributeType.TIMESTAMP ||
            type == HibernateAttributeType.VERSION ||
            type == HibernateAttributeType.COMPOSITE_ID ||
            type == HibernateAttributeType.ONE_TO_MANY ||
            type == HibernateAttributeType.MANY_TO_MANY) continue;
        result.add(new MyDomAttributeAction(this, (HibernateAttributeType)type));
      }
    }
    return result;
  }

  public void setTable(final DatabaseTableInfo tableInfo) throws IncorrectOperationException {
    throw new AssertionError("operation not supported");
  }

  public PersistentEmbeddedAttribute addEmbeddedAttribute(final PersistentEmbeddable embeddable, final String attributeName, final PropertyMemberType accessMode)
    throws IncorrectOperationException {
    final HbmEmbeddableImpl object = getManipulatorTarget();
    final ArrayList<HbmEmbeddedAttributeBase> result = new ArrayList<HbmEmbeddedAttributeBase>();
    for (HbmEmbeddedAttributeBase attribute : object.getDefiningAttributes()) {
      final HbmEmbeddedAttributeBase attr;
      if (attribute instanceof HbmComponent) {
        attr = ((HbmComponent)attribute).addComponent();
      }
      else if (attribute instanceof HbmCompositeElement) {
        attr = ((HbmCompositeElement)attribute).addNestedCompositeElement();
      }
      else if (attribute instanceof HbmNestedCompositeElement) {
        attr = ((HbmNestedCompositeElement)attribute).addNestedCompositeElement();
      }
      else if (attribute instanceof HbmCompositeId) {
        attr = null;
      }
      else {
        attr = null;
      }
      if (attr != null) {
        attr.getName().setValue(attributeName);
        attr.getClazz().setValue(embeddable.getClazz().getValue());
        result.add(attr);
      }
    }
    if (result.isEmpty()) return null;
    if (ensureClassExists() == null) return result.get(0);
    final PersistentObjectManipulator<PersistentEmbeddable> manipulator =
      PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(embeddable, PersistentObjectManipulator.class);
    final PsiClass embeddableClass;
    if (manipulator == null || (embeddableClass = manipulator.ensureClassExists()) == null) return result.get(0);
    for (HbmEmbeddedAttributeBase attr : result) {
      attr.getClazz().setValue(embeddable.getClazz().getValue());
    }
    ensurePropertyExists(getManipulatorTarget().getClazz().getValue(), attributeName,
                         JavaPsiFacade.getInstance(embeddableClass.getProject()).getElementFactory().createType(embeddableClass),
                         accessMode, PsiAnnotation.EMPTY_ARRAY);
    return result.get(0);
  }

  public PersistentRelationshipAttribute addRelationshipAttribute(final PersistentEntityBase entity, final RelationshipType relationshipType,
                                                                  final JavaContainerType containerType, final String attributeName, final String targetAttributeName,
                                                                  final boolean inverse,
                                                                  final boolean optional,
                                                                  final String fetchType,
                                                                  final Collection<String> cascadeVariants,
                                                                  final PropertyMemberType accessMode) throws IncorrectOperationException {
    assert !relationshipType.isMany(false) : "to-many associations forbidden";
    final ArrayList<HbmRelationAttributeBase> results = new ArrayList<HbmRelationAttributeBase>();
    processDefiningAttributes(new PairProcessor<HbmEmbeddedAttributeBase, AttributeManipulator>() {
      public boolean process(final HbmEmbeddedAttributeBase hbmEmbeddedAttributeBase, final AttributeManipulator attributeManipulator) {
        final HbmRelationAttributeBase attrBase;
        switch (relationshipType) {
          case MANY_TO_ONE:
            attrBase = attributeManipulator.addManyToOne(hbmEmbeddedAttributeBase);
            break;
          case ONE_TO_ONE:
            attrBase = attributeManipulator.addOneToOne(hbmEmbeddedAttributeBase);
            break;
          default:
            throw new AssertionError();
        }
        if (attrBase != null) {
          attrBase.getName().setValue(attributeName);
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
          if (optional && attrBase instanceof HbmRelationAttributeBase.AnyToManyBase) {
            ((HbmRelationAttributeBase.AnyToManyBase)attrBase).getNotFound()
                .setValue(optional ? NotFoundType.IGNORE : NotFoundType.EXCEPTION);
          }
          if (inverse) {
            final DomElement parent = attrBase.getParent();
            if (parent instanceof HbmContainer && !(parent instanceof HbmPrimitiveArray || parent instanceof HbmIdbag)) {
              ((HbmContainer)parent).getInverse().setValue(Boolean.TRUE);
            }
          }
          results.add(attrBase);
        }
        return true;
      }
    });
    if (results.isEmpty()) return null;

    final PsiClass curClass;
    if ((curClass = ensureClassExists()) == null) return results.get(0);
    final PersistentObjectManipulator<PersistentEntityBase> manipulator =
      PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(entity, PersistentObjectManipulator.class);
    final PsiClass entityClass;
    if (manipulator == null || (entityClass = manipulator.ensureClassExists()) == null) return results.get(0);
    for (HbmRelationAttributeBase attrBase : results) {
      attrBase.getTargetEntityClass().setValue(entityClass);
    }

    final PsiClassType entityClassType = JavaPsiFacade.getInstance(curClass.getProject()).getElementFactory().createType(entityClass);
    final PsiType propertyType = relationshipType.isMany(false) ? containerType.createCollectionType(curClass, entityClassType, null) : entityClassType;

    ensurePropertyExists(curClass, attributeName, propertyType,
                         accessMode, PsiAnnotation.EMPTY_ARRAY);
    return results.get(0);
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

  private boolean processDefiningAttributes(final PairProcessor<HbmEmbeddedAttributeBase, AttributeManipulator> processor) {
    final HbmEmbeddableImpl object = getManipulatorTarget();
    for (HbmEmbeddedAttributeBase attribute : object.getDefiningAttributes()) {
      final AttributeManipulator manipulator = AttributeManipulator.getManipulator(attribute);
      if (!processor.process(attribute, manipulator)) return false;
    }
    return true;
  }

  public PersistentAttribute addAttribute(final String name, final PsiType attributeType, final PropertyMemberType accessType,
                                          final Collection<? extends DatabaseColumnInfo> columns) throws IncorrectOperationException {
    final Ref<PersistentAttribute> firstRef = Ref.create(null);
    processDefiningAttributes(new PairProcessor<HbmEmbeddedAttributeBase, AttributeManipulator>() {
      public boolean process(final HbmEmbeddedAttributeBase hbmEmbeddedAttributeBase, final AttributeManipulator attributeManipulator) {
        final HbmPropertyBase attrBase = attributeManipulator.addProperty(hbmEmbeddedAttributeBase);
        if (attrBase != null) {
          attrBase.getName().setValue(name);
          HibernateObjectManipulator.setColumns(attrBase, columns, true, isGenerateColumnProperties());
          if (firstRef.isNull()) firstRef.set(attrBase);
        }
        return true;
      }
    });
    final PsiClass curClass;
    if ((curClass = ensureClassExists()) == null) return firstRef.get();

    ensurePropertyExists(curClass, name, attributeType, accessType, PsiAnnotation.EMPTY_ARRAY);

    return firstRef.get();
  }

  public PersistentAttribute addIdAttribute(final boolean compositeId, final String name, final PsiType attributeType, final PropertyMemberType accessType,
                                            final Collection<? extends DatabaseColumnInfo> columns) throws IncorrectOperationException {
    throw new UnsupportedOperationException("addIdAttribute not supported");
  }

  public void setIdClass(final String qualifiedName) throws IncorrectOperationException {
    throw new UnsupportedOperationException("setIdClass not supported");
  }

  public void addNamedQuery(final String queryName, final String queryText) {
    throw new UnsupportedOperationException("addNamedQuery not supported");
  }

  public static class MyDomAttributeAction extends MyAttributeAction<HibernateEmbeddableManipulator> {

    public MyDomAttributeAction(final HibernateEmbeddableManipulator manipulator, final HibernateAttributeType attributeType) {
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

    private boolean acceptedBy(PersistentObject object) {
      for (HbmEmbeddedAttributeBase attribute : getManipulator().getManipulatorTarget().getDefiningAttributes()) {
        if (AttributeManipulator.getManipulator(attribute).acceptsAttribute((HibernateAttributeType)myAttributeType)) return true;
      }
      return false;
    }

    public void invokeAction(@NotNull final Collection<PsiElement> result) throws IncorrectOperationException {
      final Pair<JavaContainerType,PsiType> collectionType = PersistenceCommonUtil.getContainerType(myInfo.type);
      final PsiClass attributeClass = myInfo.type instanceof PsiClassType? ((PsiClassType)myInfo.type).resolve() : null;
      final PsiClass elementClass = collectionType.getSecond() instanceof PsiClassType? ((PsiClassType)collectionType.getSecond()).resolve() : null;
      getManipulator().processDefiningAttributes(new PairProcessor<HbmEmbeddedAttributeBase, AttributeManipulator>() {
        public boolean process(final HbmEmbeddedAttributeBase hbmEmbeddedAttributeBase, final AttributeManipulator attributeManipulator) {
          final HbmContainer attributeContainer;
          if (myAttributeType.isContainer()) {
            assert collectionType.getFirst() != null;
            final boolean isPrimitive = collectionType.getSecond() != null && collectionType.getSecond() instanceof PsiPrimitiveType;
            attributeContainer = attributeManipulator.addContainer(hbmEmbeddedAttributeBase, collectionType.getFirst(), isPrimitive);
          }
          else {
            attributeContainer = null;
          }

          final HbmAttributeBase attribute;
          if (myAttributeType == HibernateAttributeType.ANY) {
            attribute = attributeManipulator.addAny(hbmEmbeddedAttributeBase);
          }
          else if (myAttributeType == HibernateAttributeType.COMPONENT) {
            final HbmEmbeddedAttributeBase component = attributeManipulator.addComponent(hbmEmbeddedAttributeBase);
            if (component != null) {
              component.getClazz().setValue(attributeClass);
            }
            attribute = component;
          }
          else if (myAttributeType == HibernateAttributeType.DYNAMIC_COMPONENT) {
            attribute = attributeManipulator.addDynamicComponent(hbmEmbeddedAttributeBase);
          }
          else if (myAttributeType == HibernateAttributeType.COMPOSITE_ELEMENTS) {
            if (attributeContainer != null) {
              final HbmCompositeElement compositeElement = attributeContainer.getCompositeElement();
              compositeElement.getClazz().setValue(elementClass);
              attribute = compositeElement;
            }
            else attribute = null;
          }
          else if (myAttributeType == HibernateAttributeType.ELEMENTS) {
            if (attributeContainer != null) {
              final HbmElement element = attributeContainer.getElement();
              element.getTypeAttr().setValue(collectionType.getSecond());
              attribute = element;
            }
            else attribute = null;
          }
          else if (myAttributeType == HibernateAttributeType.MANY_TO_ONE) {
            attribute = attributeManipulator.addManyToOne(hbmEmbeddedAttributeBase);
          }
          else if (myAttributeType == HibernateAttributeType.ONE_TO_ONE) {
            attribute = attributeManipulator.addOneToOne(hbmEmbeddedAttributeBase);
          }
          else if (myAttributeType == HibernateAttributeType.PROPERTY) {
            attribute = attributeManipulator.addProperty(hbmEmbeddedAttributeBase);
          }
          else {
            throw new AssertionError("invalid type: " + myAttributeType);
          }
          if (attribute != null) {
            attribute.getName().setValue(myInfo.name);
            if (getRelationshipType() != null) {
              ((HbmRelationAttributeBase)attribute).getTargetEntityClass()
                  .setValue(myAttributeType.isContainer() ? elementClass : attributeClass);
              if (myInfo.inverse) {
                final DomElement parent = attribute.getParent();
                if (parent instanceof HbmContainer && !(parent instanceof HbmPrimitiveArray || parent instanceof HbmIdbag)) {
                  ((HbmContainer)parent).getInverse().setValue(Boolean.TRUE);
                }
              }
            }
          }
          return true;
        }
      });
      super.invokeAction(result);
    }
  }

  public abstract static class AttributeManipulator {

    public static AttributeManipulator getManipulator(final HbmEmbeddedAttributeBase attribute) {
      if (attribute instanceof HbmComponent) return COMPONENT;
      if (attribute instanceof HbmCompositeElement) return COMPOSITE_ELEMENT;
      if (attribute instanceof HbmCompositeId) return COMPOSITE_ID;
      if (attribute instanceof HbmNestedCompositeElement) return NESTED_COMPOSITE_ELEMENT;
      return EMPTY;
    }

    public static final AttributeManipulator EMPTY = new AttributeManipulator() {
    };
    public static final AttributeManipulator COMPONENT = new AttributeManipulator() {
      public boolean acceptsAttribute(final HibernateAttributeType type) {
        return true;
      }

      public HbmAny addAny(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmComponent)attribute).addAny();
      }

      public HbmManyToOneBase addManyToOne(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmComponent)attribute).addManyToOne();
      }

      public HbmOneToOne addOneToOne(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmComponent)attribute).addOneToOne();
      }

      public HbmEmbeddedAttributeBase addComponent(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmComponent)attribute).addComponent();
      }

      public HbmDynamicComponent addDynamicComponent(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmComponent)attribute).addDynamicComponent();
      }

      public HbmContainer addContainer(final HbmEmbeddedAttributeBase attribute, final JavaContainerType containerType, final boolean isPrimitive) {
        final HbmComponent component = (HbmComponent)attribute;
        switch (containerType) {
          case ARRAY:
            return isPrimitive ? component.addPrimitiveArray() : component.addArray();
          case COLLECTION:
            return component.addSet();
          case LIST:
            return component.addList();
          case MAP:
            return component.addMap();
          case SET:
            return component.addSet();
        }
        throw new AssertionError(containerType);
      }

    };
    public static final AttributeManipulator COMPOSITE_ELEMENT = new AttributeManipulator() {
      public boolean acceptsAttribute(final HibernateAttributeType type) {
        return type == HibernateAttributeType.ANY ||
               type == HibernateAttributeType.COMPOSITE_ELEMENTS ||
               type == HibernateAttributeType.PROPERTY ||
               type == HibernateAttributeType.MANY_TO_ONE;
      }

      public HbmAny addAny(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmCompositeElement)attribute).addAny();
      }

      public HbmPropertyBase addProperty(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmCompositeElement)attribute).addProperty();
      }

      public HbmManyToOneBase addManyToOne(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmCompositeElement)attribute).addManyToOne();
      }

      public HbmEmbeddedAttributeBase addComponent(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmCompositeElement)attribute).addNestedCompositeElement();
      }
    };

    public static final AttributeManipulator COMPOSITE_ID = new AttributeManipulator() {
      public boolean acceptsAttribute(final HibernateAttributeType type) {
        return type == HibernateAttributeType.PROPERTY || type == HibernateAttributeType.MANY_TO_ONE;
      }

      public HbmManyToOneBase addManyToOne(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmCompositeId)attribute).addManyToOne();
      }

      public HbmPropertyBase addProperty(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmCompositeId)attribute).addProperty();
      }
    };
    public static final AttributeManipulator NESTED_COMPOSITE_ELEMENT = new AttributeManipulator() {
      public boolean acceptsAttribute(final HibernateAttributeType type) {
        return type == HibernateAttributeType.ANY ||
               type == HibernateAttributeType.COMPOSITE_ELEMENTS ||
               type == HibernateAttributeType.PROPERTY ||
               type == HibernateAttributeType.MANY_TO_ONE;
      }

      public HbmAny addAny(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmNestedCompositeElement)attribute).addAny();
      }

      public HbmPropertyBase addProperty(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmNestedCompositeElement)attribute).addProperty();
      }

      public HbmManyToOneBase addManyToOne(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmNestedCompositeElement)attribute).addManyToOne();
      }

      public HbmEmbeddedAttributeBase addComponent(final HbmEmbeddedAttributeBase attribute) {
        return ((HbmNestedCompositeElement)attribute).addNestedCompositeElement();
      }
    };

    public boolean acceptsAttribute(HibernateAttributeType type) {
      return false;
    }

    @Nullable
    public HbmAny addAny(final HbmEmbeddedAttributeBase attribute) {
      return null;
    }

    @Nullable
    public HbmPropertyBase addProperty(final HbmEmbeddedAttributeBase attribute) {
      return null;
    }

    @Nullable
    public HbmManyToOneBase addManyToOne(final HbmEmbeddedAttributeBase attribute) {
      return null;
    }

    @Nullable
    public HbmOneToOne addOneToOne(final HbmEmbeddedAttributeBase attribute) {
      return null;
    }

    @Nullable
    public HbmEmbeddedAttributeBase addComponent(final HbmEmbeddedAttributeBase attribute) {
      return null;
    }

    @Nullable
    public HbmDynamicComponent addDynamicComponent(final HbmEmbeddedAttributeBase attribute) {
      return null;
    }

    @Nullable
    public HbmContainer addContainer(final HbmEmbeddedAttributeBase attribute, final JavaContainerType containerType, final boolean isPrimitive) {
      return null;
    }
  }
}