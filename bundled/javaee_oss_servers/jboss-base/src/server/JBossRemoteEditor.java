/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeeRunSettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossRemoteEditor extends JavaeeRunSettingsEditor<JBossRemoteModel> {

    private JPanel panel;

    private JTextField port;

    private JTextField username;

    private JPasswordField password;

    @Override
    @NotNull
    protected JComponent getEditor() {
        return panel;
    }

    @Override
    protected void resetEditorFrom(JBossRemoteModel model) {
        port.setText(String.valueOf(model.JNDI_PORT));
        username.setText(model.USERNAME);
        password.setText(model.PASSWORD);
    }

    @Override
    protected void applyEditorTo(JBossRemoteModel model) throws ConfigurationException {
        try {
            model.JNDI_PORT = Integer.parseInt(port.getText());
        } catch (Exception e) {
            model.JNDI_PORT = 0;
        }
        model.USERNAME = username.getText();
        model.PASSWORD = new String(password.getPassword());
    }
}
