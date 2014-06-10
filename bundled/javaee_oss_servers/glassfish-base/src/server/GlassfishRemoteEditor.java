/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.javaee.server.JavaeeRunSettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class GlassfishRemoteEditor extends JavaeeRunSettingsEditor<GlassfishRemoteModel> {

    private JPanel panel;

    private JTextField port;

    private JTextField username;

    private JPasswordField password;

    private JCheckBox preserve;

    @Override
    @NotNull
    protected JComponent getEditor() {
        return panel;
    }

    @Override
    protected void resetEditorFrom(GlassfishRemoteModel model) {
        port.setText(String.valueOf(model.ADMIN_PORT));
        username.setText(model.USERNAME);
        password.setText(model.PASSWORD);
        preserve.setEnabled(GlassfishUtil.isGlassfish3(model.getVersion()));
        preserve.setSelected(model.PRESERVE);
    }

    @Override
    protected void applyEditorTo(GlassfishRemoteModel model) throws ConfigurationException {
        try {
            model.ADMIN_PORT = Integer.parseInt(port.getText());
        } catch (Exception e) {
            model.ADMIN_PORT = 0;
        }
        model.USERNAME = username.getText();
        model.PASSWORD = new String(password.getPassword());
        model.PRESERVE = preserve.isSelected();
    }
}
