/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopBundle;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class AroundAdviceStyleInspection extends AbstractAopInspection {
  private static final Logger LOG = Logger.getInstance("#com.intellij.aop.jam.AroundAdviceStyleInspection");

  protected void checkAopMethod(final PsiMethod pointcutMethod, final LocalAopModel model, final ProblemsHolder holder,
                                final AopPointcutExpressionFile aopFile) {
    if (model.getArgNamesManipulator().getAdviceType() != AopAdviceType.AROUND) return;

    final PsiElementFactory factory = JavaPsiFacade.getInstance(pointcutMethod.getProject()).getElementFactory();

    if (PsiType.VOID.equals(pointcutMethod.getReturnType()) && pointcutMethod.getReturnTypeElement() != null) {
      holder.registerProblem(model.getArgNamesManipulator().getCommonProblemElement(), AopBundle.message("around.advice.should.return.something"), new LocalQuickFix() {
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
          if (!pointcutMethod.isValid()) return;

          try {
            final PsiClassType object = PsiType.getJavaLangObject(pointcutMethod.getManager(), pointcutMethod.getResolveScope());
            pointcutMethod.getReturnTypeElement().replace(factory.createTypeElement(object));
          }
          catch (IncorrectOperationException e) {
            LOG.error(e);
          }
        }

        @NotNull
        public String getFamilyName() {
          return AopBundle.message("change.return.type.to.Object");
        }

        @NotNull
        public String getName() {
          return getFamilyName();
        }
      });
      return;
    }

    final PsiParameter[] parameters = pointcutMethod.getParameterList().getParameters();
    if (parameters.length == 0 || !parameters[0].getType().equalsToText(AopConstants.PROCEEDING_JOIN_POINT)) {
      holder.registerProblem(model.getArgNamesManipulator().getCommonProblemElement(), AopBundle.message("around.advice.call.cannot.proceed"), new LocalQuickFix() {
        @NotNull
        public String getFamilyName() {
          return AopBundle.message("add.pjp.parameter");
        }

        @NotNull
        public String getName() {
          return getFamilyName();
        }

        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
          if (!pointcutMethod.isValid()) return;

          try {
            final PsiParameter newParameter =
                factory.createParameter("pjp", factory.createTypeFromText(AopConstants.PROCEEDING_JOIN_POINT, pointcutMethod));
            final PsiParameterList list = pointcutMethod.getParameterList();
            if (list.getParametersCount() == 0) {
              list.add(newParameter);
            } else {
              list.addBefore(newParameter, list.getParameters()[0]);
            }
          }
          catch (IncorrectOperationException e) {
            LOG.error(e);
          }
        }
      });
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return AopBundle.message("around.advice.style.inspection");
  }

  @NotNull
  public String getShortName() {
    return "AroundAdviceStyleInspection";
  }
}