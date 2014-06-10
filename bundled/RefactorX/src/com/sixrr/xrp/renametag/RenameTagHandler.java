package com.sixrr.xrp.renametag;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseTagRefactoringHandler;
import com.sixrr.xrp.context.Context;

class RenameTagHandler extends BaseTagRefactoringHandler{

    protected String getHelpID(){
        return RefactorXHelpID.RenameTag;
    }

    protected String getRefactoringName(){
        return "Rename Tag";
    }

    protected void handleTag(final XmlTag tag, Project project){
        final RenameTagDialog dialog =
                new RenameTagDialog(tag);
        dialog.show();
        if(!dialog.isOK()){
            return;
        }
        final Context context = dialog.getContext();
        final boolean previewUsages = dialog.isPreviewUsages();
        final String newTagName = dialog.getNewTagName();
        final CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable(){
            public void run(){
                final Runnable action = new Runnable(){
                    public void run(){
                        final RenameTagProcessor processor =
                                new RenameTagProcessor(tag, newTagName, context, previewUsages);
                        processor.run();
                    }
                };
                final Application application = ApplicationManager.getApplication();
                application.runWriteAction(action);
            }
        }, "Rename Tag", null);
    }
}
