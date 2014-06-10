package com.sixrr.xrp.tagtoattribute;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.ErrorMessageUtil;
import com.sixrr.xrp.context.Context;
import org.jetbrains.annotations.NotNull;

class TagToAttributeHandler implements RefactoringActionHandler {
    private static final String REFACTORING_NAME = "Tag To Attribute";

    public void invoke(@NotNull Project project,
                       Editor editor,
                       PsiFile file,
                       DataContext dataContext) {
        final XmlTag selectedTag = findSelectedTag(editor, file);
        if (selectedTag == null) {
            final String message = "Cannot perform the refactoring.\n" +
                    "The caret should be positioned at a tag to be refactored.";
            ErrorMessageUtil.showErrorMessage(REFACTORING_NAME, message, RefactorXHelpID.TagToAttribute, project);
        } else  if(selectedTag.getParentTag()==null) {
            final String message = "Cannot perform the refactoring.\n" +
                    "This refactoring is not available for top-level tags.";
            ErrorMessageUtil.showErrorMessage(REFACTORING_NAME, message, RefactorXHelpID.TagToAttribute, project);
        } else {
            handleDeleteAttribute(selectedTag, project);
        }
    }

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
            handleDeleteAttribute((XmlTag) elements[0], project);
        }
    }



    private static void handleDeleteAttribute(final XmlTag tag, Project project) {
        final TagToAttributeDialog dialog =
                new TagToAttributeDialog(tag);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final String attributeName = dialog.getAttributeName();
        final boolean previewUsages = dialog.isPreviewUsages();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final TagToAttributeProcessor processor =
                                new TagToAttributeProcessor(tag, attributeName, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Replace Tag With Attribute", null);

    }

}
