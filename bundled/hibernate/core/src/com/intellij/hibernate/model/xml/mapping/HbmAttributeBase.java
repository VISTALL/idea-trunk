package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.AttributeMemberConverter;
import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.psi.PsiMember;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 */
public interface HbmAttributeBase extends CommonDomModelElement, PersistentAttribute {

  @PrimaryKey
  MutableGenericValue<String> getName();

  GenericAttributeValue<AccessType> getAccess();

  @Nullable
  PsiMember getPsiMember();

  @NameValue
  @Attribute("name")
  @Convert(AttributeMemberConverter.class)
  GenericAttributeValue<PsiMember> getTargetMember();

}
