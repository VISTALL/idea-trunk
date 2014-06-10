/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.xml.XmlElement;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.model.jam.SpringJamModel;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.AddDomElementQuickFix;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class SpringBeanInstantiationInspection extends SpringBeanInspectionBase {

  protected void checkBean(final SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {
    final PsiClass psiClass = springBean.getClazz().getValue();
    if (psiClass != null && !springBean.isAbstract()) {
      if (psiClass.isInterface()) {
        return;
      }
      final boolean factory = DomUtil.hasXml(springBean.getFactoryMethod());
      final boolean lookup = springBean.getLookupMethods().size() > 0;
      if ((psiClass.hasModifierProperty(PsiModifier.ABSTRACT) && !factory && !lookup && !isJavaConfiBean(springBean))) {
        holder.createProblem(springBean.getClazz(),
                             HighlightSeverity.WARNING,
                             SpringBundle.message("abstract.class.not.allowed"),
                             new MarkAbstractFix(springBean.getAbstract()));
      }
    }
  }

  private static boolean isJavaConfiBean(final SpringBean springBean) {
    final XmlElement xmlElement = springBean.getXmlElement();
    final PsiClass beanClass = springBean.getBeanClass();

    if(xmlElement != null && beanClass != null) {
      final Module module = ModuleUtil.findModuleForPsiElement(xmlElement);
      if(module != null) {
        for (SpringJavaConfiguration javaConfiguration : SpringJamModel.getModel(module).getConfigurations()) {
          if(beanClass.equals(javaConfiguration.getPsiClass())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.bean.instantiation.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringBeanInstantiationInspection";
  }

  private static class MarkAbstractFix extends AddDomElementQuickFix<GenericAttributeValue<Boolean>> {

    public MarkAbstractFix(final GenericAttributeValue<Boolean> value) {
      super(value);
    }

    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      if (CodeInsightUtilBase.preparePsiElementForWrite(descriptor.getPsiElement())) {
        myElement.setValue(Boolean.TRUE);
      }
    }

    @NotNull
    public String getName() {
      return SpringBundle.message("mark.bean.as.abstract");
    }
  }
}
