package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.index.WSIndexEntry;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.utils.PsiUtil;
import com.advancedtools.webservices.utils.DeployUtils;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Maxim
 */
public class WebMethodExposedImplicitlyInspection extends MarkWebServiceMembersBase {
  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.INFO;
  }

  protected void checkMember(ProblemsHolder problemsHolder, PsiMember psiMember) {
    if (psiMember.getContainingClass() == null) return;
    final ExternallyBoundClassContext context = getClassContext(psiMember.getContainingClass());
    if (!context.isExternallyBound()) {
      removeHighlightersIfExist(psiMember);
      return;
    }

    addOrUpdateHighlighter(psiMember, context);
    if (psiMember instanceof PsiMethod &&
        ( context.annotation == null ||
          AnnotationUtil.findAnnotation(psiMember, JWSDPWSEngine.wsMethodsSet) == null
        )
       ) {
      final Module module = PsiUtil.findModule(psiMember);
      final WSIndexEntry[] entries = context.index.getWsEntries(Arrays.asList(context.getEntries(module)), psiMember);
      
      if (entries.length == 0 || !DeployUtils.canBeWebMethod(psiMember)) return;

      problemsHolder.registerProblem(
        findNameIdentifier(psiMember),
        WSBundle.message("webservices.inspections.implicitly.exposed.webmethod.inspection.webmethod.problem", psiMember.getName()),
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        new LocalQuickFix[] {new WebMethodExposedImplicitlyInspection.AnnotateAsWebMethodFix()}
      );
    }
  }

  protected void doCheckClass(PsiClass c, ProblemsHolder problemsHolder) {
    removeInvalidHighlighters(c);
  }

  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.implicitly.exposed.webmethod.inspection.display.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.implicitly.exposed.webmethod.inspection.short.name");
  }

  static class AnnotateAsWebMethodFix extends InsertAnnotationFix implements LocalQuickFix {
    protected AnnotateAsWebMethodFix() {
      super("@javax.jws.WebMethod");
    }

    @NotNull
    public String getName() {
      return WSBundle.message("webservices.inspections.implicitly.exposed.webmethod.inspection.annotate.web.method.fix");
    }
  }
}
