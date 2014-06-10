package com.sixrr.xrp.tagtoattribute;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class TagToAttributeAction extends XMLTagRefactoringAction {


    protected RefactoringActionHandler getHandler(DataContext context) {
        return new TagToAttributeHandler();
    }
}
