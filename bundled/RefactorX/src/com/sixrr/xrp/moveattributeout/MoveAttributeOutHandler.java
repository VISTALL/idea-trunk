package com.sixrr.xrp.moveattributeout;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseAttributeRefactoringHandler;
import com.sixrr.xrp.context.Context;

class MoveAttributeOutHandler extends BaseAttributeRefactoringHandler {


    protected String getHelpID() {
        return RefactorXHelpID.MoveAttributeOut;
    }

    protected String getRefactoringName() {
        return "Move Attribute Out";
    }

    protected void handleAttribute(final XmlAttribute attribute, Project project) {
        final MoveAttributeOutDialog dialog =
                new MoveAttributeOutDialog(attribute);
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
                        final MoveAttributeOutProcessor processor =
                                new MoveAttributeOutProcessor(attribute, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Move Attribute Out", null);

    }

}
