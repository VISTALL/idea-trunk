package com.sixrr.xrp.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class XSLTDialog extends DialogWrapper {
    private JTextPane textPane;
    private JPanel contentPanel;

    public XSLTDialog(Project project, String text) {
        super(project, false);
        textPane.setText(text);
        setTitle("XSLT Preview");
        init();
    }

    public Action[] createActions() {
        return new Action[0];
    }

    public String getTitle() {
        return "XSLT";
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    @NonNls
    protected String getDimensionServiceKey() {
        return "RefactorX.XSLT";

    }

}