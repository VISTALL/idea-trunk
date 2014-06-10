package com.advancedtools.webservices.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

/**
 * @author Maxim
 */
public abstract class MyReferenceProvider {
  public static final MyReferenceProvider[] EMPTY = new MyReferenceProvider[0];

  public abstract PsiReference[] getReferencesByElement(PsiElement psiElement);
}
