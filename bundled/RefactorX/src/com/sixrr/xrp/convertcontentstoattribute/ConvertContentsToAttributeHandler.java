package com.sixrr.xrp.convertcontentstoattribute;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseTagRefactoringHandler;
import com.sixrr.xrp.context.Context;

class ConvertContentsToAttributeHandler extends BaseTagRefactoringHandler {

    protected String getHelpID() {
        return RefactorXHelpID.ConvertContentsToAttribute;
    }

    protected String getRefactoringName() {
        return "Convert Contents to Attribute";
    }

    protected void handleTag(final XmlTag tag, Project project) {
        final ConvertContentsToAttributeDialog dialog =
                new ConvertContentsToAttributeDialog(tag);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final String attributeName = dialog.getAttributeName();
        final boolean previewUsages = dialog.isPreviewUsages();
        final boolean trim = dialog.getTrimContents();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final ConvertContentsToAttributeProcessor processor =
                                new ConvertContentsToAttributeProcessor(tag, attributeName, context, trim, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Convert Contents to Attribute", null);

    }

}
