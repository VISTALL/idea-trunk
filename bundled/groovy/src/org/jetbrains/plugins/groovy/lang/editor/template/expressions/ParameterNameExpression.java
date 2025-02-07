/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.lang.editor.template.expressions;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;

/**
 * @author ven
 */
public class ParameterNameExpression extends Expression {
  public ParameterNameExpression() {
  }

  public Result calculateResult(ExpressionContext context) {
    PsiDocumentManager.getInstance(context.getProject()).commitDocument(context.getEditor().getDocument());
    SuggestedNameInfo info = getNameInfo(context);
    if (info == null) return new TextResult("p");
    String[] names = info.names;
    if (names.length > 0) {
      return new TextResult(names[0]);
    }
    return null;
  }

  @Nullable
  private SuggestedNameInfo getNameInfo(ExpressionContext context) {
    Project project = context.getProject();
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(context.getEditor().getDocument());
    assert file != null;
    PsiElement elementAt = file.findElementAt(context.getStartOffset());
    GrParameter parameter = PsiTreeUtil.getParentOfType(elementAt, GrParameter.class);
    if (parameter == null) return null;
    JavaCodeStyleManager manager = JavaCodeStyleManager.getInstance(project);
    return manager.suggestVariableName(VariableKind.PARAMETER, null, null, parameter.getTypeGroovy());
  }

  public Result calculateQuickResult(ExpressionContext context) {
    return calculateResult(context);
  }

  public LookupElement[] calculateLookupItems(ExpressionContext context) {
    SuggestedNameInfo info = getNameInfo(context);
    if (info == null) return null;
    LookupElement[] result = new LookupElement[info.names.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = LookupElementBuilder.create(info.names[i]);
    }
    return result;
  }
}