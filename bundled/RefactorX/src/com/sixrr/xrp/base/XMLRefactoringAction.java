package com.sixrr.xrp.base;

import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.actions.BaseRefactoringAction;

public abstract class XMLRefactoringAction extends BaseRefactoringAction {
    protected boolean isAvailableForLanguage(Language language) {
        final String id = language.getID();
        return "XML".equals(id)|| "XHTML".equals(id)|| "JSPX".equals(id);
    }

    public boolean isAvailableInEditorOnly() {
        return false;
    }

    protected boolean isAvailableForFile(PsiFile file) {
        return file instanceof XmlFile;
    }
}
