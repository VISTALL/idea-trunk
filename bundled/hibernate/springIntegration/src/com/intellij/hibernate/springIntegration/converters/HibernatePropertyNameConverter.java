package com.intellij.hibernate.springIntegration.converters;

import com.intellij.hibernate.HibernateConvertersRegistry;
import com.intellij.hibernate.HibernateManager;
import com.intellij.hibernate.model.xml.config.Property;
import com.intellij.hibernate.springIntegration.IntegrationUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.spring.model.xml.beans.Prop;
import com.intellij.spring.model.xml.beans.Props;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HibernatePropertyNameConverter extends ResolvingConverter<String> implements Condition<GenericDomValue> {

  @NonNls private static final String HIBERNATE_PROPERTIES = "hibernateProperties";

  @NotNull
  public Collection<? extends String> getVariants(final ConvertContext context) {
    final HibernateConvertersRegistry convertersRegistry =
      HibernateManager.getInstance(context.getPsiManager().getProject()).getConvertersRegistry();

    final Map<String, Converter> converters = convertersRegistry.getConverters(Property.class);
    List<String> definedProperties = getDefinedPropertyKeys(context);

    final Set<String> allPropertyNames = converters.keySet();
    allPropertyNames.removeAll(definedProperties);

    return allPropertyNames;
  }

  private static List<String> getDefinedPropertyKeys(final ConvertContext context) {
    List<String> keys = new ArrayList<String>();
    final Props props = context.getInvocationElement().getParentOfType(Props.class, false);
    if (props != null) {
      for (Prop prop : props.getProps()) {
        final String value = prop.getKey().getStringValue();
        if (StringUtil.isNotEmpty(value)) {
          keys.add(value);
        }
      }
    }
    return keys;
  }

  public String fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    return s;
  }

  public String toString(@Nullable final String s, final ConvertContext context) {
    return s;
  }

  public boolean value(final GenericDomValue domValue) {
    final SpringProperty springProperty = domValue.getParentOfType(SpringProperty.class, false);
    if (springProperty != null && HIBERNATE_PROPERTIES.equals(springProperty.getName().getStringValue())) {
      final SpringBean springBean = springProperty.getParentOfType(SpringBean.class, false);
      return IntegrationUtil.isSessionFactoryBean(springBean, false, true);
    }
    return false;
  }

}
