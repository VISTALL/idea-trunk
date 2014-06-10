package com.sixrr.xrp.renametag;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class RenameTagAction extends XMLTagRefactoringAction{

    protected RefactoringActionHandler getHandler(DataContext context) {
        return new RenameTagHandler();
    }
}
