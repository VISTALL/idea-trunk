/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.ide.IdeBundle;
import com.intellij.psi.*;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.ResolvedConstructorArgs;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class ConstructorArgIndexConverter implements CustomReferenceConverter<Integer> {

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<Integer> index, final PsiElement element, final ConvertContext context) {

    final PsiReferenceBase<PsiElement> ref = new MyReference(element, index, context);
    return new PsiReference[] { ref };
  }

  private static class MyReference extends PsiReferenceBase<PsiElement> implements EmptyResolveMessageProvider {
    private final GenericDomValue<Integer> myGenericDomValue;
    private final ConvertContext myContext;

    public MyReference(final PsiElement element, final GenericDomValue<Integer> index, final ConvertContext context) {
      super(element);
      myGenericDomValue = index;
      myContext = context;
    }

    public PsiParameter resolve() {
      final SpringBean bean = (SpringBean)SpringConverterUtil.getCurrentBean(myContext);
      return ConstructorArgIndexConverter.resolve(myGenericDomValue, bean);
    }

    public boolean isSoft() {
      return true;
    }

    public Object[] getVariants() {
      final SpringBean bean = (SpringBean)SpringConverterUtil.getCurrentBean(myContext);
      final List<PsiMethod> psiMethods = SpringBeanUtil.getInstantiationMethods(bean);
      int maxParams = 0;
      for (PsiMethod method: psiMethods) {
        final PsiParameterList parameterList = method.getParameterList();
        maxParams = Math.max(maxParams, parameterList.getParametersCount());
      }
      if (maxParams > 0) {
        final Object[] objects = new Object[maxParams];
        for (int i = 0; i < maxParams; i++) {
          // todo apply more descriptive completion variants
          objects[i] = Integer.toString(i);
        }
        return objects;
      }
      return EMPTY_ARRAY;
    }

    public String getUnresolvedMessagePattern() {
      final Integer value = myGenericDomValue.getValue();
      if (value != null) {
        final SpringBean bean = (SpringBean)SpringConverterUtil.getCurrentBean(myContext);
        final PsiClass clazz = SpringBeanUtil.getInstantiationClass(bean);
        if (clazz != null) {
          return SpringBeanUtil.isInstantiatedByFactory(bean) ?
                 SpringBundle.message("cannot.find.factory.method.index", value, clazz.getQualifiedName()):
                 SpringBundle.message("cannot.find.constructor.arg.index.in.class", value, clazz.getQualifiedName());
        }
        return SpringBundle.message("cannot.find.constructor.arg.index", value);
      } else {
        return IdeBundle.message("value.should.be.integer");
      }
    }
  }

  @Nullable
  public static PsiParameter resolve(final GenericDomValue<Integer> i, SpringBean bean) {
    final Integer value = i.getValue();
    if (value != null) {
      int index = value.intValue();
      if (index >= 0) {
        final ResolvedConstructorArgs resolvedArgs = bean.getResolvedConstructorArgs();
        final PsiMethod resolvedMethod = resolvedArgs.getResolvedMethod();
        if (resolvedMethod != null) {
          return resolvedArgs.getResolvedArgs(resolvedMethod).get(i.getParent());
        } else {
          final List<PsiMethod> checkedMethods = resolvedArgs.getCheckedMethods();
          if (checkedMethods != null) {
            for (PsiMethod method: checkedMethods) {
              final PsiParameterList parameterList = method.getParameterList();
              if (parameterList.getParametersCount() > index) {
                return parameterList.getParameters()[index];
              }
            }
          }
        }
      }
    }
    return null;

  }
}
