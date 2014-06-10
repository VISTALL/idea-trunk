/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.values.converters;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.converters.SpringConverterUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FieldRetrievingFactoryBeanConverter extends Converter<String> implements CustomReferenceConverter<String> {
  private static final String FIELD_RETRIVING_FACTORY_BEAN_CLASS = "org.springframework.beans.factory.config.FieldRetrievingFactoryBean";
  @NonNls private static final String STATIC_FIELD_PROPERTY_NAME = "staticField";
  private final boolean mySoft;


  public FieldRetrievingFactoryBeanConverter() {
    this(true);
  }

  public FieldRetrievingFactoryBeanConverter(boolean soft) {
    mySoft = soft;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    return createReferences(genericDomValue, element);
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue, final PsiElement element) {
    final String stringValue = genericDomValue.getStringValue();
    if (stringValue == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    List<PsiReference> collectedReferences = new ArrayList<PsiReference>();

    final JavaClassReferenceProvider provider = new JavaClassReferenceProvider(element.getProject());
    provider.setSoft(mySoft);
    final PsiReference[] javaClassReferences = provider.getReferencesByElement(element);

    PsiClass psiClass = null;
    for (PsiReference reference : javaClassReferences) {
      final PsiElement psiElement = reference.resolve();
      if (psiElement == null) break;


      collectedReferences.add(reference);
      if (psiElement instanceof PsiClass) {
        psiClass = (PsiClass)psiElement;
      }
    }

    if (psiClass == null || stringValue.endsWith(psiClass.getName())) return javaClassReferences;

    collectedReferences.add(createFieldReference(psiClass, element, stringValue, genericDomValue));

    return collectedReferences.toArray(new PsiReference[collectedReferences.size()]);
  }

  private PsiReference createFieldReference(final PsiClass psiClass,
                                            final PsiElement element,
                                            final String stringValue,
                                            final GenericDomValue<String> genericDomValue) {
    final String className = psiClass.getName();
    assert className != null;
    final int i = stringValue.indexOf(className) + className.length();
    final String fieldName = stringValue.substring(i + 1).trim();

    final TextRange textRange = fieldName.length() == 0
                                ? TextRange.from(element.getText().indexOf(className) + className.length() + 1, 0)
                                : TextRange.from(element.getText().indexOf(fieldName), fieldName.length());

    return new PsiReferenceBase<PsiElement>(element, textRange, mySoft) {
      public PsiElement resolve() {
        if (fieldName.length() != 0) {
          final PsiField[] psiFields = psiClass.getFields();
          for (PsiField psiField : psiFields) {
            if (psiField.hasModifierProperty(PsiModifier.PUBLIC) &&
                psiField.hasModifierProperty(PsiModifier.STATIC) &&
                fieldName.equals(psiField.getName())) {
              return psiField;
            }
          }
        }
        return null;
      }

      @Override
      public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
        if (element instanceof PsiField) {
          final PsiField field = (PsiField)element;
          genericDomValue.setStringValue(field.getContainingClass().getQualifiedName() + "." + field.getName());
        }
        return getElement();
      }

      public Object[] getVariants() {
        List<String> staticFields = new ArrayList<String>();
        final PsiField[] psiFields = psiClass.getFields();
        for (PsiField psiField : psiFields) {
          if (psiField.hasModifierProperty(PsiModifier.PUBLIC) &&
              psiField.hasModifierProperty(PsiModifier.STATIC) &&
              psiField.getName() != null) {
            staticFields.add(psiField.getName());
          }
        }
        return ArrayUtil.toStringArray(staticFields);
      }
    };
  }

  public String fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return s;
  }

  public String toString(@Nullable String s, final ConvertContext context) {
    return s;
  }


  public static class FactoryClassCondition implements Condition<GenericDomValue> {
    public boolean value(GenericDomValue context) {
      return checkbeanClass(context);
    }
  }

  public static class FactoryClassAndPropertyCondition implements Condition<Pair<PsiType, GenericDomValue>> {
    public boolean value(Pair<PsiType, GenericDomValue> pair) {
      final GenericDomValue element = pair.getSecond();
      return checkbeanClass(element) && checkPropertyName(element);
    }
  }

  private static boolean checkbeanClass(DomElement element) {
    return isFieldRetrivingFactoryBean(SpringConverterUtil.getCurrentBean(element));
  }

  private static boolean checkPropertyName(DomElement element) {
    final SpringProperty springProperty = element.getParentOfType(SpringProperty.class, false);
    return springProperty != null && STATIC_FIELD_PROPERTY_NAME.equals(springProperty.getName().getStringValue());
  }

  public static boolean isFieldRetrivingFactoryBean(@Nullable CommonSpringBean springBean) {
    if (springBean == null) return false;

    final PsiClass beanClass = springBean.getBeanClass();

    return beanClass != null && FIELD_RETRIVING_FACTORY_BEAN_CLASS.equals(beanClass.getQualifiedName());
  }

  public static boolean isResolved(final Project project, String field) {
    final PsiManager psiManager = PsiManager.getInstance(project);

    final int index = field.lastIndexOf(".");
    if (index <= 0) return false;

    final String className = field.substring(0, index);
    final String fieldName = field.substring(index + 1);

    if (StringUtil.isEmpty(fieldName) || StringUtil.isEmpty(className)) return false;

    final PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(className, GlobalSearchScope.allScope(project));
    if (psiClass != null) {
      for (PsiField psiField : psiClass.getFields()) {
        if (psiField.hasModifierProperty(PsiModifier.STATIC) && fieldName.equals(psiField.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
