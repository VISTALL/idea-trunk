package com.sixrr.xrp.moveattributein;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseAttributeRefactoringHandler;
import com.sixrr.xrp.context.Context;

class MoveAttributeInHandler extends BaseAttributeRefactoringHandler {

    protected String getHelpID() {
        return RefactorXHelpID.MoveAttributeIn;
    }

    protected String getRefactoringName() {
        return "Move Attribute In";
    }

    protected void handleAttribute(final XmlAttribute attribute, Project project) {
        final MoveAttributeInDialog dialog =
                new MoveAttributeInDialog(attribute);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final String tagName = dialog.getTagName();
        final boolean previewUsages = dialog.isPreviewUsages();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final MoveAttributeInProcessor processor =
                                new MoveAttributeInProcessor(attribute, tagName, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Move Attribute In", null);
    }
}
