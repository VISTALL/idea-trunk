/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.factories.resolvers;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.factories.ObjectTypeResolver;
import com.intellij.spring.factories.SpringFactoryBeansManager;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.util.xml.converters.values.BooleanValueConverter;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Serega Vasiliev, Taras Tielkes
 */
public abstract class AbstractTypeResolver implements ObjectTypeResolver {
  @NonNls private static final String CLASS_ARRAY_EDITOR_SEPARATOR = ",";

  @Nullable
  protected static String getPropertyValue(@NotNull final CommonSpringBean bean, @NotNull final String propertyName) {
    if (bean instanceof SpringBean) {
      final SpringPropertyDefinition property = SpringUtils.findPropertyByName((SpringBean)bean, propertyName);
      if (property != null) {
        final String value = SpringUtils.getStringPropertyValue(property);
        if (value != null) return value;
      }
    }
    return null;
  }

  @NotNull
  protected static Set<String> getListOrSetValues(@NotNull final SpringBean bean, @NotNull String propertyName) {
    final SpringPropertyDefinition property = SpringUtils.findPropertyByName(bean, propertyName);
    if (property != null) {
      return SpringUtils.getListOrSetValues(property);
    }
    return Collections.emptySet();
  }

  // @see org.springframework.beans.propertyeditors.ClassArrayEditor.setAsText(String text)
  @NotNull
  protected static Set<String> getTypesFromClassArrayProperty(@NotNull final SpringBean context, final String propertyName) {
    final SpringPropertyDefinition property = SpringUtils.findPropertyByName(context, propertyName);
    if (property != null) {
      final String stringValue = SpringUtils.getStringPropertyValue(property);
      if (stringValue != null) {
        return splitAndTrim(stringValue, CLASS_ARRAY_EDITOR_SEPARATOR);
      } else {
        return SpringUtils.getListOrSetValues(property);
      }
    }
    return Collections.emptySet();
  }

  @NotNull
  private static Set<String> splitAndTrim(@NotNull String value, @NotNull String separator) {
    final List<String> parts = StringUtil.split(value, separator);
    final Set<String> trimmedParts = new HashSet<String>(parts.size());
    for (String part : parts) {
      trimmedParts.add(part.trim());
    }
    return trimmedParts;
  }

  protected static boolean isBooleanProperySetAndTrue(@NotNull final SpringBean context, @NotNull final String propertyName) {
    final String value = getPropertyValue(context, propertyName);
    return value != null && BooleanValueConverter.getInstance(true).isTrue(value);
  }

  protected static boolean isBooleanProperySetAndFalse(@NotNull final SpringBean context, @NotNull final String propertyName) {
    final String value = getPropertyValue(context, propertyName);
    return value != null && !BooleanValueConverter.getInstance(true).isTrue(value);
  }

  @Nullable
  protected static PsiClassType getTypeFromProperty(@NotNull final SpringBean context, @NotNull final String propertyName) {
    final SpringPropertyDefinition targetProperty = SpringUtils.findPropertyByName(context, propertyName);

    if (targetProperty != null) {
      if (targetProperty instanceof SpringProperty) {
        // support chained FactoryBean resolving only for inner beans
        final SpringProperty property = (SpringProperty)targetProperty;
        final SpringBean bean = property.getBean();
        if (DomUtil.hasXml(bean)) {
          final PsiClass[] classes = SpringUtils.getEffectiveBeanTypes(bean);
          final PsiManager psiManager = bean.getPsiManager();
          if (classes.length > 0 && psiManager != null) {
            return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(classes[0]);
          }
        }
      }
      return getTypeFromNonFactoryBean(SpringUtils.getReferencedSpringBean(targetProperty));
    }
    return null;
  }

  @Nullable
  protected static PsiClassType getTypeFromBeanName(@NotNull SpringBean context, @NotNull String beanName) {
    final SpringModel model = SpringUtils.getSpringModel(context);
    return getTypeFromNonFactoryBean(model.findBean(beanName));
  }

  @Nullable
  private static PsiClassType getTypeFromNonFactoryBean(@Nullable final SpringBeanPointer bean) {
    // chained FactoryBean resolving is not supported for top-level beans (to avoid circularity handling)
    if (bean != null) {
      final PsiClass targetBeanClass = bean.getBeanClass();
      if (targetBeanClass != null && !SpringFactoryBeansManager.isBeanFactory(targetBeanClass)) {
        final PsiManager psiManager = bean.getPsiManager();
        if (psiManager != null) {
          return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(targetBeanClass);
        }
      }
    }
    return null;
  }
}
