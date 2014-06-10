package com.sixrr.xrp.renameattribute;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class RenameAttributeAction extends XMLTagRefactoringAction{

    protected RefactoringActionHandler getHandler(DataContext context) {
        return new RenameAttributeHandler();
    }
}
