package com.intellij.hibernate.model.xml.impl.mapping;


import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.persistence.facet.PersistencePackageDefaults;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.model.helpers.PersistentObjectModelHelper;
import com.intellij.persistence.roles.PersistenceClassRole;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmPersistentObjectBaseImpl extends BaseImpl implements HbmPersistentObjectBase, PersistentObjectModelHelper {


  @NotNull
  public PersistentObjectModelHelper getObjectModelHelper() {
    return this;
  }

  @NotNull
  public List<? extends PersistentAttribute> getAttributes() {
    return getAllAttributes();
  }

  public PropertyMemberType getDefaultAccessMode() {
    final HbmHibernateMapping mapping = getParentOfType(HbmHibernateMapping.class, false);
    return mapping != null? mapping.getDefaultAccess().getValue() == AccessType.FIELD ? PropertyMemberType.FIELD : PropertyMemberType.GETTER : PropertyMemberType.GETTER;
  }

  public boolean isAccessModeFixed() {
    return false;
  }

  public List<HbmAttributeBase> getAllAttributes() {
    final ArrayList<HbmAttributeBase> result = new ArrayList<HbmAttributeBase>();
    visitAttributes(new HbmAttributeVisitorAdapter() {

      public void visitAttributeBase(final HbmAttributeBase attributeBase) {
        result.add(attributeBase);
      }
    });
    return result;
  }

  public void visitAttributes(final HbmAttributeVisitor visitor) {
    DomUtil.acceptAvailableChildren(this, new HbmAttributeDomElementVisitor(visitor));
  }

  protected <T> GenericValue<T> getCurrentValueOrDefault(final GenericValue<T> currentValue, final DefaultsProcessor<T> processor) {
    if (currentValue != null && currentValue.getValue() != null) return currentValue;
    final HbmHibernateMapping mappings = getParentOfType(HbmHibernateMapping.class, true);
    if (mappings != null) {
      final GenericValue<T> value1 = processor.processMappings(mappings);
      if (value1 != null && value1.getValue() != null) return value1;

      // we should have clazz at the moment
      final PsiClass clazz = getClazz().getValue();
      if (clazz == null) return currentValue;
      for (PersistenceClassRole role : PersistenceCommonUtil.getPersistenceRoles(clazz)) {
        if (role.getPersistentObject() == null) continue;
        final PersistencePackageDefaults defaults = role.getFacet().getPersistenceUnitDefaults(role.getPersistenceUnit());
        final T value3 = processor.processUnitDefaults(defaults);
        if (value3 != null) return ReadOnlyGenericValue.getInstance(value3);
      }
      return currentValue;
    }
    return currentValue;
  }

  protected interface DefaultsProcessor<T> {
    GenericValue<T> processMappings(final HbmHibernateMapping mappings);
    T processUnitDefaults(final PersistencePackageDefaults defaults);
  }
}
