package com.sixrr.xrp.addsubtag;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseTagRefactoringHandler;
import com.sixrr.xrp.context.Context;

class AddSubtagHandler extends BaseTagRefactoringHandler {


    protected String getHelpID() {
        return RefactorXHelpID.AddSubtag;
    }

    protected String getRefactoringName() {
        return "Add Subtag";
    }

    protected void handleTag(final XmlTag tag, Project project) {
        final AddSubtagDialog dialog =
                new AddSubtagDialog(tag);
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        final Context context = dialog.getContext();
        final String subtagName = dialog.getSubtagName();
        final boolean previewUsages = dialog.isPreviewUsages();
        final boolean addOnlyIfAbsent = dialog.getAddOnlyIfAbsent();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            public void run() {
                final Runnable action = new Runnable() {
                    public void run() {
                        final AddSubtagProcessor processor =
                                new AddSubtagProcessor(tag, subtagName,
                                         addOnlyIfAbsent, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Add Subtag", null);

    }

}
