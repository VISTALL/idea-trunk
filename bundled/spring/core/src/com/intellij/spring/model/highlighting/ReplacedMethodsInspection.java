/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeInsight.daemon.impl.quickfix.ExtendsListFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.converters.ReplacedMethodBeanConverter;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.ReplacedMethod;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ReplacedMethodsInspection extends SpringBeanInspectionBase{

  protected void checkBean(SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {
    for (ReplacedMethod replacedMethod : springBean.getReplacedMethods()) {
      checkReplacedMethod(springBean, replacedMethod, holder);
    }
  }

  private static void checkReplacedMethod(final SpringBean springBean,
                                          final ReplacedMethod replacedMethod,
                                          final DomElementAnnotationHolder holder) {

    final SpringBeanPointer beanPointer = replacedMethod.getReplacer().getValue();
    if (beanPointer != null) {
      final PsiClass beanClass = beanPointer.getBeanClass();

      if (beanClass != null) {
        final Project project = springBean.getManager().getProject();
        final PsiClass replacerClass = JavaPsiFacade.getInstance(project)
          .findClass(ReplacedMethodBeanConverter.METHOD_REPLACER_CLASS, GlobalSearchScope.allScope(project));
        if (replacerClass != null && !beanClass.isInheritor(replacerClass, true)) {
          holder.createProblem(replacedMethod.getReplacer(),
                               HighlightSeverity.ERROR, 
                               SpringBundle.message("spring.bean.replaced.method.must.implement.MethodReplacer"),
                               new ExtendsListFix(beanClass, replacerClass, true));
        }
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.bean.replace.methods.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringReplacedMethodsInspection";
  }
}
