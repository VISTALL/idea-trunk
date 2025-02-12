package com.intellij.spring.model;

import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;

/**
 * @author Dmitry Avdeev
 */
public interface PsiElementPointer {
  @Nullable
  PsiElement getPsiElement();
}
