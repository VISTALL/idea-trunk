/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.Function;
import com.intellij.util.NullableFunction;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class IntegrationUtil {

  @NonNls static final List<Pair<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>>> myAnnoProviders = new ArrayList<Pair<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>>>();
  @NonNls static final List<Pair<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>>> myProviders = new ArrayList<Pair<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>>>();
  static {
    final Pair<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>> annoProvider = Pair.<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>>create(
      SpringBeanPersistenceUnit.ANNO_SESSION_FACTORY, new NullableFunction<Pair<SpringBean, HibernateFacet>, PersistencePackage>() {
      public PersistencePackage fun(final Pair<SpringBean, HibernateFacet> pair) {
        return IntegrationUtil.getPersistencePackage(pair.getFirst(), pair.getSecond(), true);
      }
    });
    myAnnoProviders.add(annoProvider);
    myProviders.add(annoProvider);
    final NullableFunction<Pair<SpringBean, HibernateFacet>, PersistencePackage> basicProvider =
      new NullableFunction<Pair<SpringBean, HibernateFacet>, PersistencePackage>() {
        public PersistencePackage fun(final Pair<SpringBean, HibernateFacet> pair) {
          return IntegrationUtil.getPersistencePackage(pair.getFirst(), pair.getSecond(), false);
        }
      };
    myProviders.add(Pair.<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>>create(SpringBeanPersistenceUnit.SESSION_FACTORY, basicProvider));
    myProviders.add(Pair.<String, Function<Pair<SpringBean, HibernateFacet>, PersistencePackage>>create(SpringBeanPersistenceUnit.SESSION_FACTORY_1_0, basicProvider));
  }

  @Nullable
  public static <T> T getProvider(final SpringBaseBeanPointer bean, final List<Pair<String, T>> providers, final boolean allowAbstract) {
    if (!allowAbstract && bean.isAbstract()) return null;
    final PsiClass beanClass = bean.getBeanClass();
    final String beanClassName = beanClass != null? beanClass.getQualifiedName() : bean.getSpringBean() instanceof SpringBean ? ((SpringBean)bean.getSpringBean()).getClazz().getStringValue() : null;
    for (Pair<String, T> pair : providers) {
      final String name = pair.getFirst();
      if (name.equals(beanClassName)) return pair.getSecond();
      if (JamCommonUtil.isSuperClass(beanClass, name)) return pair.getSecond();
    }
    return null;
  }

  public static PersistencePackage getPersistencePackage(final SpringBean bean, final HibernateFacet facet, final boolean supportsAnnotation) {
    return new SpringBeanPersistenceUnit(bean, facet.getModule(), supportsAnnotation);
  }

  public static boolean isSessionFactoryBean(@Nullable final SpringBean bean, final boolean annotationOnly, final boolean allowAbstract) {
    return bean != null && isSessionFactoryBean(DomSpringBeanPointer.createDomSpringBeanPointer(bean), annotationOnly, allowAbstract);
  }

  public static boolean isSessionFactoryBean(@Nullable final DomSpringBeanPointer bean, final boolean annotationOnly, final boolean allowAbstract) {
    return bean != null && getProvider(bean, annotationOnly? myAnnoProviders : myProviders, allowAbstract) != null;
  }

  public static SpringPropertyDefinition setBeanProperty(final SpringBean bean, final String propertyName, final String propertyAlias, final String propertyValue, final boolean isList) {
    SpringPropertyDefinition property = SpringUtils.findPropertyByName(bean, propertyName, false);
    if (property == null && propertyAlias != null) {
      property = SpringUtils.findPropertyByName(bean, propertyAlias, false);
    }
    return setBeanProperty(bean, property, propertyName, propertyValue, isList);
  }

  public static SpringPropertyDefinition setBeanProperty(final SpringBean bean,
                                                          SpringPropertyDefinition property,
                                                          final String propertyName,
                                                          final String propertyValue,
                                                          final boolean isList) {
    if (!(property instanceof SpringProperty)) {
      if (property != null) property.undefine();
      property = bean.addProperty();
      ((SpringProperty)property).getName().setStringValue(propertyName);
    }
    setPropertyValue((SpringProperty)property, propertyValue, isList);
    return property;
  }

  @Nullable
  public static Prop findProp(final Props props, final String propertyName, final String propertyAlias) {
    for (Prop prop : props.getProps()) {
      final String value = prop.getKey().getValue();
      if (Comparing.strEqual(value, propertyName) || Comparing.strEqual(value, propertyAlias)) {
        return prop;
      }
    }
    return null;
  }

  public static void setBeanProperty(final Props props, final String propertyName, final String propertyAlias, final String propertyValue) {
    Prop prop = findProp(props, propertyName, propertyAlias);
    if (prop == null) {
      prop = props.addProp();
      prop.getKey().setStringValue(propertyName);
    }
    prop.setStringValue(propertyValue);
  }

  private static void setPropertyValue(final SpringProperty property, final String stringValue, final boolean isList) {
    final ListOrSet listOrSet = property.getList();
    if (!DomUtil.hasXml(listOrSet)) {
      {
        final String valueAttrString;
        final GenericAttributeValue<String> valueAttr = property.getValueAttr();
        final XmlAttribute valueAttrElement = valueAttr.getXmlAttribute();
        valueAttrString = valueAttr.getStringValue();
        if (valueAttrElement != null && StringUtil.isNotEmpty(valueAttrString)) {
          if (!isList) {
            valueAttr.setStringValue(stringValue);
            return;
          }
          listOrSet.addValue().setStringValue(valueAttrString);
          valueAttr.undefine();
        }
      }
      {
        final SpringValue value = property.getValue();
        final XmlTag valueElement = value.getXmlTag();
        final String valueString = value.getStringValue();
        if (valueElement != null && StringUtil.isNotEmpty(valueString)) {
          if (!isList) {
            value.setStringValue(stringValue);
            return;
          }
          listOrSet.addValue().setStringValue(valueString);
          value.undefine();
        }
      }
    }
    listOrSet.addValue().setStringValue(stringValue);
  }
}
