package com.intellij.velocity.psi.directives;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 29.04.2008
 */
public interface VtlDirectiveHolder extends PsiElement {
    @NotNull
    VtlDirective[] getDirectiveChildren();

    @NotNull
    String getPresentableName();
}