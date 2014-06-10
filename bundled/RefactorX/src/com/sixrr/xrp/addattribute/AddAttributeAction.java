package com.sixrr.xrp.addattribute;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class AddAttributeAction extends XMLTagRefactoringAction {

    protected RefactoringActionHandler getHandler(DataContext context) {
        return new AddAttributeHandler();
    }
}
