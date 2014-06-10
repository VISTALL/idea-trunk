package com.sixrr.xrp.wraptagcontents;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseTagRefactoringHandler;
import com.sixrr.xrp.context.Context;

class WrapTagContentsHandler extends BaseTagRefactoringHandler {


    protected String getRefactoringName() {
        return "Wrap Tag Contents";
    }

    protected String getHelpID() {
        return RefactorXHelpID.WrapTagContents;
    }

    protected void handleTag(final XmlTag tag, Project project) {
        final WrapTagContentsDialog dialog =
                new WrapTagContentsDialog(tag);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final String attributeName = dialog.getWrappingTagName();
        final boolean previewUsages = dialog.isPreviewUsages();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final WrapTagContentsProcessor processor =
                                new WrapTagContentsProcessor(tag, attributeName, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Wrap Tag Contents", null);

    }

}
