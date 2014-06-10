package com.intellij.hibernate.springIntegration.converters;

import com.intellij.hibernate.HibernateConvertersRegistry;
import com.intellij.hibernate.HibernateManager;
import com.intellij.hibernate.model.xml.config.Property;
import com.intellij.hibernate.springIntegration.IntegrationUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.Prop;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.WrappingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HibernatePropertyValueConverter extends WrappingConverter implements Condition<Pair<PsiType, GenericDomValue>> {
  @NonNls private static final String HIBERNATE_PROPERTIES = "hibernateProperties";

  @Nullable
  public Converter<?> getConverter(@NotNull final GenericDomValue domElement) {
    final Prop prop = domElement.getParentOfType(Prop.class, false);
    if (prop != null) {
      final String value = prop.getKey().getStringValue();
      if (value != null) {
        final HibernateConvertersRegistry convertersRegistry =
          HibernateManager.getInstance(domElement.getManager().getProject()).getConvertersRegistry();
        return convertersRegistry.getConverter(Property.class, value);
      }
    }
    return null;
  }

  public boolean value(final Pair<PsiType, GenericDomValue> pair) {
    final GenericDomValue domValue = pair.getSecond();
    final SpringProperty springProperty = domValue.getParentOfType(SpringProperty.class, false);
    if (springProperty != null && HIBERNATE_PROPERTIES.equals(springProperty.getName().getStringValue())) {
      final SpringBean springBean = springProperty.getParentOfType(SpringBean.class, false);
      if (springBean != null && IntegrationUtil.isSessionFactoryBean(springBean, false, true)) {
        return true;
      }
    }
    return false;
  }

}
