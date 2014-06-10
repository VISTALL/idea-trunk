/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.TypeHolder;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringBeanUtil {

  private SpringBeanUtil() {
  }

  @Nullable
  public static PsiClass getInstantiationClass(SpringBean bean) {
    final PsiMethod factoryMethod = bean.getFactoryMethod().getValue();
    if (factoryMethod != null) {
      return SpringBeanFactoryMethodConverter.getFactoryClass(bean);
    }
    return bean.getBeanClass();
  }

  public static boolean isInstantiatedByFactory(SpringBean bean) {
    return bean.getFactoryMethod().getValue() != null;
  }

  @NotNull
  public static List<PsiMethod> getInstantiationMethods(final SpringBean springBean) {
    final String factoryMethod = springBean.getFactoryMethod().getStringValue();
    if (factoryMethod != null) {
      return SpringBeanFactoryMethodConverter.getFactoryMethodCandidates(springBean, factoryMethod);
    }
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      return Arrays.asList(beanClass.getConstructors()); //private constructor is accepted
    }
    return Collections.emptyList();
  }

  @Nullable
  public static PsiType getRequiredType(@NotNull TypeHolder valueHolder) {
    final List<? extends PsiType> psiTypes = valueHolder.getRequiredTypes();
    return psiTypes.isEmpty() ? null : psiTypes.get(0);
  }

  @Nullable
  public static PsiClassType getRequiredClass(@NotNull TypeHolder valueHolder) {
    final PsiType injectionType = getRequiredType(valueHolder);
    if (injectionType instanceof PsiClassType) {
      return (PsiClassType)injectionType;
    }
    return null;
  }

  @Nullable
  public static DomSpringBean getTargetSpringBean() {
    return getTargetSpringBean(DataManager.getInstance().getDataContext());
  }

  public static DomSpringBean getTargetSpringBean(final DataContext dataContext) {
    final Editor editor = (Editor)dataContext.getData(DataConstants.EDITOR);
    if (editor != null) {
      final PsiElement targetPsiElement = TargetElementUtilBase.findTargetElement(editor, TargetElementUtilBase.REFERENCED_ELEMENT_ACCEPTED);
      if (targetPsiElement instanceof XmlTag) {
        final DomElement value = DomManager.getDomManager(targetPsiElement.getProject()).getDomElement((XmlTag)targetPsiElement);
        if (value instanceof DomSpringBean) {
          return (DomSpringBean)value;
        }
      }
    }
    final DomElement element = DomUtil.getContextElement(editor);

    return element instanceof DomSpringBean && !(element instanceof CustomBeanWrapper) ? (DomSpringBean)element : null;
  }

}
