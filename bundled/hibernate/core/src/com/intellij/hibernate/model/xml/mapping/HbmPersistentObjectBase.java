package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.PersistentObjectClassResolveConverter;
import com.intellij.persistence.model.PersistentObject;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.PrimaryKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Gregory.Shrago
 */
public interface HbmPersistentObjectBase extends HbmAttributeContainerBase, PersistentObject {
  @PrimaryKey
  @Convert(PersistentObjectClassResolveConverter.class)
  GenericDomValue<PsiClass> getClazz();

  List<HbmAttributeBase> getAllAttributes();

  @NotNull
  List<? extends HbmPropertyBase> getProperties();

  HbmPropertyBase addProperty();

  @NotNull
  List<? extends HbmManyToOneBase> getManyToOnes();

  HbmManyToOneBase addManyToOne();

}
