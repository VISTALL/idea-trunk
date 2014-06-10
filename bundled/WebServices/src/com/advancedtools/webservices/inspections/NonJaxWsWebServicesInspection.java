package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.index.FileBasedWSIndex;
import com.advancedtools.webservices.index.WSIndexEntry;
import com.advancedtools.webservices.utils.PsiUtil;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public class NonJaxWsWebServicesInspection extends MarkWebServiceMembersBase {
  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  protected void checkMember(ProblemsHolder problemsHolder, PsiMember psiMember) {
  }

  protected void doCheckClass(PsiClass c, ProblemsHolder problemsHolder) {
    try {
      final ExternallyBoundClassContext context = getClassContext(c);

      if (context == null || !context.isExternallyBound()) {
        removeHighlightersIfExist(c);
        return;
      }

      WSIndexEntry[] entries = context.getEntries(PsiUtil.findModule(c));
      if (entries.length == 0 || entries[0] == null) return;

      if (context.annotation == null && FileBasedWSIndex.WS_TYPE.equals(entries[0].getWsStatus(c))) {
        problemsHolder.registerProblem(
          findNameIdentifier(c),
          WSBundle.message("webservices.inspections.nonjaxwswebservices.inspectionwebservice.problem"),
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
          new LocalQuickFix[] {new AnnotateAsWebServiceFix()}
        );
      }
      addOrUpdateHighlighter(c, context);
    } finally {
      removeInvalidHighlighters(c.getParent());
    }
  }

  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.nonjaxwswebservices.inspection.display.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.nonjaxwswebservices.inspection.short.name");
  }

  static class AnnotateAsWebServiceFix extends InsertAnnotationFix implements LocalQuickFix {
    public AnnotateAsWebServiceFix() {
      super("@javax.jws.WebService");
    }

    @NotNull
    public String getName() {
      return WSBundle.message("webservices.inspections.nonjaxwswebservices.inspection.annotate.web.service.fix");
    }
  }
}
