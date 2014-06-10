package com.sixrr.xrp.base;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.refactoring.RefactoringActionHandler;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAttributeRefactoringHandler implements RefactoringActionHandler {
    public void invoke(@NotNull Project project,
                       Editor editor,
                       PsiFile file,
                       DataContext dataContext) {
        final XmlAttribute selectedAttribute = findSelectedAttribute(editor, file);
        if (selectedAttribute != null) {
            handleAttribute(selectedAttribute, project);
        } else {
            final String message = "Cannot perform the refactoring.\n" +
                    "The caret should be positioned at the attribute to be refactored.";
            showErrorMessage(message, project);
        }
    }

    private void showErrorMessage(String message, Project project) {

        ErrorMessageUtil.showErrorMessage(getRefactoringName(), message, getHelpID(), project);
    }

    protected abstract String getHelpID();

    protected abstract String getRefactoringName();

    private static XmlAttribute findSelectedAttribute(Editor editor, PsiFile file) {
        final ScrollingModel scrollingModel = editor.getScrollingModel();
        scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
        final CaretModel caretModel = editor.getCaretModel();
        final int position = caretModel.getOffset();
        PsiElement element = file.findElementAt(position);
        while (element != null) {
            if (element instanceof XmlAttribute) {
                return (XmlAttribute) element;
            }
            element = element.getParent();
        }
        return null;
    }

    public void invoke(@NotNull Project project,
                       @NotNull PsiElement[] elements,
                       DataContext dataContext) {
        if (elements.length == 1 && elements[0] instanceof XmlAttribute) {
            handleAttribute((XmlAttribute) elements[0], project);
        }
    }

    protected abstract void handleAttribute(XmlAttribute attribute, Project project);
}
