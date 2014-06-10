/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.containers.ComparatorUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringPropertyImpl extends SpringInjectionImpl implements SpringProperty {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    final List<BeanProperty> properties = getName().getValue();
    if (properties == null) return Collections.emptyList();
    final ArrayList<PsiType> list = new ArrayList<PsiType>(properties.size());
    for (BeanProperty property : properties) {
      final PsiType psiType = property.getPropertyType();

      if (psiType instanceof PsiClassReferenceType) {
        final DomSpringBean bean = getParentOfType(DomSpringBean.class, false);
        if (bean != null) {
          final PsiClass derivedClass = bean.getBeanClass();
          if (derivedClass != null) {
            final PsiClass superClass = PsiTreeUtil.getParentOfType(property.getMethod(), PsiClass.class);
            if (superClass != null && derivedClass.isInheritor(superClass, true)) {
              final PsiSubstitutor superClassSubstitutor =
                TypeConversionUtil.getSuperClassSubstitutor(superClass, derivedClass, PsiSubstitutor.EMPTY);

              list.add(superClassSubstitutor.substitute(psiType));
            }
          }
        }
      }
      list.add(psiType);
    }

    return list;
  }

  @NonNls
  public String getPropertyName() {
    return getName().getStringValue();
  }

  public int hashCode() {
    final String name = getPropertyName();
    return name == null ? 0 : name.hashCode();
  }

  public boolean equals(final Object obj) {
    return obj instanceof SpringProperty && ComparatorUtil.equalsNullable(getPropertyName(), ((SpringProperty)obj).getPropertyName());
  }
}
