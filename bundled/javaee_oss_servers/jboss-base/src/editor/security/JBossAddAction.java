/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.security;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.model.JBossSecurityRole;
import com.fuhrer.idea.jboss.model.JBossSecurityRoleHolder;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.javaee.module.view.common.attributes.JavaeeTreeTableView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import java.awt.event.*;

class JBossAddAction extends AnAction {

    private final JBossSecurityRoleHolder holder;

    private final JavaeeTreeTableView view;

    JBossAddAction(JBossSecurityRoleHolder holder, JavaeeTreeTableView view) {
        super(JavaeeBundle.getText("GenericAction.add"), null, DomCollectionControl.ADD_ICON);
        this.holder = holder;
        this.view = view;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)), view.getTreeTableView());
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        NodeDescriptor node = view.getSelectedDescriptor();
        if (node instanceof JBossPrincipalNode) {
            node = node.getParentDescriptor();
        }
        if (node instanceof JBossSecurityRoleNode) {
            final SecurityRole source = ((JBossSecurityRoleNode) node).getElement();
            new WriteCommandAction<Object>(view.getProject()) {
                @Override
                protected void run(Result<Object> result) throws Throwable {
                    JBossSecurityRole role = JBossSecurityRoleUtil.findSecurityRole(holder, source);
                    if (role == null) {
                        role = holder.addSecurityRole();
                        role.getRoleName().setValue(source);
                    }
                    role.addPrincipalName();
                }
            }.execute();
        }
    }

    @Override
    public void update(AnActionEvent event) {
        NodeDescriptor node = view.getSelectedDescriptor();
        event.getPresentation().setEnabled((node instanceof JBossSecurityRoleNode) || (node instanceof JBossPrincipalNode));
    }
}
