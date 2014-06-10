package com.intellij.hibernate.model.xml.mapping;

import com.intellij.persistence.model.PersistentEmbeddedAttribute;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.hibernate.model.converters.PersistentObjectClassResolveConverter;

/**
 * @author Gregory.Shrago
 */
public interface HbmEmbeddedAttributeBase extends HbmAttributeBase, HbmAttributeContainerBase, PersistentEmbeddedAttribute {

  @Convert(PersistentObjectClassResolveConverter.class)
  GenericAttributeValue<PsiClass> getClazz();

}