package com.intellij.velocity;

import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import com.intellij.velocity.psi.VtlImplicitVariable;

/**
 * @author Alexey Chmutov
 */
public class VtlTypeDeclarationProvider implements TypeDeclarationProvider {
  public PsiElement[] getSymbolTypeDeclarations(final PsiElement symbol) {
    if (symbol instanceof VtlImplicitVariable) {
      PsiType type = ((VtlImplicitVariable)symbol).getPsiType();
      PsiClass psiClass = PsiUtil.resolveClassInType(type);
      return psiClass == null ? null : new PsiElement[]{psiClass};
    }
    return null;
  }
}
