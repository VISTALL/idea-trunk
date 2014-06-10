package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.springIntegration.converters.HibernatePropertyNameConverter;
import com.intellij.hibernate.springIntegration.converters.HibernatePropertyValueConverter;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.spring.SpringManager;
import com.intellij.spring.model.converters.CustomConverterRegistry;
import com.intellij.spring.model.converters.PropertyKeyConverter;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.spring.model.values.converters.ResourceValueConverter;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.PairProcessor;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class SpringIntegrationProjectComponent implements ProjectComponent {
  private final Project myProject;

  public SpringIntegrationProjectComponent(Project project) {
    myProject = project;
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return SpringIntegrationProjectComponent.class.getName();
  }

  public void initComponent() {
    final CustomConverterRegistry converterRegistry = SpringManager.getInstance(myProject).getCustomConverterRegistry();

    final HibernatePropertyNameConverter converter = new HibernatePropertyNameConverter();
    converterRegistry.registryConverter(PropertyKeyConverter.class, Pair.<Condition<GenericDomValue>, Converter>create(converter, converter));

    final HibernatePropertyValueConverter propertyValueConverter = new HibernatePropertyValueConverter();
    SpringManager.getInstance(myProject).getValueProvidersRegistry().registerConverter(propertyValueConverter, propertyValueConverter);

    SpringManager.getInstance(myProject).getValueProvidersRegistry().registerConverter(new ResourceValueConverter() {
      @NotNull
      public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
        final ArrayList<PsiReference> result = new ArrayList<PsiReference>();
        final int startInElement = ElementManipulators.getOffsetInElement(element);
        ResourceResolverUtils.processSeparatedString(genericDomValue.getStringValue(), ",", new PairProcessor<String, Integer>() {
          public boolean process(final String s, final Integer offset) {
            result.addAll(Arrays.asList(ResourceResolverUtils.getClassPathReferences(element, s, offset+startInElement, true)));
            return true;
          }
        });
        return result.isEmpty()? PsiReference.EMPTY_ARRAY : result.toArray(new PsiReference[result.size()]);
      }
    }, new Condition<Pair<PsiType, GenericDomValue>>() {
      public boolean value(final Pair<PsiType, GenericDomValue> pair) {
        final GenericDomValue domValue = pair.getSecond();
        if (!IntegrationUtil.isSessionFactoryBean(domValue.getParentOfType(SpringBean.class, true), false, true)) return false;
        final SpringProperty property = domValue.getParentOfType(SpringProperty.class, true);
        final String name = property == null ? null : property.getName().getStringValue();
        return SpringBeanPersistenceUnit.MAPPING_RESOURCES.equals(name);
      }
    });

    // add converters for "String[]" properties
    SpringManager.getInstance(myProject).getValueProvidersRegistry().registerConverter(new ResourceValueConverter() {
      @NotNull
      public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
        final ArrayList<PsiReference> result = new ArrayList<PsiReference>();
        final int startInElement = ElementManipulators.getOffsetInElement(element);
        ResourceResolverUtils.processSeparatedString(genericDomValue.getStringValue(), ",", new PairProcessor<String, Integer>() {
          public boolean process(final String s, final Integer offset) {
            result.addAll(new PackageReferenceSet(s, element, offset+ startInElement).getReferences());
            return true;
          }
        });
        return result.isEmpty()? PsiReference.EMPTY_ARRAY : result.toArray(new PsiReference[result.size()]);
      }
    }, new Condition<Pair<PsiType, GenericDomValue>>() {
      public boolean value(final Pair<PsiType, GenericDomValue> pair) {
        final GenericDomValue domValue = pair.getSecond();
        if (!IntegrationUtil.isSessionFactoryBean(domValue.getParentOfType(SpringBean.class, true), true, true)) return false;
        final SpringProperty property = domValue.getParentOfType(SpringProperty.class, true);
        final String name = property == null ? null : property.getName().getStringValue();
        return SpringBeanPersistenceUnit.ANNOTATED_PACKAGES.equals(name);
      }
    });
  }

  public void disposeComponent() {
  }
}
