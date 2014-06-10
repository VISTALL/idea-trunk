package com.intellij.velocity.psi;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public interface VtlVariable extends PsiNamedElement {
    @Nullable
    PsiType getPsiType();
}
