/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor;

import com.fuhrer.idea.glassfish.editor.security.GlassfishSecurityRolesEditor;
import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.openapi.ui.Splitter;

import javax.swing.*;

class GlassfishAppRootEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    GlassfishAppRootEditor(JavaeeApplication xml, GlassfishAppRoot app) {
        splitter = createSplitter(GlassfishSecurityRolesEditor.get(xml, app), new JPanel(), false);
    }

    public JComponent getComponent() {
        return splitter;
    }
}
