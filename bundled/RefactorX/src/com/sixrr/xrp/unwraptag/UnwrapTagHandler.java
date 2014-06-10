package com.sixrr.xrp.unwraptag;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseTagRefactoringHandler;
import com.sixrr.xrp.context.Context;

class UnwrapTagHandler extends BaseTagRefactoringHandler {

    protected String getHelpID() {
        return RefactorXHelpID.UnwrapTag;
    }

    protected String getRefactoringName() {
        return "Unwrap Tag";
    }


    protected  void handleTag(final XmlTag tag, Project project) {
        final UnwrapTagDialog dialog =
                new UnwrapTagDialog(tag);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final boolean previewUsages = dialog.isPreviewUsages();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final UnwrapTagProcessor processor =
                                new UnwrapTagProcessor(tag, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Unwrap Tag", null);

    }

}
