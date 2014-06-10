/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.LookupMethod;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LookupMethodInspection extends SpringBeanInspectionBase {


  private static void checkLookupMethodReturnType(final LookupMethod lookupMethod,
                                          final PsiMethod method,
                                          final DomElementAnnotationHolder holder) {

    final PsiType returnType = method.getReturnType();
    if (returnType == null) {
      holder.createProblem(lookupMethod.getName(),
                           SpringBundle.message("spring.bean.lookup.method.constructor.not.allowed"));
    } else if (!(returnType instanceof PsiClassType) || ((PsiClassType)returnType).resolve() == null) {
      holder.createProblem(lookupMethod.getName(),
                           SpringBundle.message("spring.bean.lookup.method.incorrect.return.type"));
    } else {
      final SpringBeanPointer beanPointer = lookupMethod.getBean().getValue();
      if (beanPointer != null) {
        final PsiClass beanClass = beanPointer.getBeanClass();
        if (beanClass == null) {
          holder.createProblem(lookupMethod.getBean(),
                               SpringBundle.message("spring.bean.lookup.method.bean.has.no.class"));

        } else {
          final PsiClass returnClass = ((PsiClassType)returnType).resolve();
          assert returnClass != null;
          if (!SpringUtils.isEffectiveClassType(returnType, beanPointer.getSpringBean()) &&
              !InheritanceUtil.isInheritorOrSelf(beanClass, returnClass, true)) {
            String beanName = beanPointer.getName();
            if (StringUtil.isEmpty(beanName)) beanName = "unknown";
            final String message = SpringBundle.message("spring.bean.lookup.method.return.type.mismatch", beanName);
            holder.createProblem(lookupMethod.getName(), message);
            holder.createProblem(lookupMethod.getBean(), message);
          }
        }
      }
    }
  }

  private static void checkLookupMethodIdentifiers(final LookupMethod lookupMethod,
                                           final DomElementAnnotationHolder holder,
                                           final PsiMethod method) {

    if (!(method.hasModifierProperty(PsiModifier.PUBLIC) || method.hasModifierProperty(PsiModifier.PROTECTED))) {
      holder.createProblem(lookupMethod.getName(), SpringBundle.message("spring.bean.lookup.method.must.be.public.or.protected"));
    }
    if (method.hasModifierProperty(PsiModifier.STATIC)) {
      holder.createProblem(lookupMethod.getName(), SpringBundle.message("spring.bean.lookup.method.must.be.not.static"));
    }
    if (method.getParameterList().getParametersCount() > 0) {
      holder.createProblem(lookupMethod.getName(), SpringBundle.message("spring.bean.lookup.method.must.have.no.parameters"));
    }
  }

  protected void checkBean(SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {
    for (LookupMethod lookupMethod : springBean.getLookupMethods()) {
      final PsiMethod method = lookupMethod.getName().getValue();
      if (method != null) {
        checkLookupMethodIdentifiers(lookupMethod, holder, method);
        checkLookupMethodReturnType(lookupMethod, method, holder);
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.bean.lookup.method.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringBeanLookupMethodInspection";
  }
}
