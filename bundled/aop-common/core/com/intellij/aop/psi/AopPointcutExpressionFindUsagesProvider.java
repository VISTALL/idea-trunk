package com.intellij.aop.psi;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class AopPointcutExpressionFindUsagesProvider implements FindUsagesProvider {
  @Nullable
  public WordsScanner getWordsScanner() {
    return null;
  }

  public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
    return false;
  }

  @Nullable
  public String getHelpId(@NotNull final PsiElement psiElement) {
    return null;
  }

  @NotNull
  public String getType(@NotNull final PsiElement element) {
    throw new UnsupportedOperationException("Method getType is not yet implemented in " + getClass().getName());
  }

  @NotNull
  public String getDescriptiveName(@NotNull final PsiElement element) {
    throw new UnsupportedOperationException("Method getDescriptiveName is not yet implemented in " + getClass().getName() + "; element=" + element + " of class=" + element.getClass());
  }

  @NotNull
  public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
    throw new UnsupportedOperationException("Method getNodeText is not yet implemented in " + getClass().getName());
  }
}