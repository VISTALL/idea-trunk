/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.editor.JBossPropertyDialog;
import com.fuhrer.idea.jboss.model.JBossProperty;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.ui.dualView.TreeTableView;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.util.Collection;

class JBossEditAction extends AnAction {

    private final TreeTableView view;

    JBossEditAction(TreeTableView view) {
        super(JavaeeBundle.getText("GenericAction.edit"), null, DomCollectionControl.EDIT_ICON);
        this.view = view;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)), view);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Collection<?> selection = view.getSelection();
        if (!selection.isEmpty()) {
            Object selected = selection.iterator().next();
            if (selected instanceof DefaultMutableTreeNode) {
                Object node = ((DefaultMutableTreeNode) selected).getUserObject();
                if (node instanceof JavaeeNodeDescriptor) {
                    JBossProperty property = JBossValueClassesEditor.findProperty((JavaeeNodeDescriptor<?>) node);
                    if (property != null) {
                        new JBossPropertyDialog(property).show();
                    }
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
        boolean enabled = false;
        Collection<?> selection = view.getSelection();
        if (!selection.isEmpty()) {
            Object selected = selection.iterator().next();
            if (selected instanceof DefaultMutableTreeNode) {
                Object node = ((DefaultMutableTreeNode) selected).getUserObject();
                if (node instanceof JavaeeNodeDescriptor) {
                    enabled = JBossValueClassesEditor.findProperty((JavaeeNodeDescriptor<?>) node) != null;
                }
            }
        }
        event.getPresentation().setEnabled(enabled);
    }
}
