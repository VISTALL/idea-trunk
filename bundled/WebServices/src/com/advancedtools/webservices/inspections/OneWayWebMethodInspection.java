package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim
 */
public class OneWayWebMethodInspection extends BaseWebServicesInspection {
  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.oneway.operation.display.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.oneway.operation.short.name");
  }

  protected final void doCheckClass(PsiClass c, ProblemsHolder problemsHolder) {
  }

  protected final void checkMember(final ProblemsHolder problemsHolder, PsiMember member) {
    if (member instanceof PsiMethod) {
      final PsiMethod method = (PsiMethod) member;

      final PsiAnnotation annotation = AnnotationUtil.findAnnotation(method, JWSDPWSEngine.wsOneWayMethodSet);

      if (annotation != null) {
        final PsiTypeElement returnTypeElement = method.getReturnTypeElement();

        if (returnTypeElement != null && !returnTypeElement.getText().equals("void")) {
          final List<LocalQuickFix> quickfixes = new ArrayList<LocalQuickFix>(2);
          quickfixes.add(new RemoveElementFix(annotation));
          quickfixes.add(new ReplaceTypeElementFix(returnTypeElement, "void"));

          problemsHolder.registerProblem(
            returnTypeElement,
            WSBundle.message("webservices.inspections.oneway.operation.problem"), 
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            quickfixes.toArray(new LocalQuickFix[quickfixes.size()])
          );
        }
      }
    }
  }

  private static class ReplaceTypeElementFix implements LocalQuickFix {
    private final PsiElement annotation;
    private final String text;

    public ReplaceTypeElementFix(@NotNull PsiElement annotation, @NotNull String text) {
      this.annotation = annotation;
      this.text = text;
    }

    @NotNull
    public String getName() {
      return WSBundle.message("webservices.inspections.oneway.operation.replace.returntype.with.void.fix.name");
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final PsiElement psiElement = descriptor.getPsiElement();
      if (psiElement == null) return;
      if (!EnvironmentFacade.getInstance().prepareFileForWrite(psiElement.getContainingFile())) return;
      try {
        final PsiElementFactory psiElementFactory = EnvironmentFacade.getInstance().getElementsFactory(psiElement.getProject());
        PsiElement replacement = psiElementFactory.createTypeElement(psiElementFactory.createTypeFromText(text, psiElement));
        annotation.replace(replacement);
      } catch (IncorrectOperationException e) {
        throw new RuntimeException(e);
      }

    }
  }

  private static class RemoveElementFix implements LocalQuickFix {
    private final PsiElement annotation;

    public RemoveElementFix(@NotNull PsiElement annotation) {
      this.annotation = annotation;
    }

    @NotNull
    public String getName() {
      return WSBundle.message("webservices.inspections.oneway.operation.remove.annotation.fix.name");
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final PsiElement psiElement = descriptor.getPsiElement();
      if (psiElement == null) return;
      if (!EnvironmentFacade.getInstance().prepareFileForWrite(psiElement.getContainingFile())) return;
      try {
        annotation.delete();
      } catch (IncorrectOperationException e) {
        throw new RuntimeException(e);
      }

    }
  }
}
