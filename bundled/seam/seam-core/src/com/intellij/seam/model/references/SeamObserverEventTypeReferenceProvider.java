package com.intellij.seam.model.references;

import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;

/**
 * User: Sergey.Vasiliev
 */
public class SeamObserverEventTypeReferenceProvider extends BasePsiLiteralExpressionReferenceProvider {

  protected PsiReference getPsiLiteralExpressionReference(final PsiLiteralExpression literalExpression) {
    return new SeamObserverEventTypeReference<PsiLiteralExpression>(literalExpression) {
      protected String getEventType(final PsiLiteralExpression psiElement) {
        return (String)psiElement.getValue();
      }
    };
  }
}
