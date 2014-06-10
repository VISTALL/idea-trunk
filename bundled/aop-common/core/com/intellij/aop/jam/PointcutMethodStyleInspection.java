/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopBundle;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.aop.psi.PsiIfPointcutExpression;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class PointcutMethodStyleInspection extends AbstractAopInspection {

  protected void checkAopMethod(final PsiMethod pointcutMethod, final LocalAopModel model, final ProblemsHolder holder,
                                final AopPointcutExpressionFile aopFile) {
    if (model.getArgNamesManipulator().getAdviceType() != null) return;

    final PsiElement problemElement = model.getArgNamesManipulator().getCommonProblemElement();
    if (pointcutMethod.getReturnType() != PsiType.VOID) {
      final PsiPointcutExpression expression = aopFile.getPointcutExpression();
      if (expression instanceof PsiIfPointcutExpression && PsiType.BOOLEAN.equals(pointcutMethod.getReturnType())) {
        return;
      }

      holder.registerProblem(problemElement, AopBundle.message("pointcut.methods.should.have.void.return.type"));
      return;
    }

    if (pointcutMethod.getThrowsList().getTextLength() > 0) {
      holder.registerProblem(problemElement, AopBundle.message("pointcut.methods.should.have.no.throws.clause"));
      return;
    }

    final PsiCodeBlock body = pointcutMethod.getBody();
    if (body != null && body.getStatements().length > 0) {
      holder.registerProblem(problemElement, AopBundle.message("pointcut.methods.should.have.empty.body"));
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return AopBundle.message("pointcut.method.style.inspection");
  }

  @NotNull
  public String getShortName() {
    return "PointcutMethodStyleInspection";
  }
}
