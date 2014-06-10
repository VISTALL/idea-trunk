package com.advancedtools.webservices.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

/**
 * @by Konstantin Bulenkov
 */
public class ChangeMethodReturnTypeFix implements LocalQuickFix {
  PsiType myType;
  String myName;

  public ChangeMethodReturnTypeFix(@NotNull PsiType type, @NotNull String name) {
    myType = type;
    myName = name;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (! (element instanceof PsiTypeElement)) return;
    PsiTypeElement returnType = (PsiTypeElement)element;
    try {
      PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
      PsiType type = PsiType.getJavaLangString(returnType.getManager(), GlobalSearchScope.allScope(project));
      PsiMethod method = (PsiMethod)returnType.getParent();
      returnType.replace(factory.createTypeElement(type));
      PsiElement returnStatement = factory.createStatementFromText("return null; //TODO replace this stub to something useful", null);

      final PsiCodeBlock body = method.getBody();
      if (body != null) {
        body.add(returnStatement);
      }
    } catch(Exception e) {//
    }
  }
}
