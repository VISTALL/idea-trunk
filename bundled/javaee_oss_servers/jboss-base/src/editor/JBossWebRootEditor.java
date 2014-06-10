/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.editor.reference.JBossReferenceEditor;
import com.fuhrer.idea.jboss.editor.security.JBossSecurityRolesEditor;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.openapi.ui.Splitter;

import javax.swing.*;

class JBossWebRootEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    JBossWebRootEditor(WebApp xml, JBossWebRoot web) {
        JBossWebSettingsEditor up = new JBossWebSettingsEditor(web);
        Splitter left = createSplitter(up, new JBossVirtualHostsEditor(web), true);
        Splitter right = createSplitter(new JBossReferenceEditor(xml, web), JBossSecurityRolesEditor.get(xml, web), true);
        splitter = createSplitter(left, right, false);
    }

    public JComponent getComponent() {
        return splitter;
    }
}
