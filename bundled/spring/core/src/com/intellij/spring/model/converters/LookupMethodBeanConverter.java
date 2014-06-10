/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.LookupMethod;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LookupMethodBeanConverter extends SpringBeanResolveConverter {

  @Nullable
  public List<PsiClassType> getRequiredClasses(final ConvertContext context) {
    final LookupMethod lookupMethod = (LookupMethod)context.getInvocationElement().getParent();
    assert lookupMethod != null;
    final PsiMethod psiMethod = lookupMethod.getName().getValue();
    if (psiMethod != null) {
      final PsiType returnType = psiMethod.getReturnType();
      if (returnType instanceof PsiClassType) {
        return  Collections.singletonList((PsiClassType)returnType);
      }
    }
    return null;
  }
}