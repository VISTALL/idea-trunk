package com.intellij.seam.model.references;

import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NonNls;

/**
 * User: Sergey.Vasiliev
 */
public class SeamEventTypeReferenceProvider extends BasePsiLiteralExpressionReferenceProvider {
  @NonNls final public static String SEAM_EVENTS_CLASSNAME ="org.jboss.seam.core.Events";

  @NonNls final public static String[] METHODS = new String[] {"raiseEvent", "raiseAsynchronousEvent", "raiseTimedEvent", "raiseTransactionSuccessEvent", "raiseTransactionCompletionEvent"};

  protected PsiReference getPsiLiteralExpressionReference(final com.intellij.psi.PsiLiteralExpression literalExpression) {
    return new SeamEventTypeReference.SeamLiteralExpression(literalExpression);
  }
}
