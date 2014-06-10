package com.sixrr.xrp.renameattribute;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseAttributeRefactoringHandler;
import com.sixrr.xrp.context.Context;

class RenameAttributeHandler extends BaseAttributeRefactoringHandler{


    protected String getHelpID() {
        return RefactorXHelpID.RenameAttribute;
    }

    protected String getRefactoringName() {
        return "Rename attribute";
    }

    protected void handleAttribute(final XmlAttribute attribute, Project project) {
        final RenameAttributeDialog dialog =
                new RenameAttributeDialog(attribute);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final boolean previewUsages = dialog.isPreviewUsages();
        final String newAttributeName = dialog.getNewAttributeName();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final RenameAttributeProcessor processor =
                                new RenameAttributeProcessor(attribute,newAttributeName, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Rename Attribute", null);

    }

}
