package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.inspections.fixes.RemoveElementFix;
import static com.advancedtools.webservices.rest.RestAnnotations.CONSUME_MIME;
import static com.advancedtools.webservices.rest.RestAnnotations.CONSUMES;
import static com.advancedtools.webservices.rest.RestAnnotations.PRODUCE_MIME;
import static com.advancedtools.webservices.rest.RestAnnotations.PRODUCES;
import static com.advancedtools.webservices.utils.RestUtils.getAnnotationValue;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

//import javax.activation.MimeType;
//import javax.activation.MimeTypeParseException;

/**
 * @by Konstantin Bulenkov
 */
public class ValidMimeAnnotationInspection extends BaseWebServicesInspection {
  private static final @NonNls Pattern myPattern = Pattern.compile("[^\\p{Cc}^\\s]+/[^\\p{Cc}^\\s]+");

  protected void checkMember(final ProblemsHolder problemsHolder, final PsiMember psiMember) {
    if (psiMember instanceof PsiMethod) {
      PsiModifierList modifiers = psiMember.getModifierList();
      if (modifiers == null) return;
      checkMime(modifiers.findAnnotation(CONSUME_MIME), problemsHolder);
      checkMime(modifiers.findAnnotation(CONSUMES), problemsHolder);
      checkMime(modifiers.findAnnotation(PRODUCE_MIME), problemsHolder);
      checkMime(modifiers.findAnnotation(PRODUCES), problemsHolder);
    }
  }

  private static void checkMime(PsiAnnotation anno, ProblemsHolder problemsHolder) {
    if (anno == null) return;
    String mime = getAnnotationValue(anno);
    PsiNameValuePair[] attrs = anno.getParameterList().getAttributes();
    if (mime == null || attrs.length == 0) return;
    if (!myPattern.matcher(mime).matches()) {
      problemsHolder.registerProblem(attrs[0], WSBundle.message("webservices.inspections.rest.annotation.has.wrong.mime.type.problem"),
                                     ProblemHighlightType.ERROR,
                                     new RemoveElementFix(attrs[0], WSBundle.message("webservices.inspections.rest.annotation.has.wrong.mime.type.fix")));
    }
  }

  protected void doCheckClass(final PsiClass c, final ProblemsHolder problemsHolder) {
  }

  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.rest.annotation.has.wrong.mime.type.display.name");
  }

  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.rest.annotation.has.wrong.mime.type.short.name");
  }
}
