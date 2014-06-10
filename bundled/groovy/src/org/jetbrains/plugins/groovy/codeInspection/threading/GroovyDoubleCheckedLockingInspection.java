/*
 * Copyright 2007-2008 Dave Griffith
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
package org.jetbrains.plugins.groovy.codeInspection.threading;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspection;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspectionVisitor;
import org.jetbrains.plugins.groovy.codeInspection.utils.ControlFlowUtils;
import org.jetbrains.plugins.groovy.codeInspection.utils.EquivalenceChecker;
import org.jetbrains.plugins.groovy.codeInspection.utils.SideEffectChecker;
import org.jetbrains.plugins.groovy.codeInspection.utils.SingleCheckboxOptionsPanel;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrCondition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrIfStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrSynchronizedStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrCodeBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import javax.swing.*;

public class GroovyDoubleCheckedLockingInspection extends BaseInspection {

  /**
   * @noinspection PublicField,WeakerAccess
   */
  public boolean ignoreOnVolatileVariables = false;

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return THREADING_ISSUES;
  }

  @NotNull
  public String getDisplayName() {
    return "Double-checked locking";
  }

  @NotNull
  protected String buildErrorString(Object... infos) {
    return "Double-checked locking #loc";
  }

  @Nullable
  public JComponent createOptionsPanel() {
    return new SingleCheckboxOptionsPanel("Ignore double-checked locking on volatile fields", this,
        "ignoreOnVolatileVariables"
    );
  }

  public BaseInspectionVisitor buildVisitor() {
    return new DoubleCheckedLockingVisitor();
  }

  private class DoubleCheckedLockingVisitor
      extends BaseInspectionVisitor {

    public void visitIfStatement(@NotNull GrIfStatement statement) {
      super.visitIfStatement(statement);
      final GrCondition outerCondition = statement.getCondition();
      if (!(outerCondition instanceof GrExpression)) {
        return;
      }
      if (SideEffectChecker.mayHaveSideEffects((GrExpression) outerCondition)) {
        return;
      }
      GrStatement thenBranch = statement.getThenBranch();
      if (thenBranch == null) {
        return;
      }
      thenBranch = ControlFlowUtils.stripBraces(thenBranch);
      if (!(thenBranch instanceof GrSynchronizedStatement)) {
        return;
      }
      final GrSynchronizedStatement syncStatement =
          (GrSynchronizedStatement) thenBranch;
      final GrCodeBlock body = syncStatement.getBody();
      if (body == null) {
        return;
      }
      final GrStatement[] statements = body.getStatements();
      if (statements.length != 1) {
        return;
      }
      if (!(statements[0] instanceof GrIfStatement)) {
        return;
      }
      final GrIfStatement innerIf = (GrIfStatement) statements[0];
      final GrCondition innerCondition = innerIf.getCondition();
      if (!(innerCondition instanceof GrExpression)) {
        return;
      }
      if (!EquivalenceChecker.expressionsAreEquivalent((GrExpression) innerCondition,
          (GrExpression) outerCondition)) {
        return;
      }
      if (ignoreOnVolatileVariables &&
          ifStatementAssignsVolatileVariable(innerIf)) {
        return;
      }
      registerStatementError(statement);
    }

    private boolean ifStatementAssignsVolatileVariable(
        GrIfStatement statement) {
      GrStatement innerThen = statement.getThenBranch();
      innerThen = ControlFlowUtils.stripBraces(innerThen);
      if (!(innerThen instanceof GrAssignmentExpression)) {
        return false;
      }
      final GrAssignmentExpression assignmentExpression =
          (GrAssignmentExpression) innerThen;
      final GrExpression lhs =
          assignmentExpression.getLValue();
      if (!(lhs instanceof GrReferenceExpression)) {
        return false;
      }
      final GrReferenceExpression referenceExpression =
          (GrReferenceExpression) lhs;
      final PsiElement element =
          referenceExpression.resolve();
      if (!(element instanceof PsiField)) {
        return false;
      }
      final PsiField field = (PsiField) element;
      return field.hasModifierProperty(PsiModifier.VOLATILE);
    }
  }
}
