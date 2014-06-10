package com.advancedtools.webservices.references;

import com.intellij.psi.ResolveResult;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 */
class MyResolveResult implements ResolveResult {
  private final PsiElement myelement;

  MyResolveResult(PsiElement _element) { myelement = _element; }

  @Nullable
  public PsiElement getElement() {
    return myelement;
  }

  public boolean isValidResult() {
    return true;
  }
}
