package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.*;
import com.intellij.hibernate.model.enums.NotFoundType;
import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.hibernate.model.enums.CascadeType;
import com.intellij.persistence.model.PersistentEntity;
import com.intellij.persistence.model.PersistentRelationshipAttribute;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.util.xml.*;
import com.intellij.javaee.model.xml.converters.AttributeConverter;

import java.util.List;

/**
 * @author Gregory.Shrago
 */
public interface HbmRelationAttributeBase extends HbmAttributeBase, PersistentRelationshipAttribute {

  @Required
  @Attribute("name")
  @Convert(AttributeMemberConverter.class)
  GenericAttributeValue<PsiMember> getTargetMember();

  @Convert(MappingClassResolveConverter.class)
  GenericAttributeValue<PsiClass> getClazz();

  @Convert(PersistentEntityByNameConverter.class)
  GenericAttributeValue<PersistentEntity> getEntityNameValue();

  GenericDomValue<PsiClass> getTargetEntityClass();

  @Convert(LazyTypeConverter.class)
  GenericAttributeValue<LazyType> getLazy();

  @Convert(CascadeTypeListConverter.class)
  GenericAttributeValue<List<CascadeType>> getCascade();

  interface NonOneToManyBase extends HbmRelationAttributeBase {
    @Convert(AttributeConverter.class)
    GenericAttributeValue<PersistentAttribute> getPropertyRef();
  }

  interface AnyToManyBase extends HbmRelationAttributeBase, HbmCollectionAttributeBase {
    GenericAttributeValue<NotFoundType> getNotFound();
  }

  interface AnyToOneBase extends NonOneToManyBase {

  }

}
