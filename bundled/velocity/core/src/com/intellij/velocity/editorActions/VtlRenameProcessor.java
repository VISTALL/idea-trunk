package com.intellij.velocity.editorActions;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.psi.directives.VtlForeach;
import com.intellij.openapi.editor.Editor;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.05.2008
 */
public class VtlRenameProcessor extends RenamePsiElementProcessor {
    @Override
    public void renameElement(PsiElement element, String newName, UsageInfo[] usages, RefactoringElementListener listener) throws IncorrectOperationException {
        // fixed name variable cannot be renamed
    }

    @Override
    public PsiElement substituteElementToRename(PsiElement element, Editor editor) {
        return null;
    }

    public boolean canProcessElement(final PsiElement element) {
        return element instanceof VtlForeach.FixedNameReferenceElement;
    }
}
