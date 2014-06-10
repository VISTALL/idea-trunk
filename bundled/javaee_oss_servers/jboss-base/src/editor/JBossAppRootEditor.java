/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.editor.security.JBossSecurityRolesEditor;
import com.fuhrer.idea.jboss.model.JBossAppRoot;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.openapi.ui.Splitter;

import javax.swing.*;

class JBossAppRootEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    JBossAppRootEditor(JavaeeApplication xml, JBossAppRoot app) {
        Splitter left = createSplitter(new JBossAppSettingsEditor(app), JBossSecurityRolesEditor.get(xml, app), true);
        splitter = createSplitter(left, new JPanel(), false);
    }

    public JComponent getComponent() {
        return splitter;
    }
}
