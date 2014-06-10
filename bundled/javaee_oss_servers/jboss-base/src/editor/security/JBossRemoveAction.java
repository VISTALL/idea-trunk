/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.security;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.model.JBossSecurityRole;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.javaee.module.view.common.attributes.JavaeeTreeTableView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import java.awt.event.*;

class JBossRemoveAction extends AnAction {

    private final JavaeeTreeTableView view;

    JBossRemoveAction(JavaeeTreeTableView view) {
        super(JavaeeBundle.getText("GenericAction.remove"), null, DomCollectionControl.REMOVE_ICON);
        this.view = view;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)), view.getTreeTableView());
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        NodeDescriptor node = view.getSelectedDescriptor();
        if (node instanceof JBossPrincipalNode) {
            final GenericDomValue<String> principal = ((JBossPrincipalNode) node).getElement();
            new WriteCommandAction<Object>(view.getProject()) {
                @Override
                protected void run(Result<Object> result) throws Throwable {
                    JBossSecurityRole role = (JBossSecurityRole) principal.getParent();
                    principal.undefine();
                    if ((role != null) && role.getPrincipalNames().isEmpty()) {
                        role.undefine();
                    }
                }
            }.execute();
        }
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(view.getSelectedDescriptor() instanceof JBossPrincipalNode);
    }
}
