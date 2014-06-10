package com.sixrr.xrp.deletetag;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sixrr.xrp.base.XMLTagRefactoringAction;

public class DeleteTagAction extends XMLTagRefactoringAction {

    protected RefactoringActionHandler getHandler(DataContext context) {
        return new DeleteTagHandler();
    }
}
