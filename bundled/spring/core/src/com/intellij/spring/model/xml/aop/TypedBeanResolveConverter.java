/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.aop;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomJavaUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TypedBeanResolveConverter extends SpringBeanResolveConverter {

  @Nullable
  public List<PsiClassType> getRequiredClasses(final ConvertContext context) {
    final DomElement element = context.getInvocationElement();
    final RequiredBeanType type = element.getAnnotation(RequiredBeanType.class);
    assert type != null;
    final PsiClass aClass = DomJavaUtil.findClass(type.value(), context.getFile(), context.getModule(), null);
    return aClass == null ? null : Arrays.asList(JavaPsiFacade.getInstance(context.getFile().getProject()).getElementFactory().createType(aClass));
  }
}
