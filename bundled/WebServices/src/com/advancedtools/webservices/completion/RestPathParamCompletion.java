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

package com.advancedtools.webservices.completion;

import com.advancedtools.webservices.rest.RestAnnotations;
import com.advancedtools.webservices.rest.RestUriTemplateParser;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.patterns.PsiExpressionPattern;
import com.intellij.patterns.PsiJavaPatterns;
import static com.intellij.patterns.PsiJavaPatterns.psiExpression;
import static com.intellij.patterns.PsiJavaPatterns.psiParameter;
import com.intellij.patterns.PsiParameterPattern;
import static com.intellij.patterns.StandardPatterns.string;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class RestPathParamCompletion extends CompletionContributor {
  private static final PsiExpressionPattern.Capture<PsiExpression> PATH_PARAM_FILTER =
    psiExpression().insideAnnotationParam(string().oneOf(RestAnnotations.PATH_PARAM), "value");
  private static final PsiParameterPattern ANNOTATED_WITH_PATH_PARAM =
    psiParameter().withAnnotation(RestAnnotations.PATH_PARAM);
  private static final PsiExpressionPattern.Capture<PsiExpression> INSIDE_ANNOTATION
    = psiExpression().annotationParam(RestAnnotations.PATH_PARAM, "value");

  public RestPathParamCompletion() {
    extend(CompletionType.BASIC, PsiJavaPatterns.psiElement().withParent(PATH_PARAM_FILTER), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final List<String> values = findPathAnnotationValue(parameters.getPosition().getContext());
        for (String value : values) {
          result.addElement(LookupElementBuilder.create(value));
        }
        if (!values.isEmpty()) {
          result.stopHere();
        }
      }
    });
    extend(CompletionType.BASIC, PsiJavaPatterns.psiElement().withParent(ANNOTATED_WITH_PATH_PARAM), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final PsiModifierList modifierList = ((PsiParameter)parameters.getPosition().getContext()).getModifierList();
        if (modifierList != null) {
          final PsiAnnotation annotation = modifierList.findAnnotation(RestAnnotations.PATH_PARAM);
          if (annotation != null) {
            final String value = AnnotationModelUtil.getStringValue(annotation, "value", null).getValue();
            if (value != null && value.length() > 0) {
              result.addElement(LookupElementBuilder.create(value));
              result.stopHere();
            }
          }
        }
      }
    });
    extend(CompletionType.BASIC, PsiJavaPatterns.psiElement().withParent(INSIDE_ANNOTATION), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final List<String> values = findPathAnnotationValue(parameters.getPosition().getContext());
        for (String value : values) {
          result.addElement(LookupElementBuilder.create("\"" + value + "\"").setPresentableText(value));
        }
        if (!values.isEmpty()) {
          result.stopHere();
        }

      }
    });
  }

  private static List<String> findPathAnnotationValue(PsiElement el) {
    List<String> variants = new ArrayList<String>();
    PsiAnnotation annotation = null;
    while (el != null) {
      if (el instanceof PsiMethod) {
        PsiMethod method = (PsiMethod)el;
        annotation = method.getModifierList().findAnnotation(RestAnnotations.PATH);
      }
      if (el instanceof PsiClass) {
        final PsiModifierList modifierList = ((PsiClass)el).getModifierList();
        if (modifierList != null) {
          annotation = modifierList.findAnnotation(RestAnnotations.PATH);
        }
      }
      if (annotation != null) {
        variants.addAll(findNames(annotation));
        annotation = null;
      }
      el = el.getParent();

      if (el instanceof PsiFile) break;
    }
    return variants;
  }

  private static List<String> findNames(PsiAnnotation annotation) {
    List<String> variants = new ArrayList<String>();
    if (annotation != null) {
      String path = AnnotationModelUtil.getStringValue(annotation, "value", null).getValue();
      try {
        variants.addAll(new RestUriTemplateParser(path).getNames());
      }
      catch (Exception e) {//
      }
    }
    return variants;
  }

}
