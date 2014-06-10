/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class AopHighlightErrorFilter extends HighlightErrorFilter {

  public boolean shouldHighlightErrorElement(@NotNull final PsiErrorElement element) {
    return !value(element);
  }

  public static boolean value(final PsiErrorElement psiErrorElement) {
    final PsiFile file = psiErrorElement.getContainingFile();
    if (file instanceof AopPointcutExpressionFile) {
      if (((AopPointcutExpressionFile)file).getAopModel().getAdvisedElementsSearcher().shouldSuppressErrors()) return true;
    }

    return false;
  }
}
