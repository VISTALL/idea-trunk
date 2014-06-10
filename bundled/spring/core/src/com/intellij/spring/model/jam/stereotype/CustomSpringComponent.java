package com.intellij.spring.model.jam.stereotype;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

public class CustomSpringComponent extends SpringStereotypeElement {
  private PsiClass myPsiClass;

  public CustomSpringComponent(@NotNull String anno, @NotNull PsiClass psiClass) {
    super(anno, psiClass);

    myPsiClass = psiClass;
  }

  @NotNull
  public PsiClass getPsiElement() {
    return myPsiClass;
  }
}