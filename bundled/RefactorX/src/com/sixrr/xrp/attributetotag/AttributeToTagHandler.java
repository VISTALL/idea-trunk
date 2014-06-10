package com.sixrr.xrp.attributetotag;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseAttributeRefactoringHandler;
import com.sixrr.xrp.context.Context;

class AttributeToTagHandler extends BaseAttributeRefactoringHandler {

    protected String getHelpID() {
        return RefactorXHelpID.AttributeToTag;
    }

    protected String getRefactoringName() {
        return "Attribute To Tag";
    }

    protected void handleAttribute(final XmlAttribute attribute, Project project) {
        final AttributeToTagDialog dialog =
                new AttributeToTagDialog(attribute);
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
                        final AttributeToTagProcessor processor =
                                new AttributeToTagProcessor(attribute, tagName, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Replace Attribute With Tag", null);
    }
}
