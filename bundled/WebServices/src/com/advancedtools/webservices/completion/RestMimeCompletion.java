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

import static com.advancedtools.webservices.rest.RestAnnotations.*;
import com.advancedtools.webservices.utils.RestUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiExpressionPattern;
import com.intellij.patterns.PsiJavaElementPattern;
import static com.intellij.patterns.PsiJavaPatterns.*;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @by Konstantin Bulenkov
 */
public class RestMimeCompletion extends CompletionContributor {
  private static final PsiJavaElementPattern.Capture<PsiLiteralExpression> MIME_ANNO_VALUE = literalExpression()
      .insideAnnotationParam(string().oneOf(PRODUCE_MIME, PRODUCES,CONSUME_MIME, CONSUMES), "value");
  private static final PsiExpressionPattern.Capture<PsiExpression> INSIDE_ANNOTATION
    = psiExpression().insideAnnotationParam(string().oneOf(PRODUCE_MIME, PRODUCES,CONSUME_MIME, CONSUMES), "value");

  public RestMimeCompletion() {
    extend(CompletionType.BASIC, psiElement().withParent(MIME_ANNO_VALUE), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        PsiLiteralExpression literal = (PsiLiteralExpression)parameters.getPosition().getContext();
          Object obj = JavaConstantExpressionEvaluator.computeConstantExpression(literal, false);
          if (!(obj instanceof String)) return;
          final String value = obj.toString();
          Project project = parameters.getPosition().getProject();
          String[] mimes = RestUtils.getAllMimes(project, RestUtils.PREDEFINED_MIME_TYPES);
          boolean secondPartOnly = value.contains("/");
          String prefix = (secondPartOnly) ? value.substring(0, value.indexOf('/')) : value;
          for (String mime : mimes) {
            if (! mime.equals(result.getPrefixMatcher().getPrefix()))
              if (secondPartOnly) {
                if (mime.contains("/") && mime.startsWith(prefix)) {
                  mime = mime.substring(mime.indexOf('/') + 1);
                  result.addElement(LookupElementBuilder.create(mime));
                }
              } else {
                result.addElement(LookupElementBuilder.create(mime));
              }
          }
          if (mimes.length != 0) {
            result.stopHere();
          }
      }
    });

    extend(CompletionType.BASIC, psiElement().withParent(INSIDE_ANNOTATION), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        String[] mimes = RestUtils.getAllMimes(parameters.getPosition().getProject(), RestUtils.PREDEFINED_MIME_TYPES);
          for (String mime : mimes) {
            result.addElement(LookupElementBuilder.create("\"" + mime + "\"").setPresentableText(mime));
          }
          if (mimes.length != 0) {
            result.stopHere();
          }
      }
    });
  }

}
