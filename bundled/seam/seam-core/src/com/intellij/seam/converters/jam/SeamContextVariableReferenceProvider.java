package com.intellij.seam.converters.jam;

import com.intellij.javaee.JavaeeAnnoNameReference;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class SeamContextVariableReferenceProvider extends PsiReferenceProviderBase {
  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
    final PsiElement psiElement = PsiUtilBase.getOriginalElement(element, PsiLiteralExpression.class);

    if (psiElement != null) {
      final Module module = ModuleUtil.findModuleForPsiElement(psiElement);

      if (module != null) {
        final Object value = JavaPsiFacade.getInstance(psiElement.getProject()).getConstantEvaluationHelper()
          .computeConstantExpression((PsiLiteralExpression)psiElement);
        if (value instanceof String) {
          final List<CommonModelElement> components = SeamCommonUtils.findSeamComponents((String)value, module);
          final CommonModelElement resolveTo = components.size() > 0 ? components.get(0) : null;

          return new PsiReference[]{new JavaeeAnnoNameReference(element, resolveTo) {
            public Object[] getVariants() {
              return SeamCommonUtils.getSeamContextVariableNames(module).toArray();
            }
          }};
        }
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

}
