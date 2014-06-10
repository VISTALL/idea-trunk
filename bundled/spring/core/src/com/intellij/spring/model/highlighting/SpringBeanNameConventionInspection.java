/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiNameHelper;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.values.converters.FieldRetrievingFactoryBeanConverter;
import com.intellij.spring.model.xml.beans.Alias;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringBeanNameConventionInspection extends SpringBeanInspectionBase {

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("model.inspection.display.bean.name.convention");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringBeanNameConventionInspection";
  }

  protected void checkBean(SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {
    final String beanId = springBean.getId().getStringValue();

    if (acceptBean(springBean, beanId)) {
      checkName(springBean.getId(), beanId, holder);
    }
  }

  private static boolean acceptBean(final SpringBean springBean, final String beanId) {
    return !StringUtil.isEmpty(beanId) && (!FieldRetrievingFactoryBeanConverter.isFieldRetrivingFactoryBean(springBean) ||
                                           !FieldRetrievingFactoryBeanConverter.isResolved(springBean.getManager().getProject(), beanId));

  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  private static void checkAlias(final Alias alias, final DomElementAnnotationHolder holder) {
    final String aliasName = alias.getAlias().getStringValue();

    checkName(alias.getAlias(), aliasName, holder);
  }

  private static void checkName(final DomElement domElement, @NotNull final String name, final DomElementAnnotationHolder holder) {
    PsiNameHelper psiNameHelper = JavaPsiFacade.getInstance(domElement.getManager().getProject()).getNameHelper();
    final boolean identifier = psiNameHelper.isIdentifier(name);
    if (!identifier) {
      boolean keyword = psiNameHelper.isKeyword(name); // IDEADEV-15506
      if(!keyword) {
        holder.createProblem(domElement, SpringBundle.message("model.inspection.invalid.identifier.message", name));
      }
    }

    if (Character.isUpperCase(name.charAt(0))) {
      holder.createProblem(domElement, SpringBundle.message("model.inspection.invalid.lowercase.name.message", name));
    }
  }
}