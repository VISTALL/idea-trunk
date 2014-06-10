package com.intellij.webBeans.jam.references;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

public class WebBeansJamReferenceContributor extends PsiReferenceContributor {

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        PsiJavaPatterns.literalExpression().with(new PatternCondition<PsiLiteralExpression>("valueAttr") {
          @Override
          public boolean accepts(@NotNull final PsiLiteralExpression psiLiteralExpression, final ProcessingContext context) {
            final PsiNameValuePair pair = PsiTreeUtil.getParentOfType(psiLiteralExpression, PsiNameValuePair.class);
            if (pair != null) {
              @NonNls final String name = pair.getName();
              if (name == null || name.equals("value")) {
                final String qualifiedName = ((PsiAnnotation)pair.getParent().getParent()).getQualifiedName();
                return qualifiedName != null && (qualifiedName.equals(WebBeansAnnoConstants.NAMED_ANNOTATION));
              }
            }
            return false;
          }
        }),
        new PsiReferenceProviderBase() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            return PsiReference.EMPTY_ARRAY;
            //return new PsiReference[] { PsiReferenceBase.createSelfReference(element, PsiTreeUtil.getParentOfType(element, PsiAnnotation.class)) };
          }
        });
  }
}

