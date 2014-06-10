package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.index.FileBasedWSIndex;
import com.advancedtools.webservices.index.WSIndexEntry;
import com.advancedtools.webservices.utils.PsiUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public class EmptyWebServiceInspection extends BaseWebServicesInspection {
  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.empty.webservice.display.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.empty.webservice.short.name");
  }

  protected void doCheckClass(PsiClass c, ProblemsHolder problemsHolder) {
    final ExternallyBoundClassContext context = getClassContext(c);
    if (!context.isExternallyBound()) {
      return;
    }

    // Check if something is exposed
    WSIndexEntry[] entries = context.getEntries(PsiUtil.findModule(c));
    if (entries.length > 0 && FileBasedWSIndex.WS_TYPE.equals(entries[0].getWsStatus(c)) ||
        context.annotation != null && context.annotation.getNameReferenceElement().getText().equals(WEB_SERVICE_ANNOTATION_NAME)
       ) {
      boolean hasPublicMethod = false;

      for(PsiMethod m:c.getAllMethods()) {
        if (m.isConstructor() || m.hasModifierProperty(PsiModifier.STATIC)) continue;
        if(m.hasModifierProperty(PsiModifier.PUBLIC) && !"Object".equals(m.getContainingClass().getName())) {
          hasPublicMethod = true;
          break;
        }
      }

      if (!hasPublicMethod) {
        problemsHolder.registerProblem(c.getNameIdentifier(), "Web service or xml mapped object without methods",
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING, EMPTY);
      }
    }
  }

  protected final void checkMember(final ProblemsHolder problemsHolder,PsiMember member) {}
}
