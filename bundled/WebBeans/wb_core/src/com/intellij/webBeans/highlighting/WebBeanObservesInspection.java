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
import com.intellij.psi.*;
import com.intellij.util.containers.HashSet;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import com.intellij.webBeans.utils.SimpleWebBeanValidationUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeanObservesInspection extends BaseWebBeanInspection {
  @Override
  protected void checkClass(PsiClass psiClass, ProblemsHolder holder, @NotNull Module module) {

    Map<PsiMethod, Set<PsiParameter>> observesMethods = collectObserverMethods(psiClass);
    for (PsiMethod method : observesMethods.keySet()) {
      Set<PsiParameter> observesParameters = observesMethods.get(method);
      checkObservesMethod(holder, method, observesParameters);
      checkObservesParameter(holder, observesParameters);
    }
  }

  private static void checkObservesMethod(ProblemsHolder holder, PsiMethod method, Set<PsiParameter> observesParameters) {
    checkMultipleObservesParameters(holder, method, observesParameters);
    checkWrongAnnotatedParameter(method, holder, WebBeansAnnoConstants.DISPOSES_ANNOTATION);
    checkWrongAnnotations(holder, method, WebBeansAnnoConstants.PRODUCES_ANNOTATION);
    checkWrongAnnotations(holder, method, WebBeansAnnoConstants.INITIALIZER_ANNOTATION);
  }

  private static void checkWrongAnnotations(ProblemsHolder holder, PsiMethod method, String anno) {
    if (AnnotationUtil.isAnnotated(method, anno, false)) {
      holder.registerProblem(method.getNameIdentifier(),
                             WebBeansInspectionBundle.message("WebBeanObservesInspection.wrong.observe.method.annotation", anno));

    }
  }

  private static void checkMultipleObservesParameters(ProblemsHolder holder, PsiMethod method, Set<PsiParameter> observesParameters) {
    if (observesParameters.size() > 1) {
      holder.registerProblem(method.getNameIdentifier(),
                             WebBeansInspectionBundle.message("WebBeanObservesInspection.multiple.observes.parameters.not.allowed"));
    }
  }

  private static void checkObservesParameter(ProblemsHolder holder, Set<PsiParameter> observesParameters) {
    for (PsiParameter parameter : observesParameters) {
      checkParameterizedType(holder, parameter);
    }
  }

  private static void checkWrongAnnotatedParameter(PsiMethod method, ProblemsHolder holder, String anno) {
    for (PsiParameter parameter : method.getParameterList().getParameters()) {
      if (AnnotationUtil.isAnnotated(parameter, anno, true)) {
        holder.registerProblem(method.getNameIdentifier(),
                               WebBeansInspectionBundle.message("WebBeanObservesInspection.observer.method.with.wrong.parameters", anno));
        break;
      }
    }
  }

  private static void checkParameterizedType(ProblemsHolder holder, PsiParameter parameter) {
    PsiType type = parameter.getType();
    if (type instanceof PsiClassType && SimpleWebBeanValidationUtils.isParameterizedType(((PsiClassType)type).resolve())) {
      holder.registerProblem(parameter.getNameIdentifier(),
                             WebBeansInspectionBundle.message("WebBeanObservesInspection.cannot.observes.parameterized.types"));
    }
  }

  @NotNull
  private static Map<PsiMethod, Set<PsiParameter>> collectObserverMethods(PsiClass psiClass) {
    Map<PsiMethod, Set<PsiParameter>> observesMethods = new HashMap<PsiMethod, Set<PsiParameter>>();
    for (PsiMethod psiMethod : psiClass.getMethods()) {
      for (PsiParameter psiParameter : psiMethod.getParameterList().getParameters()) {
        if (AnnotationUtil.isAnnotated(psiParameter, WebBeansAnnoConstants.OBSERVES_ANNOTATION, false)) {
          if (observesMethods.get(psiMethod) == null) observesMethods.put(psiMethod, new HashSet<PsiParameter>());

          observesMethods.get(psiMethod).add(psiParameter);
        }
      }
    }
    return observesMethods;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return WebBeansInspectionBundle.message("inspection.name.observer.method.errors");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "WebBeanObservesInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}