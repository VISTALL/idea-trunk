package com.intellij.velocity;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.velocity.psi.VtlLanguage;
import com.intellij.velocity.psi.directives.VtlDirective;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.xml.breadcrumbs.BreadcrumbsInfoProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlBreadcrumbsInfoProvider extends BreadcrumbsInfoProvider {
    public Language[] getLanguages() {
        return new Language[]{VtlLanguage.INSTANCE};
    }

    public boolean acceptElement(@NotNull final PsiElement e) {
        return e instanceof VtlDirective;
    }

    public PsiElement getParent(@NotNull final PsiElement e) {
        VtlDirective directive = PsiTreeUtil.getParentOfType(e, VtlDirective.class);
        return directive instanceof VtlFile ? null : directive;
    }

    @NotNull
    public String getElementInfo(@NotNull final PsiElement e) {
        return ((VtlDirective) e).getPresentableName();
    }

    public String getElementTooltip(@NotNull final PsiElement e) {
        return null;
    }
}
