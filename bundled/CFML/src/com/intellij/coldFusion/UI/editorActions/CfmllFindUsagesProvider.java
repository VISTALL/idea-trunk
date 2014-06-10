package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 12.02.2009
 */
public class CfmllFindUsagesProvider implements FindUsagesProvider {
    public WordsScanner getWordsScanner() {
        return null;
    }

    public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
        return psiElement instanceof CfmlReferenceExpression;
    }

    public String getHelpId(@NotNull final PsiElement psiElement) {
        return null;
    }

    @NotNull
    public String getType(@NotNull final PsiElement element) {
        return "reference";
    }

    @NotNull
    public String getDescriptiveName(@NotNull final PsiElement element) {
        return "reference";
    }

    @NotNull
    public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
        return element.getText();
    }
}

