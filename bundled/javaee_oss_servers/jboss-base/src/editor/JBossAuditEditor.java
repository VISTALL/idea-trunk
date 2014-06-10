/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossAudit;
import com.fuhrer.idea.jboss.model.JBossAuditField;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.intellij.openapi.util.Factory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class JBossAuditEditor extends JavaeeBaseEditor {

    private final JPanel panel = new JPanel(new GridLayout(2, 2));

    JBossAuditEditor(final JBossCmpBean cmp) {
        JBossAudit audit = cmp.getManager().createStableValue(new Factory<JBossAudit>() {
            public JBossAudit create() {
                return cmp.getAudit();
            }
        });
        add(new JBossAuditFieldEditor(JBossBundle.getText("JBossAuditEditor.created.by"), audit) {
            @Override
            protected JBossAuditField createElement(@NotNull JBossAudit parent) {
                return parent.getCreatedBy();
            }
        });
        add(new JBossAuditFieldEditor(JBossBundle.getText("JBossAuditEditor.created.time"), audit) {
            @Override
            protected JBossAuditField createElement(@NotNull JBossAudit parent) {
                return parent.getCreatedTime();
            }
        });
        add(new JBossAuditFieldEditor(JBossBundle.getText("JBossAuditEditor.updated.by"), audit) {
            @Override
            protected JBossAuditField createElement(@NotNull JBossAudit parent) {
                return parent.getUpdatedBy();
            }
        });
        add(new JBossAuditFieldEditor(JBossBundle.getText("JBossAuditEditor.updated.time"), audit) {
            @Override
            protected JBossAuditField createElement(@NotNull JBossAudit parent) {
                return parent.getUpdatedTime();
            }
        });
    }

    public JComponent getComponent() {
        return panel;
    }

    private void add(JBossAuditFieldEditor editor) {
        panel.add(editor.getComponent());
        addComponent(editor);
    }
}
