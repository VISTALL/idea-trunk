package com.intellij.spring.references;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class SpringQualifierNameReferenceProvider extends PsiReferenceProviderBase {

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
    if (element instanceof PsiLiteralExpression) {
      final PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
      if (literalExpression.getValue() instanceof String) {
        final PsiModifierListOwner psiModifierListOwner = PsiTreeUtil.getParentOfType(element, PsiModifierListOwner.class);
        if (psiModifierListOwner instanceof PsiClass) {
          // stereotype components (@Service, @Component, @Repository, etc.)
          final PsiAnnotation qualifierAnnotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
          if (qualifierAnnotation != null) {
          return new PsiReference[] {new PsiReferenceBase<PsiElement>(element) {
            public PsiElement resolve() {
              return qualifierAnnotation;
            }

            public Object[] getVariants() {
              return EMPTY_ARRAY;
            }
          }};
          }
        }  else {
          // @Autowired components (method, field, parameter)
          return new PsiReference[]{new SpringQualifierReference(literalExpression)};
        }
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}