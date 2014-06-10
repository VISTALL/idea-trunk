package com.sixrr.xrp.deletetag;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseTagRefactoringHandler;
import com.sixrr.xrp.context.Context;

class DeleteTagHandler extends BaseTagRefactoringHandler {

    protected String getHelpID() {
        return RefactorXHelpID.DeleteTag;
    }

    protected String getRefactoringName() {
        return "Delete Tag";
    }

    protected void handleTag(final XmlTag tag, Project project) {
        final DeleteTagDialog dialog =
                new DeleteTagDialog(tag);
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
                        final DeleteTagProcessor processor =
                                new DeleteTagProcessor(tag, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Delete Tag", null);

    }

}
