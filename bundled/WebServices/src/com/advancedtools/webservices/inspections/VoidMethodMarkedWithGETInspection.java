package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.inspections.fixes.ChangeMethodReturnTypeFix;
import static com.advancedtools.webservices.rest.RestAnnotations.GET;
import static com.advancedtools.webservices.utils.RestUtils.isAnnotatedAs;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

/**
 * @by Konstantin Bulenkov
 */
public class VoidMethodMarkedWithGETInspection extends BaseWebServicesInspection {
  protected void checkMember(final ProblemsHolder problemsHolder, final PsiMember psiMember) {
    if (! (psiMember instanceof PsiMethod)) return;
    final PsiMethod method = (PsiMethod)psiMember;
    PsiType type = method.getReturnType();
    if (PsiType.VOID.equals(type) && isAnnotatedAs(GET, method)) {
      problemsHolder.registerProblem(method.getReturnTypeElement(),
                                     WSBundle.message("webservices.inspections.rest.void.method.annotated.as.get.problem"),
                                     ProblemHighlightType.ERROR,
                                     new ChangeMethodReturnTypeFix(PsiType.getJavaLangString(method.getManager(), GlobalSearchScope.projectScope(method.getProject())),
                                                                   WSBundle.message("webservices.inspections.rest.void.method.annotated.as.get.fix.name")));        
    }
  }

  protected void doCheckClass(final PsiClass c, final ProblemsHolder problemsHolder) {
  }

  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.rest.void.method.annotated.as.get.display.name");
  }

  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.rest.void.method.annotated.as.get.short.name");
  }
}
