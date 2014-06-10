package com.sixrr.xrp.deleteattribute;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseAttributeRefactoringHandler;
import com.sixrr.xrp.context.Context;

class DeleteAttributeHandler extends BaseAttributeRefactoringHandler {


    protected String getHelpID() {
        return RefactorXHelpID.DeleteAttribute;
    }

    protected String getRefactoringName() {
        return "Delete attribute";
    }

    protected void handleAttribute(final XmlAttribute attribute, Project project) {
        final DeleteAttributeDialog dialog =
                new DeleteAttributeDialog(attribute);
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
                        final DeleteAttributeProcessor processor =
                                new DeleteAttributeProcessor(attribute, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Delete Attribute", null);

    }

}
