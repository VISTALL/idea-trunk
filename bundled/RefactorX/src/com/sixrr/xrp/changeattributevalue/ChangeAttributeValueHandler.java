package com.sixrr.xrp.changeattributevalue;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseAttributeRefactoringHandler;
import com.sixrr.xrp.context.Context;

class ChangeAttributeValueHandler extends BaseAttributeRefactoringHandler {

    protected String getHelpID() {
        return RefactorXHelpID.ChangeAttributeValue;
    }

    protected String getRefactoringName() {
        return "Change Attribute Value";
    }

    protected void handleAttribute(final XmlAttribute attribute, Project project) {
        final ChangeAttributeValueDialog dialog =
                new ChangeAttributeValueDialog(attribute);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final String newAttributeValue = dialog.getNewAttributeValue();
        final boolean previewUsages = dialog.isPreviewUsages();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final ChangeAttributeValueProcessor processor =
                                new ChangeAttributeValueProcessor(attribute, newAttributeValue, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Change Attribute Value", null);
    }
}
