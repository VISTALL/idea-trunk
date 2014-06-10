package com.sixrr.xrp.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.ui.ScopePanel;
import com.sixrr.xrp.ui.ScopePanelListener;
import com.sixrr.xrp.ui.XSLTDialog;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;

public abstract class BaseRefactoringDialog extends DialogWrapper implements ScopePanelListener {
    private final Action refactorAction = new RefactorAction();
    private final Action previewAction = new PreviewAction();
    private final Action showSXLTAction = new ShowXSLTAction();
    private boolean previewResults = false;
    protected ScopePanel scopePanel;
    protected final ValidationDocListener docListener = new ValidationDocListener();
    private final Project project;

    public BaseRefactoringDialog(Project project, boolean b) {
        super(project, b);
        this.project = project;
        setModal(true);
    }

    public boolean isPreviewUsages() {
        return previewResults;
    }

    protected void doOKAction() {
        validateButtons();
        if (!isOKActionEnabled()) {
            return;
        }
        close(0);
    }

    protected void validateButtons() {
        final boolean isValid = isValid();
        previewAction.setEnabled(isValid);
        refactorAction.setEnabled(isValid);
        showSXLTAction.setEnabled(isValid);
    }

    protected Action[] createActions() {
        return new Action[]{refactorAction, previewAction, showSXLTAction, getCancelAction(), getHelpAction()};
    }

    public void scopeSelectionHasChanged() {
        validateButtons();
    }

    public Context getContext() {
        return scopePanel.getContext();
    }

    protected JComponent createCenterPanel() {
        return scopePanel;
    }

    protected abstract boolean isValid();

    protected class RefactorAction extends AbstractAction {
        private RefactorAction() {
            super();
            putValue(Action.NAME, "Refactor");
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            validateButtons();
            if (!isOKActionEnabled()) {
                return;
            }
            previewResults = false;
            close(0);
        }
    }

    private class PreviewAction extends AbstractAction {
        private PreviewAction() {
            super();
            putValue(Action.NAME, "Preview");
        }

        public void actionPerformed(ActionEvent e) {
            validateButtons();
            if (!isOKActionEnabled()) {
                return;
            }
            previewResults = true;
            close(0);
        }
    }

    private class ShowXSLTAction extends AbstractAction {
        private ShowXSLTAction() {
            super();
            putValue(Action.NAME, "Show XSLT...");
        }

        public void actionPerformed(ActionEvent e) {
            validateButtons();
            if (!isOKActionEnabled()) {
                return;
            }
            String xslt = calculateXSLT();
            XSLTDialog dialog = new XSLTDialog(project, xslt);
            dialog.show();
        }
    }

    @NonNls
    protected abstract String getDimensionServiceKey();

    protected abstract void doHelpAction();

    public abstract JComponent getPreferredFocusedComponent();

    @NonNls
    protected abstract String calculateXSLT();

    protected class ValidationDocListener implements DocumentListener {
        public void insertUpdate(DocumentEvent documentEvent) {
            validateButtons();
        }

        public void removeUpdate(DocumentEvent documentEvent) {
            validateButtons();
        }

        public void changedUpdate(DocumentEvent documentEvent) {
            validateButtons();
        }
    }
}
