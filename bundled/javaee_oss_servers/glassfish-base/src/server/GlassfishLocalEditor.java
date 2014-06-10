/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.javaee.server.JavaeeRunSettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

class GlassfishLocalEditor extends JavaeeRunSettingsEditor<GlassfishLocalModel> {

    private JPanel panel;

    private JComboBox domain;

    private JTextField username;

    private JPasswordField password;

    private JCheckBox preserve;

    GlassfishLocalEditor() {
        domain.addActionListener(new ActionListener() {
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
    protected void resetEditorFrom(GlassfishLocalModel model) {
        domain.removeAllItems();
        File[] files;
        if (GlassfishUtil.isGlassfish3(model.getVersion())) {
            files = new File(model.getHome(), "glassfish/domains").listFiles();
        } else {
            files = new File(model.getHome(), "domains").listFiles();
        }
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    domain.addItem(file.getName());
                }
            }
        }
        domain.setSelectedItem(null);
        domain.setSelectedItem(model.DOMAIN);
        username.setText(model.USERNAME);
        password.setText(model.PASSWORD);
        preserve.setEnabled(GlassfishUtil.isGlassfish3(model.getVersion()));
        preserve.setSelected(model.PRESERVE);
    }

    @Override
    protected void applyEditorTo(GlassfishLocalModel model) throws ConfigurationException {
        model.DOMAIN = (String) domain.getSelectedItem();
        model.USERNAME = username.getText();
        model.PASSWORD = new String(password.getPassword());
        model.PRESERVE = preserve.isSelected();
    }
}
