/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.editor.security.JBossSecurityRolesEditor;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.intellij.javaee.model.xml.ejb.EjbJar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class JBossEjbRootEditor extends JavaeeBaseEditor {

    private final JComponent component;

    JBossEjbRootEditor(@Nullable EjbJar xml, @NotNull JBossEjbRoot ejb) {
        JBossEjbSettingsEditor editor = new JBossEjbSettingsEditor(ejb);
        if (xml == null) {
            component = editor.getComponent();
            addComponent(editor);
        } else {
            component = createSplitter(editor, JBossSecurityRolesEditor.get(xml, ejb.getAssemblyDescriptor()), false);
        }
    }

    public JComponent getComponent() {
        return component;
    }
}
