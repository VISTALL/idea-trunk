/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeEnableEditor;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.fuhrer.idea.jboss.model.JBossDbDefaults;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossCmpDefaultsEditor extends JavaeeEnableEditor<JBossDbDefaults, JBossCmpRoot> {

    JBossCmpDefaultsEditor(@NotNull JBossCmpRoot root) {
        super(JBossBundle.getText("JBossCmpDefaultsEditor.title"), root);
        getComponent().setBorder(BorderFactory.createTitledBorder(JBossBundle.getText("JBossCmpDefaultsEditor.title")));
        JTabbedPane pane = new JTabbedPane();
        addContent(pane, new JBossDbDefaultsEditor(getElement()), JBossBundle.getText("JBossDbDefaultsEditor.title"));
        addContent(pane, new JBossUnknownPkEditor(getElement()), JBossBundle.getText("JBossUnknownPkEditor.title"));
        addContent(pane, new JBossDbReadAheadEditor(getElement()), JBossBundle.getText("JBossDbReadAheadEditor.title"));
        setContent(pane);
    }

    @Override
    protected JBossDbDefaults createElement(@NotNull JBossCmpRoot parent) {
        return parent.getDefaults();
    }
}
