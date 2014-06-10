/*
 * Copyright 2007-2008 Dave Griffith, Bas Leijdekkers
 *
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
package org.jetbrains.plugins.groovy.codeInspection.control;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspection;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspectionVisitor;
import org.jetbrains.plugins.groovy.codeInspection.GroovyFix;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrCondition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrBlockStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrIfStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrStatementOwner;

public class GroovyConstantIfStatementInspection extends BaseInspection {

  @NotNull
  public String getGroupDisplayName() {
    return CONTROL_FLOW;
  }

  @NotNull
  public String getDisplayName() {
    return "Constant if statement";
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  protected String buildErrorString(Object... args) {
    return "#ref statement can be simplified #loc";
  }

  public BaseInspectionVisitor buildVisitor() {
    return new ConstantIfStatementVisitor();
  }

  public GroovyFix buildFix(PsiElement location) {
    return new ConstantIfStatementFix();
  }

  private static class ConstantIfStatementFix extends GroovyFix {

    @NotNull
    public String getName() {
      return "Simplify";
    }

    public void doFix(Project project, ProblemDescriptor descriptor)
        throws IncorrectOperationException {
      final PsiElement ifKeyword = descriptor.getPsiElement();
      final GrIfStatement ifStatement = (GrIfStatement) ifKeyword.getParent();
      assert ifStatement != null;
      final GrStatement thenBranch = ifStatement.getThenBranch();
      final GrStatement elseBranch = ifStatement.getElseBranch();
      final GrExpression condition = (GrExpression) ifStatement.getCondition();
      // todo still needs some handling for conflicting declarations
      if (isFalse(condition)) {
        if (elseBranch != null) {
          replaceStatement(ifStatement, elseBranch);
        } else {
          ifStatement.delete();
        }
      } else {
        replaceStatement(ifStatement, thenBranch);
      }
    }
  }

  private static class ConstantIfStatementVisitor
      extends BaseInspectionVisitor {

    public void visitIfStatement(GrIfStatement statement) {
      super.visitIfStatement(statement);
      final GrCondition condition = statement.getCondition();
      if (!(condition instanceof GrExpression)) {
        return;
      }
      final GrStatement thenBranch = statement.getThenBranch();
      if (thenBranch == null) {
        return;
      }
      final GrExpression conditionExpression = (GrExpression) condition;
      if (isTrue(conditionExpression) || isFalse(conditionExpression)) {
        registerStatementError(statement);
      }
    }
  }

  private static boolean isFalse(GrExpression expression) {
    @NonNls final String text = expression.getText();
    return "false".equals(text);
  }

  private static boolean isTrue(GrExpression expression) {
    @NonNls final String text = expression.getText();
    return "true".equals(text);
  }
}