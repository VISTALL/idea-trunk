/*
 * Copyright 2003-2007 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.threading;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.QuickFixFactory;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ControlFlowUtils;
import com.siyeh.ig.psiutils.EquivalenceChecker;
import com.siyeh.ig.psiutils.SideEffectChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DoubleCheckedLockingInspection extends BaseInspection {

    /** @noinspection PublicField*/
    public boolean ignoreOnVolatileVariables = false;

    @NotNull
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "double.checked.locking.display.name");
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "double.checked.locking.problem.descriptor");
    }

    @Nullable
    public JComponent createOptionsPanel() {
        return new SingleCheckboxOptionsPanel(InspectionGadgetsBundle.message(
                "double.checked.locking.ignore.on.volatiles.option"), this, 
                "ignoreOnVolatileVariables"
        );
    }

    public BaseInspectionVisitor buildVisitor() {
        return new DoubleCheckedLockingVisitor();
    }

    private class DoubleCheckedLockingVisitor
            extends BaseInspectionVisitor {

        @Override public void visitIfStatement(@NotNull PsiIfStatement statement) {
            super.visitIfStatement(statement);
            final PsiExpression outerCondition = statement.getCondition();
            if (outerCondition == null) {
                return;
            }
            if (SideEffectChecker.mayHaveSideEffects(outerCondition)) {
                return;
            }
            PsiStatement thenBranch = statement.getThenBranch();
            thenBranch = ControlFlowUtils.stripBraces(thenBranch);
            if (!(thenBranch instanceof PsiSynchronizedStatement)) {
                return;
            }
            final PsiSynchronizedStatement syncStatement =
                    (PsiSynchronizedStatement)thenBranch;
            final PsiCodeBlock body = syncStatement.getBody();
            if (body == null) {
                return;
            }
            final PsiStatement[] statements = body.getStatements();
            if (statements.length != 1) {
                return;
            }
            if (!(statements[0] instanceof PsiIfStatement)) {
                return;
            }
            final PsiIfStatement innerIf = (PsiIfStatement)statements[0];
            final PsiExpression innerCondition = innerIf.getCondition();
            if (!EquivalenceChecker.expressionsAreEquivalent(innerCondition,
                    outerCondition)) {
                return;
            }
          PsiField field = null;
          if (ignoreOnVolatileVariables) {
            field = findCheckedField(innerIf);
            if (field != null && field.hasModifierProperty(PsiModifier.VOLATILE)) {
              return;
            }
          }
          registerStatementError(statement, field);
        }

        @Nullable
        private PsiField findCheckedField(PsiIfStatement statement) {
          final PsiExpression expression = statement.getCondition();
          if (expression instanceof PsiBinaryExpression) {
            PsiBinaryExpression binaryExpression = (PsiBinaryExpression)expression;
            if (binaryExpression.getOperationTokenType() == JavaTokenType.EQEQ) {
              final PsiExpression lOperand = binaryExpression.getLOperand();
              final PsiExpression rOperand = binaryExpression.getROperand();

              PsiExpression fieldReference = null;
              if (PsiType.NULL.equals(lOperand.getType())) {
                fieldReference = rOperand;
              }
              else if (rOperand != null && PsiType.NULL.equals(rOperand.getType())) {
                fieldReference = lOperand;
              }

              if (fieldReference instanceof PsiReferenceExpression) {
                final PsiElement resolved = ((PsiReferenceExpression)fieldReference).resolve();
                return resolved instanceof PsiField ? (PsiField)resolved : null;
              }
            }
          }
          return null;
        }
    }

  protected InspectionGadgetsFix buildFix(final Object... infos) {
    if (infos == null || infos.length == 0 || infos[0] == null) return null;
    final PsiField field = (PsiField)infos[0];
    final IntentionAction action = QuickFixFactory.getInstance().createModifierListFix(field, PsiModifier.VOLATILE, true, true);

    return new InspectionGadgetsFix() {
      protected void doFix(final Project project, final ProblemDescriptor descriptor) throws IncorrectOperationException {
        if (action.isAvailable(project, null, null)) {
          action.invoke(project, null, null);
        }
      }

      @NotNull
      public String getName() {
        return action.getText();
      }
    };
  }
}