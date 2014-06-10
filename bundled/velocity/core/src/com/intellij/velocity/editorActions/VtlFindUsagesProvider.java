/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.editorActions;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.VtlVariable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlFindUsagesProvider implements FindUsagesProvider {
    public WordsScanner getWordsScanner() {
        return null;
    }

    public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
        return psiElement instanceof VtlVariable;
    }

    public String getHelpId(@NotNull final PsiElement psiElement) {
        return null;
    }

    @NotNull
    public String getType(@NotNull final PsiElement element) {
        return VelocityBundle.message("type.name.variable");
    }

    @NotNull
    public String getDescriptiveName(@NotNull final PsiElement element) {
        return VelocityBundle.message("type.name.variable");
    }

    @NotNull
    public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
        if (element instanceof VtlVariable) {
            return ((VtlVariable) element).getName();
        }
        return element.getText();
    }
}
