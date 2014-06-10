package com.intellij.spring.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class SpringBeanNamesReferenceProvider extends PsiReferenceProviderBase {
  final public static String[] METHODS = new String[] {"containsBean", "getBean", "isSingleton", "getType", "getAliases"};

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
    if (element instanceof PsiLiteralExpression) {
      final PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
      if (literalExpression.getValue() instanceof String) {
        return new PsiReference[] {
          new SpringBeanReference(literalExpression)
        };
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }

}
