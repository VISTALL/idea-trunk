package com.intellij.velocity.psi.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class SoftFileReferenceSet extends FileReferenceSet {
    public SoftFileReferenceSet(@NotNull String text, PsiElement element, int startInElement) {
        super(text, element, startInElement, null, true);
    }

    protected boolean isSoft() {
        return true;
    }
}


