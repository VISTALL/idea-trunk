package com.advancedtools.webservices.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public abstract class MyPathReferenceProvider extends MyReferenceProvider {
  @NotNull
  public abstract PsiReference[] getReferencesByString(String s, PsiElement psiElement, int i);
}