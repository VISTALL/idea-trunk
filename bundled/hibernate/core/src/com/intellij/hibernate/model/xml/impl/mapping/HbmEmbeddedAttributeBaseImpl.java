package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.converters.AttributeMemberConverter;
import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.xml.mapping.HbmCompositeId;
import com.intellij.hibernate.model.xml.mapping.HbmEmbeddedAttributeBase;
import com.intellij.hibernate.model.xml.mapping.HbmAttributeVisitor;
import com.intellij.jpa.util.JpaUtil;
import com.intellij.persistence.model.helpers.PersistentAttributeModelHelper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.util.NullableFunction;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmEmbeddedAttributeBaseImpl extends HbmAttributeBaseImpl implements HbmEmbeddedAttributeBase, PersistentAttributeModelHelper {

  public boolean isFieldAccess() {
    final AccessType thisAccess = getAccess().getValue();
    return thisAccess == AccessType.FIELD || thisAccess == null && AttributeMemberConverter.isDefaultAccessField(this);
  }

  public boolean isIdAttribute() {
    return this instanceof HbmCompositeId;
  }

  public List<? extends GenericValue<String>> getMappedColumns() {
    return Collections.emptyList();
  }

  public boolean isLob() {
    return false;
  }

  @Nullable
  public PsiType getPsiType() {
    return JpaUtil.findType(this, NullableFunction.NULL);
  }

  @Nullable
  public GenericValue<PsiClass> getTargetEmbeddableClass() {
    return getClazz();
  }


  @NotNull
  public PersistentAttributeModelHelper getAttributeModelHelper() {
    return this;
  }

  public void visitAttributes(final HbmAttributeVisitor visitor) {
    DomUtil.acceptAvailableChildren(this, new HbmAttributeDomElementVisitor(visitor));
  }

}