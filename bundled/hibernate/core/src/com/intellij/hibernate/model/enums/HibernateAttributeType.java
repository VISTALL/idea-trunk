/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.enums;

import com.intellij.hibernate.HibernateMessages;
import static com.intellij.hibernate.model.annotations.mapping.CollectionOfElementsImpl.*;
import com.intellij.hibernate.model.common.mapping.CollectionOfElements;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.jam.reflect.JamMemberMeta;
import com.intellij.javaee.model.common.persistence.mapping.AttributeType;
import com.intellij.jpa.model.annotations.mapping.JamAttributeBase;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.model.RelationshipType;
import com.intellij.persistence.util.JavaContainerType;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.ModelMergerUtil;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gregory.Shrago
 */
public class HibernateAttributeType extends AttributeType{

  public static final HibernateAttributeType ID = new HibernateAttributeType(HbmId.class, HibernateMessages.message("type.hibernate.attr.id"), ID_ATTR_META);
  public static final HibernateAttributeType COMPOSITE_ID = new HibernateAttributeType(HbmCompositeId.class, HibernateMessages.message("type.hibernate.attr.composite.id"), EMBEDDED_ID_ATTR_META);
  public static final HibernateAttributeType VERSION = new HibernateAttributeType(HbmVersion.class, HibernateMessages.message("type.hibernate.attr.version"), VERSION_ATTR_META);
  public static final HibernateAttributeType TIMESTAMP = new HibernateAttributeType(HbmTimestamp.class, HibernateMessages.message("type.hibernate.attr.timestamp"), BASIC_ATTR_META);
  public static final HibernateAttributeType PROPERTY = new HibernateAttributeType(HbmProperty.class, HibernateMessages.message("type.hibernate.attr.property"), BASIC_ATTR_META) {
    protected boolean accepts(final PersistentAttribute attribute) {
      return super.accepts(attribute) || ReflectionCache.isInstance(attribute, HbmKeyProperty.class);
    }
  };

  public static final HibernateAttributeType ONE_TO_ONE =  new HibernateAttributeType(HbmOneToOne.class, HibernateMessages.message("type.hibernate.attr.one.to.one"), ONE_TO_ONE_ATTR_META);
  public static final HibernateAttributeType ONE_TO_MANY = new HibernateAttributeType(HbmOneToMany.class, HibernateMessages.message("type.hibernate.attr.one.to.many"), ONE_TO_MANY_ATTR_META);
  public static final HibernateAttributeType MANY_TO_ONE = new HibernateAttributeType(HbmManyToOne.class, HibernateMessages.message("type.hibernate.attr.many.to.one"), MANY_TO_ONE_ATTR_META) {
    protected boolean accepts(final PersistentAttribute attribute) {
      return super.accepts(attribute) || ReflectionCache.isInstance(attribute, HbmKeyManyToOne.class);
    }
  };
  public static final HibernateAttributeType MANY_TO_MANY = new HibernateAttributeType(HbmManyToMany.class, HibernateMessages.message("type.hibernate.attr.many.to.many"), MANY_TO_MANY_ATTR_META);

  public static final HibernateAttributeType ELEMENTS  = new HibernateAttributeType(HbmElement.class, HibernateMessages.message("type.hibernate.attr.elements"), BASIC_COLLECTION_ATTR_META, COLLECTION_OF_ELEMENTS_ATTR_META);
  public static final HibernateAttributeType COMPOSITE_ELEMENTS = new HibernateAttributeType(HbmCompositeElement.class, HibernateMessages.message("type.hibernate.attr.composite.elements"), EMBEDDED_COLLECTION_ATTR_META, COLLECTION_OF_EMBEDDED_ELEMENTS_ATTR_META);
  public static final HibernateAttributeType COLLECTION_OF_ELEMENTS = new HibernateAttributeType(CollectionOfElements.class, HibernateMessages.message("type.hibernate.attr.collection.of.elements"), COLLECTION_OF_ELEMENTS_ATTR_META, COLLECTION_OF_EMBEDDED_ELEMENTS_ATTR_META);

  public static final HibernateAttributeType COMPONENT = new HibernateAttributeType(HbmComponent.class, HibernateMessages.message("type.hibernate.attr.component"), EMBEDDED_ATTR_META) {
    protected boolean accepts(final PersistentAttribute attribute) {
      return super.accepts(attribute) || ReflectionCache.isInstance(attribute, HbmNestedCompositeElement.class);
    }
  };

  public static final HibernateAttributeType ANY = new HibernateAttributeType(HbmAny.class, HibernateMessages.message("type.hibernate.attr.any"));
  public static final HibernateAttributeType DYNAMIC_COMPONENT = new HibernateAttributeType(HbmDynamicComponent.class, HibernateMessages.message("type.hibernate.attr.dynamic.component"));

  public HibernateAttributeType(final Class attributeClass, final String typeName, final JamMemberMeta<PsiMember, ? extends JamAttributeBase>... jamMeta) {
    super(attributeClass, typeName, jamMeta);
  }

  public boolean isIdAttribute() {
    return this == ID || this == COMPOSITE_ID;
  }

  public boolean isEmbedded() {
    return this == COMPOSITE_ID || this == COMPONENT || this == COMPOSITE_ELEMENTS;
  }

  public boolean isContainer() {
    return this == COLLECTION_OF_ELEMENTS ||
           this == COMPOSITE_ELEMENTS ||
           this == ELEMENTS ||
           this == MANY_TO_MANY ||
           this == ONE_TO_MANY;
  }

  public boolean isBasic() {
    return this == PROPERTY;
  }

  @Nullable
  public RelationshipType getRelationshipType() {
    if (this == MANY_TO_MANY) {
      return RelationshipType.MANY_TO_MANY;
    }
    else if (this == MANY_TO_ONE) {
      return RelationshipType.MANY_TO_ONE;
    }
    else if (this == ONE_TO_MANY) {
      return RelationshipType.ONE_TO_MANY;
    }
    else if (this == ONE_TO_ONE) {
      return RelationshipType.ONE_TO_ONE;
    }
    else {
      return null;
    }
  }

  public static void initialize() {
    // nothing
  }

  @NotNull
  public PsiType getDefaultPsiType(final PersistentAttribute attribute) {
    if (!isContainer()) return super.getDefaultPsiType(attribute);
    final HbmAttributeBase domAttribute = ModelMergerUtil.getImplementation(attribute, HbmAttributeBase.class);
    if (domAttribute == null) return super.getDefaultPsiType(attribute);
    final HbmContainer container = domAttribute.getParentOfType(HbmContainer.class, false);
    if (container == null) return super.getDefaultPsiType(attribute);
    final PsiType psiType = getDefaultElementPsiType(attribute);
    final JavaContainerType containerType;
    PsiType mapKeyType = null;
    if (container instanceof HbmArray) {
      containerType = JavaContainerType.ARRAY;
    }
    else if (container instanceof HbmBag) {
      containerType = DomUtil.hasXml(((HbmBag)container).getOrderBy()) ? JavaContainerType.LIST : JavaContainerType.COLLECTION;
    }
    else if (container instanceof HbmIdbag) {
      containerType = DomUtil.hasXml(((HbmIdbag)container).getOrderBy()) ? JavaContainerType.LIST : JavaContainerType.COLLECTION;
    }
    else if (container instanceof HbmList) {
      containerType = JavaContainerType.LIST;
    }
    else if (container instanceof HbmMap) {
      containerType = JavaContainerType.MAP;
      final PsiManager psiManager = attribute.getPsiManager();
      mapKeyType = PsiType.getJavaLangObject(psiManager, ProjectScope.getAllScope(psiManager.getProject()));
    }
    else if (container instanceof HbmPrimitiveArray) {
      containerType = JavaContainerType.ARRAY;
    }
    else if (container instanceof HbmSet) {
      containerType = JavaContainerType.SET;
    }
    else {
      containerType = JavaContainerType.COLLECTION;
    }
    return containerType.createCollectionType(attribute.getIdentifyingPsiElement(), psiType, mapKeyType);
  }

  @NotNull
  protected PsiType getDefaultElementPsiType(final PersistentAttribute attribute) {
    if (this == DYNAMIC_COMPONENT) {
      try {
        final PsiManager psiManager = attribute.getPsiManager();
        final PsiClassType stringClass = PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(psiManager.getProject()));
        final PsiClassType objectClass = PsiType.getJavaLangObject(psiManager, GlobalSearchScope.allScope(psiManager.getProject()));
        return JavaContainerType.MAP.createCollectionType(attribute.getIdentifyingPsiElement(), objectClass, stringClass);
      }
      catch (IncorrectOperationException e) {
        throw new AssertionError(e);
      }
    }
    return super.getDefaultElementPsiType(attribute);
  }
}
