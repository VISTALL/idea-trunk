package com.sixrr.xrp.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Intention implements IntentionAction {
    private final PsiElementPredicate predicate;

    /**
     * @noinspection AbstractMethodCallInConstructor,OverridableMethodCallInConstructor
     */
    protected Intention() {
        super();
        predicate = getElementPredicate();
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile file)
            throws IncorrectOperationException {
        if (isFileReadOnly(project, file)) {
            return;
        }
        final PsiElement element = findMatchingElement(file, editor);
        if (element == null) {
            return;
        }
        processIntention(element);
    }

    protected abstract void processIntention(@NotNull PsiElement element)
            throws IncorrectOperationException;

    @NotNull protected abstract PsiElementPredicate getElementPredicate();

    @Nullable
    private PsiElement findMatchingElement(PsiFile file,
                                   Editor editor) {
        final CaretModel caretModel = editor.getCaretModel();
        final int position = caretModel.getOffset();
        PsiElement element = file.findElementAt(position);
        while (element != null) {
            if (predicate.satisfiedBy(element)) {
                return element;
            } else {
                element = element.getParent();
            }
        }
        return null;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return findMatchingElement(file, editor) != null;
    }

    public boolean startInWriteAction() {
        return true;
    }

    private static boolean isFileReadOnly(Project project, PsiFile file) {
        final VirtualFile virtualFile = file.getVirtualFile();
        final ReadonlyStatusHandler statusHandler = ReadonlyStatusHandler.getInstance(project);
        final ReadonlyStatusHandler.OperationStatus status = statusHandler.ensureFilesWritable(new VirtualFile[]{virtualFile});
        return status.hasReadonlyFiles();
    }
}
