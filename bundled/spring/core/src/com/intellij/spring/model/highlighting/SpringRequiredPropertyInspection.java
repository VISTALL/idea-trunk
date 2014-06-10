/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.spring.model.actions.generate.SpringPropertiesGenerateProvider;
import com.intellij.spring.model.actions.generate.SpringTemplateBuilder;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringRequiredPropertyInspection extends SpringBeanInspectionBase {

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.required.property.inspection");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringRequiredPropertyInspection";
  }


  protected void checkBean(final SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {
    if (springBean.isAbstract()) {
      return;
    }
    final PsiClass psiClass = springBean.getBeanClass();
    if (psiClass != null) {
      final Map<String,PsiMethod> properties = PropertyUtil.getAllProperties(psiClass, true, false);
      final List<SpringPropertyDefinition> list = springBean.getAllProperties();
      final List<String> missing = new ArrayList<String>();
      final List<PsiMethod> missingMethods = new ArrayList<PsiMethod>();
      for (Map.Entry<String, PsiMethod> entry : properties.entrySet()) {
        if (AnnotationUtil.findAnnotation(entry.getValue(), SpringAnnotationsConstants.REQUIRED_ANNOTATION) != null) {
          if (!isDefined(list, entry.getKey())) {
            missing.add(entry.getKey());
            missingMethods.add(entry.getValue());
          }
        }
      }
      if (!missing.isEmpty()) {
        final DomElement element = DomUtil.hasXml(springBean.getClazz()) ? springBean.getClazz() : springBean;
        holder.createProblem(element, HighlightSeverity.ERROR, SpringBundle.message("required.properties.missed", StringUtil.join(missing, ",")), new LocalQuickFix() {
          @NotNull
          public String getName() {
            return SpringBundle.message("create.missing.properties");
          }

          @NotNull
          public String getFamilyName() {
            return getName();
          }

          public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            if (!ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(descriptor.getPsiElement().getContainingFile().getVirtualFile()).hasReadonlyFiles()) {
              final Editor editor = SpringTemplateBuilder.getEditor(descriptor);
              SpringPropertiesGenerateProvider.doGenerate(editor, springBean, project,
                                                          missingMethods.toArray(new PsiMethod[missingMethods.size()]));
            }
          }
        });
      }
    }
  }

  private static boolean isDefined(final List<SpringPropertyDefinition> list, final String property) {

    for (SpringPropertyDefinition definition : list) {
      final String name = definition.getPropertyName();
      if (name != null && name.equals(property)) {
        return true;
      }
    }
    return false;
  }

}