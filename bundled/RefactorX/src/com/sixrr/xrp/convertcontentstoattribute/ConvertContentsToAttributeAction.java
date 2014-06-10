package com.sixrr.xrp.convertcontentstoattribute;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class ConvertContentsToAttributeAction extends XMLTagRefactoringAction {


    protected RefactoringActionHandler getHandler(DataContext context) {
        return new ConvertContentsToAttributeHandler();
    }
}
