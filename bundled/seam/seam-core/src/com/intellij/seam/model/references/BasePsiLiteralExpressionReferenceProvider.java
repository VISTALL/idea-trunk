package com.intellij.seam.model.references;

import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public abstract class BasePsiLiteralExpressionReferenceProvider extends PsiReferenceProviderBase {

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
    if (element instanceof PsiLiteralExpression) {
      final PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
      if (literalExpression.getValue() instanceof String) {
        return new PsiReference[] {getPsiLiteralExpressionReference(literalExpression)};
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }

  protected abstract PsiReference getPsiLiteralExpressionReference(final PsiLiteralExpression literalExpression);
}