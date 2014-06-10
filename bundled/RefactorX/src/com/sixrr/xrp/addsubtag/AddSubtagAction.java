package com.sixrr.xrp.addsubtag;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class AddSubtagAction extends XMLTagRefactoringAction {

    protected RefactoringActionHandler getHandler(DataContext context) {
        return new AddSubtagHandler();
    }
}
