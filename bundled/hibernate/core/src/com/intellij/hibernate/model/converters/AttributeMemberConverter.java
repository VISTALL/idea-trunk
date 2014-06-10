package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.model.PersistentEmbeddedAttribute;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.converters.AbstractMemberResolveConverter;
import com.intellij.jpa.util.JpaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 */
public class AttributeMemberConverter extends AbstractMemberResolveConverter {

  protected boolean methodSuits(final PsiMethod psiMethod) {
    return PropertyUtil.isSimplePropertyGetter(psiMethod) && !psiMethod.hasModifierProperty(PsiModifier.FINAL) && super.methodSuits(psiMethod);
  }

  protected boolean fieldSuits(final PsiField psiField) {
    return !psiField.hasModifierProperty(PsiModifier.FINAL) && !psiField.hasModifierProperty(PsiModifier.TRANSIENT) && super.fieldSuits(psiField);
  }

  @Nullable
  protected PsiClass getTargetClass(final ConvertContext context) {
    final DomElement parent = context.getInvocationElement().getParent();
    for (DomElement cur = parent instanceof PersistentAttribute? parent.getParent() : parent; cur != null; cur = cur.getParent()) {
      if (cur instanceof HbmPersistentObjectBase) {
        return ((HbmPersistentObjectBase)cur).getClazz().getValue();
      }
      else if (cur instanceof HbmEmbeddedAttributeBase) {
        if (cur instanceof HbmCompositeId && !HibernateUtil.isEmbedded((HbmCompositeId)cur)) continue;
        return PersistenceCommonUtil.getTargetClass((PersistentEmbeddedAttribute)cur);
      }
    }
    return null;
  }

  @NotNull
  protected PropertyMemberType[] getMemberTypes(final ConvertContext context) {
    final PersistentAttribute attributeBase = DomUtil.getParentOfType(context.getInvocationElement(), PersistentAttribute.class, false);
    final HbmContainer container = attributeBase != null? null : DomUtil.getParentOfType(context.getInvocationElement(), HbmContainer.class, false);

    final boolean isField;
    if (attributeBase != null) {
      isField = attributeBase.getAttributeModelHelper().isFieldAccess();
    }
    else if (container != null) {
      final AccessType type = container.getAccess().getValue();
      isField = type == AccessType.FIELD || type == null && isDefaultAccessField(container);
    }
    else {
      isField = false;
    }
    return new PropertyMemberType[]{isField ? PropertyMemberType.FIELD : PropertyMemberType.GETTER};
  }

  @NotNull
  protected PsiType getPsiType(final ConvertContext context) {
    final DomElement invocationElement = context.getInvocationElement();
    final DomElement parent = invocationElement.getParent();
    final PersistentAttribute attributeBase;
    if (parent instanceof HbmAttributeBase) {
      attributeBase = (PersistentAttribute)parent;
    }
    else if (parent instanceof HbmContainer) {
      attributeBase = ((HbmContainer)parent).getContainedAttribute();
    }
    else {
      attributeBase = null;
    }
    if (attributeBase == null) return super.getPsiType(context);
    return JpaUtil.getAttributeTypeOrDefault(attributeBase);
  }

  public static boolean isDefaultAccessField(final DomElement element) {
    final HbmHibernateMapping mapping = element.getParentOfType(HbmHibernateMapping.class, false);
    return mapping != null && mapping.getDefaultAccess().getValue() == AccessType.FIELD;
  }
}
