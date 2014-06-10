package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.enums.CascadeType;
import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.hibernate.model.enums.NotFoundType;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.persistence.model.RelationshipType;
import com.intellij.persistence.model.TableInfoProvider;
import com.intellij.persistence.model.helpers.PersistentRelationshipAttributeModelHelper;
import com.intellij.psi.PsiClass;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmAnyToOneImpl extends HbmAttributeBaseImpl implements HbmRelationAttributeBase.AnyToOneBase,
                                                                              PersistentRelationshipAttributeModelHelper {

  public GenericDomValue<PsiClass> getTargetEntityClass() {
    return getClazz();
  }


  @NotNull
  public PersistentRelationshipAttributeModelHelper getAttributeModelHelper() {
    return this;
  }

  @NotNull
  public RelationshipType getRelationshipType() {
    return this instanceof HbmOneToOne? RelationshipType.ONE_TO_ONE : RelationshipType.MANY_TO_ONE;
  }

  public boolean isRelationshipSideOptional(final boolean thisSide) {
    return thisSide ||
           (this instanceof HbmOneToOne && !Boolean.TRUE.equals(((HbmOneToOne)this).getConstrained().getValue())) ||
           (this instanceof HbmOneToMany && ((HbmOneToMany)this).getNotFound().getValue() == NotFoundType.IGNORE);
  }

  @Nullable
  public String getMappedByAttributeName() {
    return getPropertyRef().getStringValue();
  }

  public TableInfoProvider getTableInfoProvider() {
    return getParentOfType(HbmTableInfoProvider.class, true);
  }

  public boolean isInverseSide() {
    return false;
  }

  public String getFetchType() {
    final LazyType type = getLazy().getValue();
    return (type == null ? LazyType.TRUE : type).name();
  }

  public Collection<String> getCascadeTypes() {
    final List<CascadeType> value = getCascade().getValue();
    return getCascadeTypes(value);
  }

  static Collection<String> getCascadeTypes(final List<CascadeType> value) {
    if (value == null) return Collections.emptyList();
    return ContainerUtil.map2Set(value, new Function<CascadeType, String>() {
      public String fun(final CascadeType cascadeType) {
        return cascadeType.getDisplayName();
      }
    });
  }

}