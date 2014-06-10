/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor;

import com.fuhrer.idea.glassfish.editor.security.GlassfishSecurityRolesEditor;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.openapi.ui.Splitter;

import javax.swing.*;

class GlassfishWebRootEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    GlassfishWebRootEditor(WebApp xml, GlassfishWebRoot web) {
        splitter = createSplitter(new GlassfishWebSettingsEditor(web), GlassfishSecurityRolesEditor.get(xml, web), false);
    }

    public JComponent getComponent() {
        return splitter;
    }
}
