/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.values.converters.FieldRetrievingFactoryBeanConverter;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringValueHolderDefinition;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class SpringBeanIdConverter implements CustomReferenceConverter<String> {

  private static final FieldRetrievingFactoryBeanConverter.FactoryClassCondition CONDITION = new FieldRetrievingFactoryBeanConverter.FactoryClassCondition();
  private static final FieldRetrievingFactoryBeanConverter CONVERTER = new FieldRetrievingFactoryBeanConverter(true);

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    if (genericDomValue.getParent() instanceof CustomBeanWrapper) return PsiReference.EMPTY_ARRAY;
    if (CONDITION.value((GenericDomValue)context.getInvocationElement())) {
      return CONVERTER.createReferences(genericDomValue, element, context);
    }
    return createDefaultReferences(genericDomValue, element);
  }

  private static PsiReference[] createDefaultReferences(final GenericDomValue<String> genericDomValue, final PsiElement element) {
    return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {

      public PsiElement resolve() {
        return getElement().getParent().getParent();
      }

      public boolean isSoft() {
        return true;
      }

      public Object[] getVariants() {
        final DomSpringBean springBean = genericDomValue.getParentOfType(DomSpringBean.class, false);

        List<String> names = suggestUnusedBeanNames(springBean);

        names.addAll(Arrays.asList(SpringUtils.suggestBeanNames(springBean)));

        return ArrayUtil.toStringArray(names);
      }

      public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
        return element;
      }
    }};
  }

  private static List<String> suggestUnusedBeanNames(final DomSpringBean springBean) {
    PsiClass beanClass = springBean.getBeanClass();
    PsiType psiType = beanClass == null ? null : JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory().createType(beanClass);

    SpringModel model = SpringUtils.getSpringModel(springBean);
    Collection<? extends SpringBeanPointer> list = model.getAllCommonBeans(true);

    List<String> unusedReferences = new ArrayList<String>();

    for (SpringBeanPointer pointer : list) {
      final CommonSpringBean bean = pointer.getSpringBean();
      if (bean instanceof DomSpringBean) {
        DomSpringBean domSpringBean = (DomSpringBean)bean;
        for (SpringValueHolderDefinition definition : DomUtil.getDefinedChildrenOfType(domSpringBean, SpringValueHolderDefinition.class)) {
          GenericDomValue<SpringBeanPointer> refElement = definition.getRefElement();
          if (refElement != null && !StringUtil.isEmptyOrSpaces(refElement.getStringValue()) && refElement.getValue() == null) {
            String unusedBeanRef = refElement.getStringValue();
            if (psiType != null) {
              PsiType requiredType = SpringBeanUtil.getRequiredType(definition);
              if (requiredType != null && requiredType.isAssignableFrom(psiType)) {
                 unusedReferences.add(unusedBeanRef);
              }
            }  else {
              unusedReferences.add(unusedBeanRef);
            }
          }
        }
      }
    }

    return unusedReferences;
  }
}
