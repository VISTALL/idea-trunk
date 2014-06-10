package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

public interface CfmlVariable extends PsiNamedElement {
    @Nullable
    PsiType getPsiType();
}
