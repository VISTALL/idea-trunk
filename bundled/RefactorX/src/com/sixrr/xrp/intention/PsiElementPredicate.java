package com.sixrr.xrp.intention;

import com.intellij.psi.PsiElement;

public interface PsiElementPredicate {
    boolean satisfiedBy(PsiElement element);
}
