/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.surroundWith.surrounders.surroundersImpl.expressions;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrParenthesizedExpression;

/**
 * User: Dmitry.Krasilschikov
 * Date: 22.05.2007
 */
public class GroovyWithParenthesisExprSurrounder extends GroovyExpressionSurrounder {
  protected TextRange surroundExpression(GrExpression expression) {
    GrParenthesizedExpression result = (GrParenthesizedExpression) GroovyPsiElementFactory.getInstance(expression.getProject()).createExpressionFromText("(a)");
    replaceToOldExpression(result.getOperand(), expression);
    result = (GrParenthesizedExpression) expression.replaceWithExpression(result, true);
    return new TextRange(result.getTextRange().getEndOffset(), result.getTextRange().getEndOffset());
  }

  public String getTemplateDescription() {
    return "(...)";
  }
}
