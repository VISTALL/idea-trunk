/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.javaee.appServerIntegrations.ApplicationServerPersistentDataEditor;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.text.MessageFormat;

class JavaeePersistentDataEditor extends ApplicationServerPersistentDataEditor<JavaeePersistentData> {

    private JPanel panel;

    private TextFieldWithBrowseButton home;

    private JLabel version;

    private JLabel error;

    protected JavaeePersistentDataEditor() {
        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setText(MessageFormat.format(label.getText(), getServerName()));
            }
        }
        home.addBrowseFolderListener(JavaeeBundle.getText("PersistentDataEditor.chooser.title", getServerName()),
                JavaeeBundle.getText("PersistentDataEditor.chooser.description", getServerName()), null,
                new FileChooserDescriptor(false, true, false, false, false, false));
        home.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent event) {
                updateVersion();
            }
        });
        error.setIcon(JavaeeBundle.getIcon("/runConfigurations/configurationWarning.png"));
        updateVersion();
    }

    @Override
    protected void resetEditorFrom(JavaeePersistentData settings) {
        home.setText(settings.HOME);
    }

    @Override
    protected void applyEditorTo(JavaeePersistentData settings) throws ConfigurationException {
        settings.HOME = home.getText();
        settings.VERSION = getVersion();
    }

    @Override
    @NotNull
    protected JComponent createEditor() {
        return panel;
    }

    @Override
    protected void disposeEditor() {
    }

    private String getServerName() {
        return JavaeeIntegration.getInstance().getName();
    }

    private void updateVersion() {
        version.setText(JavaeeBundle.getText("PersistentDataEditor.unknown"));
        try {
            version.setText(getVersion());
            JavaeeIntegration.getInstance().checkValidServerHome(home.getText(), version.getText());
            error.setVisible(false);
        } catch (Exception e) {
            error.setText(JavaeeBundle.getText("PersistentDataEditor.warning", getServerName()));
            error.setVisible(true);
        }
    }

    private String getVersion() throws ConfigurationException {
        try {
            return JavaeeIntegration.getInstance().getServerVersion(home.getText());
        } catch (Exception e) {
            throw new ConfigurationException(JavaeeBundle.getText("PersistentDataEditor.invalid", getServerName(), home.getText()));
        }
    }
}
