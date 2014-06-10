package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.hibernate.model.enums.NotFoundType;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.persistence.model.RelationshipType;
import com.intellij.persistence.model.TableInfoProvider;
import com.intellij.persistence.model.helpers.PersistentRelationshipAttributeModelHelper;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.reflect.DomAttributeChildDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmAnyToManyImpl extends HbmCollectionAttributeBaseImpl implements HbmRelationAttributeBase.AnyToManyBase,
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
    return this instanceof HbmOneToMany? RelationshipType.ONE_TO_MANY : RelationshipType.MANY_TO_MANY;
  }

  public boolean isRelationshipSideOptional(final boolean thisSide) {
    return thisSide || getNotFound().getValue() == NotFoundType.IGNORE;
  }

  @Nullable
  public String getMappedByAttributeName() {
    if (this instanceof HbmRelationAttributeBase.NonOneToManyBase) {
      return ((HbmRelationAttributeBase.NonOneToManyBase)this).getPropertyRef().getStringValue();
    }
    return null;
  }

  public TableInfoProvider getTableInfoProvider() {
    return getParentOfType(HbmTableInfoProvider.class, true);
  }

  public boolean isInverseSide() {
    final HbmContainer container = getParentOfType(HbmContainer.class, false);
    if (container != null) {
      final DomAttributeChildDescription childDescription = container.getGenericInfo().getAttributeChildDescription("inverse");
      if (childDescription != null) {
        final GenericAttributeValue value = childDescription.getDomAttributeValue(container);
        return value != null && Boolean.TRUE.equals(value.getValue());
      }
    }
    return false;
  }

  public String getFetchType() {
    final LazyType type = getLazy().getValue();
    return (type == null ? LazyType.TRUE : type).name();
  }

  public Collection<String> getCascadeTypes() {
    return HbmAnyToOneImpl.getCascadeTypes(getCascade().getValue());
  }
}
