package com.sixrr.xrp;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

public class RefactorXMenuGroup extends DefaultActionGroup{
    public RefactorXMenuGroup() {
        super("_XML Refactorings", true);
    }

    public void update(AnActionEvent e){
        final PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        final Presentation presentation = e.getPresentation();
        final boolean isXML = file instanceof XmlFile;
        presentation.setVisible(isXML);
        presentation.setEnabled(isXML);
    }
}
