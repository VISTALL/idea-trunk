/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeeRunSettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

class JBossLocalEditor extends JavaeeRunSettingsEditor<JBossLocalModel> {

    private JPanel panel;

    private JComboBox server;

    private JTextField username;

    private JPasswordField password;

    JBossLocalEditor() {
        server.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fireLogFilesChanged();
            }
        });
    }

    @Override
    @NotNull
    protected JComponent getEditor() {
        return panel;
    }

    @Override
    protected void resetEditorFrom(JBossLocalModel model) {
        server.removeAllItems();
        File[] files = new File(model.getHome(), "server").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    server.addItem(file.getName());
                }
            }
        }
        server.setSelectedItem(null);
        server.setSelectedItem(model.SERVER);
        username.setText(model.USERNAME);
        password.setText(model.PASSWORD);
    }

    @Override
    protected void applyEditorTo(JBossLocalModel model) throws ConfigurationException {
        model.SERVER = (String) server.getSelectedItem();
        model.USERNAME = username.getText();
        model.PASSWORD = new String(password.getPassword());
    }
}
