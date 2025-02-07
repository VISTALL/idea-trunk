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
package org.jetbrains.plugins.groovy.annotator.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.codeInspection.GroovyInspectionBundle;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

/**
 * User: Dmitry.Krasilschikov
 * Date: 13.11.2007
 */
public class SecondUnsafeCallQuickFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.annotator.inspections.SecondUnsafeCallQuickFix");

  @NotNull
  public String getName() {
    return GroovyInspectionBundle.message("second.unsafe.call");
  }

  @NotNull
  public String getFamilyName() {
    //return GroovyInspectionBundle.message("second.unsafe.call");
    return "Probable bugs (Groovy)";
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiElement element = descriptor.getPsiElement();
    if (!(element instanceof GrReferenceExpression)) return;

    final PsiElement newDot = GroovyPsiElementFactory.getInstance(project).createDotToken(GroovyElementTypes.mOPTIONAL_DOT.toString());
    ((GrReferenceExpression) element).replaceDotToken(newDot);
  }
}
