/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public class AutowiredDependenciesInspection extends SpringBeanInspectionBase {

  public void checkFileElement(final DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder) {
    super.checkFileElement(domFileElement, holder);

    final Beans beans = domFileElement.getRootElement();
    final DefaultAutowire defaultAutowire = beans.getDefaultAutowire().getValue();

    if (defaultAutowire != null && !DefaultAutowire.NO.equals(defaultAutowire)) {
      holder.createProblem(beans.getDefaultAutowire(), HighlightSeverity.WARNING, SpringBundle.message("spring.bean.autowire.escape"),
                           createDefaultAutowireEscapeQuickFixes(beans.<Beans>createStableCopy(), defaultAutowire));
    }
  }

  private static LocalQuickFix createDefaultAutowireEscapeQuickFixes(final Beans beans,
                                                                     final DefaultAutowire defaultAutowire) {
    return new LocalQuickFix() {
      @NotNull
      public String getName() {
        return SpringBundle.message("spring.bean.autowire.escape");
      }

      @NotNull
      public String getFamilyName() {
        return SpringBundle.message("model.bean.quickfix.family");
      }

      public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        if (!beans.isValid()) return;
        
        new WriteCommandAction(project, DomUtil.getFile(beans)) {
          protected void run(final Result result) throws Throwable {
            for (SpringBean bean : beans.getBeans()) {
              if (isAutowireCandidate(bean)) {
                final Autowire autowire = bean.getAutowire().getValue();

                if (autowire == null || autowire.getValue().equals(defaultAutowire.getValue()) || autowire.equals(Autowire.DEFAULT)) {
                  escapeAutowire(defaultAutowire.getValue(), bean);
                }
              }
            }
            beans.getDefaultAutowire().undefine();
          }
        }.execute();
      }
    };
  }

  protected void checkBean(SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel model) {
    if (isAutowireCandidate(springBean)) {
      if (model != null) {
        addAutowireEscapeWarning(springBean, holder);
      }
    }
  }

  private static boolean isAutowireCandidate(final SpringBean springBean) {
    final Boolean autoWireCandidate = springBean.getAutowireCandidate().getValue();

    return autoWireCandidate == null || autoWireCandidate.booleanValue();

  }

  private static void addAutowireEscapeWarning(final SpringBean springBean, final DomElementAnnotationHolder holder) {
    final Autowire autowire = springBean.getAutowire().getValue();
    if (autowire != null && !Autowire.NO.equals(autowire)) {
      holder.createProblem(springBean.getAutowire(), HighlightSeverity.WARNING, SpringBundle.message("spring.bean.use.autowire"),
                           createEscapeAutowireQuickFixes(springBean.<SpringBean>createStableCopy(), autowire));
    }
  }

  private static LocalQuickFix createEscapeAutowireQuickFixes(final SpringBean springBean, @NotNull final Autowire autowire) {
    return new LocalQuickFix() {
      @NotNull
      public String getName() {
        return SpringBundle.message("spring.bean.autowire.escape");
      }

      @NotNull
      public String getFamilyName() {
        return SpringBundle.message("model.bean.quickfix.family");
      }

      public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        if (!springBean.isValid()) return;

        final SpringModel springModel = SpringUtils.getSpringModel(springBean);

        new WriteCommandAction(springBean.getManager().getProject(), DomUtil.getFile(springBean)) {
          protected void run(final Result result) throws Throwable {
            escapeAutowire(autowire.getValue(), springBean);
          }
        }.execute();
      }
    };
  }

  private static void escapeAutowire(@NotNull final String autowire, final SpringBean springBean) {
    final SpringModel springModel = SpringUtils.getSpringModel(springBean);

    if (autowire.equals(Autowire.BY_TYPE.getValue())) {
      escapeByTypeAutowire(springBean, springModel);
    }
    else if (autowire.equals(Autowire.BY_NAME.getValue())) {
      escapeByNameAutowire(springBean);
    }
    else if (autowire.equals(Autowire.CONSTRUCTOR.getValue())) {
      escapeConstructorAutowire(springBean, springModel);
    }
    else if (autowire.equals(Autowire.AUTODETECT.getValue())) {
      if (SpringConstructorArgResolveUtil.hasEmptyConstructor(springBean) &&
          !SpringConstructorArgResolveUtil.isInstantiatedByFactory(springBean)) {
        escapeByTypeAutowire(springBean, springModel);
      }
      else {
        escapeConstructorAutowire(springBean, springModel);
      }
    }
  }

  private static void escapeConstructorAutowire(final SpringBean springBean, final SpringModel springModel) {
    final Map<PsiType, Collection<SpringBaseBeanPointer>> map = SpringAutowireUtil.getConstructorAutowiredProperties(springBean, springModel);
    for (PsiType psiType : map.keySet()) {
      final ConstructorArg arg = springBean.addConstructorArg();
      arg.getType().setStringValue(psiType.getCanonicalText());
      arg.getRefAttr().setStringValue(chooseReferencedBeanName(map.get(psiType)));
    }

    springBean.getAutowire().undefine();

  }

  private static void escapeByNameAutowire(final SpringBean springBean) {
    final Map<PsiMethod, SpringBaseBeanPointer> autowiredProperties = SpringAutowireUtil.getByNameAutowiredProperties(springBean);
    for (PsiMethod psiMethod : autowiredProperties.keySet()) {
      final SpringProperty springProperty = springBean.addProperty();
      final SpringBaseBeanPointer autowiredBean = autowiredProperties.get(psiMethod);
      final String refBeanName = autowiredBean != null && autowiredBean.getName() != null ? autowiredBean.getName() : "";

      springProperty.getName().setStringValue(PropertyUtil.getPropertyNameBySetter(psiMethod));
      springProperty.getRefAttr().setStringValue(refBeanName);
    }

    springBean.getAutowire().undefine();
  }

  private static void escapeByTypeAutowire(final SpringBean springBean, final SpringModel springModel) {
    final Map<PsiMethod, Collection<SpringBaseBeanPointer>> autowiredProperties = SpringAutowireUtil.getByTypeAutowiredProperties(springBean, springModel);

    for (PsiMethod psiMethod : autowiredProperties.keySet()) {
      final SpringProperty springProperty = springBean.addProperty();
      springProperty.getName().setStringValue(PropertyUtil.getPropertyNameBySetter(psiMethod));
      springProperty.getRefAttr().setStringValue(chooseReferencedBeanName(autowiredProperties.get(psiMethod)));
    }

    springBean.getAutowire().undefine();
  }

  @NotNull
  private static String chooseReferencedBeanName(Collection<SpringBaseBeanPointer> autowiredBeans) {
    if (autowiredBeans != null) {
      for (SpringBaseBeanPointer autowiredBean : autowiredBeans) {
        final String beanName = SpringUtils.getReferencedName(autowiredBean.getSpringBean());
        if (beanName != null && beanName.trim().length() > 0) return beanName;
      }
    }
    return "";
  }


  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.bean.autowire.escape.inspection.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "AutowiredDependenciesInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}
