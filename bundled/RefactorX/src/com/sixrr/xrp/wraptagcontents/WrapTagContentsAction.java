package com.sixrr.xrp.wraptagcontents;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class WrapTagContentsAction extends XMLTagRefactoringAction {

    protected RefactoringActionHandler getHandler(DataContext context) {
        return new WrapTagContentsHandler();
    }
}
