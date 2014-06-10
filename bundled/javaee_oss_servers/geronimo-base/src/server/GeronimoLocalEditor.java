/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.javaee.server.JavaeeRunSettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class GeronimoLocalEditor extends JavaeeRunSettingsEditor<GeronimoLocalModel> {

    private JPanel panel;

    private JTextField username;

    private JPasswordField password;

    @Override
    @NotNull
    protected JComponent getEditor() {
        return panel;
    }

    @Override
    protected void resetEditorFrom(GeronimoLocalModel model) {
        username.setText(model.USERNAME);
        password.setText(model.PASSWORD);
    }

    @Override
    protected void applyEditorTo(GeronimoLocalModel model) throws ConfigurationException {
        model.USERNAME = username.getText();
        model.PASSWORD = new String(password.getPassword());
    }
}
