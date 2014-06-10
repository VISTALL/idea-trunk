package com.intellij.seam.model.references;

import com.intellij.psi.PsiReference;

/**
 * User: Sergey.Vasiliev
 */
public class SeamRaiseEventRefernceProvider extends BasePsiLiteralExpressionReferenceProvider {

  protected PsiReference getPsiLiteralExpressionReference(final com.intellij.psi.PsiLiteralExpression literalExpression) {
    return new SeamEventTypeReference.SeamLiteralExpression(literalExpression);
  }
}