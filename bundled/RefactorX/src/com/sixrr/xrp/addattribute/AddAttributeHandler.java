package com.sixrr.xrp.addattribute;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseTagRefactoringHandler;
import com.sixrr.xrp.context.Context;

class AddAttributeHandler extends BaseTagRefactoringHandler {


    protected String getHelpID() {
        return RefactorXHelpID.AddAttribute;
    }

    protected String getRefactoringName() {
        return "Add Attribute";
    }

    protected void handleTag(final XmlTag tag, Project project) {
        final AddAttributeDialog dialog =
                new AddAttributeDialog(tag);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final String attributeName = dialog.getAttributeName();
        final String attributeValue = dialog.getAttributeValue();
        final boolean previewUsages = dialog.isPreviewUsages();
        final boolean addOnlyIfAbsent = dialog.getAddOnlyIfAbsent();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final AddAttributeProcessor processor =
                                new AddAttributeProcessor(tag, attributeName,
                                        attributeValue, addOnlyIfAbsent, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Add Attribute", null);

    }

}
