package com.sixrr.xrp.base;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.refactoring.RefactoringActionHandler;
import org.jetbrains.annotations.NotNull;

public abstract class BaseTagRefactoringHandler implements RefactoringActionHandler {
    public void invoke(@NotNull Project project,
                       Editor editor,
                       PsiFile file,
                       DataContext dataContext) {
        final XmlTag selectedTag = findSelectedTag(editor, file);
        if (selectedTag != null) {
            handleTag(selectedTag, project);
        } else {
            final String message = "Cannot perform the refactoring.\n" +
                    "The caret should be positioned at a tag to be refactored.";
            ErrorMessageUtil.showErrorMessage(getRefactoringName(), message, getHelpID(), project);
        }
    }

    protected abstract String getHelpID();

    protected abstract String getRefactoringName();

    private static XmlTag findSelectedTag(Editor editor, PsiFile file) {
        final ScrollingModel scrollingModel = editor.getScrollingModel();
        scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
        final CaretModel caretModel = editor.getCaretModel();
        final int position = caretModel.getOffset();
        PsiElement element = file.findElementAt(position);
        while (element != null) {
            if (element instanceof XmlTag) {
                return (XmlTag) element;
            }
            element = element.getParent();
        }
        return null;
    }

    public void invoke(@NotNull Project project,
                       @NotNull PsiElement[] elements,
                       DataContext dataContext) {
        if (elements.length == 1 && elements[0] instanceof XmlTag) {
            handleTag((XmlTag) elements[0], project);
        }
    }

    protected abstract void handleTag(XmlTag tag, Project project);

}
