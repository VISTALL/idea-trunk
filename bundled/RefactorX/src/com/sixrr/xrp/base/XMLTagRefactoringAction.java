package com.sixrr.xrp.base;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;

public abstract class XMLTagRefactoringAction extends XMLRefactoringAction {
    public boolean isEnabledOnElements(PsiElement[] elements) {
        if (elements.length != 1) {
            return false;
        }
        return elements[0] instanceof XmlTag;
    }
}
