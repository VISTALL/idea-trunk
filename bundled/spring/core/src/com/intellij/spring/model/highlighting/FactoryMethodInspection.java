/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.psi.PsiMethod;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.converters.SpringBeanFactoryMethodConverter;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class FactoryMethodInspection extends SpringBeanInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("model.inspection.bean.factory.method");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringFactoryMethodInspection";
  }

  protected void checkBean(SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel model) {
    final PsiMethod factoryMethod = springBean.getFactoryMethod().getValue();
    if (factoryMethod != null) {
      if (!SpringBeanFactoryMethodConverter.isPublic(factoryMethod)) {
        holder.createProblem(springBean.getFactoryMethod(), SpringBundle.message("method.must.be.public", factoryMethod.getName()));
      }
      final boolean isStatic = SpringBeanFactoryMethodConverter.isStatic(factoryMethod);
      final SpringBeanPointer factoryBean = springBean.getFactoryBean().getValue();
      if (!isStatic && factoryBean == null) {
        holder.createProblem(springBean.getFactoryMethod(), SpringBundle.message("method.must.be.static", factoryMethod.getName()));
      } else if (isStatic && factoryBean != null) {
        holder.createProblem(springBean.getFactoryMethod(), SpringBundle.message("method.must.not.be.static", factoryMethod.getName()));
      }
      if (!SpringBeanFactoryMethodConverter.isProperReturnType(factoryMethod)) {
        holder.createProblem(springBean.getFactoryMethod(), SpringBundle.message("method.cannot.instantiate.bean", factoryMethod.getName()));
      }
    }
  }
}
