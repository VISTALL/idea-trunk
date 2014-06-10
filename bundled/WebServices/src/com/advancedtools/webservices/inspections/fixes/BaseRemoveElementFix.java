package com.advancedtools.webservices.inspections.fixes;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @by Konstantin Bulenkov
 */

public abstract class BaseRemoveElementFix implements LocalQuickFix {
    private final PsiElement element;

    public BaseRemoveElementFix(@NotNull PsiElement element) {
      this.element = element;
    }

    @NotNull
    public abstract String getName();

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final PsiElement psiElement = descriptor.getPsiElement();
      if (psiElement == null) return;
      if (!EnvironmentFacade.getInstance().prepareFileForWrite(psiElement.getContainingFile())) return;
      try {
        element.delete();
      } catch (IncorrectOperationException e) {
        throw new RuntimeException(e);
      }
    }
}
