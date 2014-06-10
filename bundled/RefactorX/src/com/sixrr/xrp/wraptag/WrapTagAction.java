package com.sixrr.xrp.wraptag;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class WrapTagAction extends XMLTagRefactoringAction {

    protected RefactoringActionHandler getHandler(DataContext context) {
        return new WrapTagHandler();
    }
}
