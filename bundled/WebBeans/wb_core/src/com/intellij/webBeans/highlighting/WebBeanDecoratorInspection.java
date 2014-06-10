/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.webBeans.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeanDecoratorInspection extends BaseWebBeanInspection {

  @Override
  protected void checkClass(PsiClass psiClass, ProblemsHolder holder, @NotNull Module module) {
    final PsiAnnotation decoratorAnno = AnnotationUtil.findAnnotation(psiClass, WebBeansAnnoConstants.DECORATOR_ANNOTATION);
    if (decoratorAnno != null) {
      checkDecoratesFields(psiClass, holder, decoratorAnno);
      checkInconsistentAnnotations(psiClass, holder, decoratorAnno, WebBeansAnnoConstants.INTERCEPTOR_ANNOTATION);
    }
  }

  private static void checkInconsistentAnnotations(PsiClass psiClass,
                                                   ProblemsHolder holder,
                                                   PsiAnnotation decoratorAnno,
                                                   String annotation) {
    final PsiAnnotation anno = AnnotationUtil.findAnnotation(psiClass, annotation);
    if (anno != null) {
      holder.registerProblem(decoratorAnno,
                             WebBeansInspectionBundle.message("WebBeanDecoratorInspection.annotaion.not.allowed.for.decorator",
                                                              anno.getQualifiedName()));


    }
  }

  private static void checkDecoratesFields(PsiClass psiClass, ProblemsHolder holder, PsiAnnotation decoratorAnno) {
    Set<Pair<PsiAnnotation, PsiField>> decorates = getDecorates(psiClass);
    if (decorates.size() == 0) {
      holder.registerProblem(decoratorAnno, WebBeansInspectionBundle.message("WebBeanDecoratorInspection.cannot.find.decorates"));
    }
    if (decorates.size() > 1) {
      holder.registerProblem(decoratorAnno, WebBeansInspectionBundle.message("WebBeanDecoratorInspection.too.many.decorates.in.decorator"));
    }
    for (Pair<PsiAnnotation, PsiField> decorate : decorates) {
      checkIsInterfaceAndImplentsAllDecoratorInterfaces(holder, decorate, psiClass);
    }
  }

  private static void checkIsInterfaceAndImplentsAllDecoratorInterfaces(ProblemsHolder holder,
                                                                        Pair<PsiAnnotation, PsiField> decorate,
                                                                        PsiClass psiClass) {
    final PsiField field = decorate.second;
    PsiType type = field.getType();
    if (!isInterface(type)) {
      holder.registerProblem(field.getNameIdentifier(),
                             WebBeansInspectionBundle.message("WebBeanDecoratorInspection.decorates.must.be.interface"));
    }
    else {
      final PsiClass decoratesClass = ((PsiClassType)type).resolve();

      final Set<String> unsupportedInterfaces = new HashSet<String>();
      InheritanceUtil.processSupers(psiClass, false, new Processor<PsiClass>() {
        public boolean process(PsiClass psiClass) {
          if (psiClass.isInterface() && !InheritanceUtil.isInheritorOrSelf(decoratesClass, psiClass, true)) {
            unsupportedInterfaces.add(psiClass.getQualifiedName());
          }
          return true;
        }
      });

      if (unsupportedInterfaces.size() > 0) {
        String[] strings = unsupportedInterfaces.toArray(new String[unsupportedInterfaces.size()]);

        holder.registerProblem(field.getNameIdentifier(), WebBeansInspectionBundle.message(
          "WebBeanDecoratorInspection.decorates.type.must.implements.all.decorator.interfaces", StringUtil.join(strings, ",")));
      }
    }
  }

  private static boolean isInterface(final PsiType type) {
    if (type instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null) {
        return psiClass.isInterface();
      }
    }
    return false;
  }

  @NotNull
  private static Set<Pair<PsiAnnotation, PsiField>> getDecorates(@NotNull PsiClass psiClass) {
    Set<Pair<PsiAnnotation, PsiField>> decorates = new HashSet<Pair<PsiAnnotation, PsiField>>();
    for (PsiField psiField : psiClass.getFields()) {
      PsiAnnotation annotation = AnnotationUtil.findAnnotation(psiField, WebBeansAnnoConstants.DECORATES_ANNOTATION);
      if (annotation != null) {
        decorates.add(new Pair<PsiAnnotation, PsiField>(annotation, psiField));
      }
    }
    return decorates;
  }


  @Nls
  @NotNull
  public String getDisplayName() {
    return WebBeansInspectionBundle.message("inspection.name.decorator.errors");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "WebBeanDecoratorInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}