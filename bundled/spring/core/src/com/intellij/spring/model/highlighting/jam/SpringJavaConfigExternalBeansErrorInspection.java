package com.intellij.spring.model.highlighting.jam;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.jam.utils.SpringJamUtils;
import com.intellij.spring.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.model.jam.javaConfig.SpringJavaExternalBean;
import com.intellij.spring.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpringJavaConfigExternalBeansErrorInspection extends SpringJavaConfigInspectionBase {
  protected void checkJavaConfiguration(final SpringJavaConfiguration javaConfiguration, final Module module, final ProblemsHolder holder) {
    if (javaConfiguration instanceof JavaConfigConfiguration) {
      checkExternalBean((JavaConfigConfiguration)javaConfiguration, holder);
    }
  }

  private static void checkExternalBean(final JavaConfigConfiguration configuration, final ProblemsHolder holder) {
    final List<? extends SpringJavaExternalBean> list = configuration.getExternalBeans();
    if (list.size() > 0) {
      for (SpringJavaExternalBean externalBean : list) {
        checkExternalBeanExist(externalBean, holder);
        checkExternalBeanType(externalBean, holder);
      }
    }
  }

  private static void checkExternalBeanType(final SpringJavaExternalBean externalBean, final ProblemsHolder holder) {
    final List<SpringBaseBeanPointer> beans = SpringJamUtils.findExternalBeans(externalBean.getPsiElement());
    if (beans.size() == 1) {
      SpringBaseBeanPointer springBean = beans.get(0);
      final PsiMethod psiMethod = externalBean.getPsiElement();
      if (psiMethod != null) {
        final PsiType returnType = psiMethod.getReturnType();

        if (returnType instanceof PsiClassType) {

          final PsiClass beanClass = springBean.getBeanClass();
          if (beanClass != null) {
            final PsiClassType beanClassType = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory().createType(beanClass);
            final PsiClassType externalBeanClassType = (PsiClassType)returnType;
            if (!externalBeanClassType.isAssignableFrom(beanClassType)) {
              holder.registerProblem(psiMethod.getReturnTypeElement(),
                                     SpringBundle.message("spring.java.configuration.inspection.incorrect.return.type.of.external.bean",
                                                          beanClassType.getClassName()));
            }
          }
        }
        else {
          holder.registerProblem(psiMethod.getReturnTypeElement(),
                                 SpringBundle.message("spring.java.configuration.inspection.class.type.expected"));
        }
      }
    }
  }

  private static void checkExternalBeanExist(final SpringJavaExternalBean externalBean, final ProblemsHolder holder) {
    final PsiMethod member = externalBean.getPsiElement();
    if (member != null) {
      if (SpringJamUtils.findExternalBeans(externalBean.getPsiElement()).size() == 0) {
        final PsiIdentifier psiIdentifier = member.getNameIdentifier();
        holder.registerProblem(psiIdentifier, SpringBundle.message("spring.java.configuration.inspection.cannot.find.external.bean"));
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.java.configuration.inspection.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringJavaConfigExternalBeansErrorInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}
