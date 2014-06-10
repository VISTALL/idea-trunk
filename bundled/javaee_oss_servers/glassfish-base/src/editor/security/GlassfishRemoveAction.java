/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.fuhrer.idea.glassfish.model.GlassfishSecurityRole;
import com.fuhrer.idea.javaee.JavaeeBundle;
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

class GlassfishRemoveAction extends AnAction {

    private final JavaeeTreeTableView view;

    GlassfishRemoveAction(JavaeeTreeTableView view) {
        super(JavaeeBundle.getText("GenericAction.remove"), null, DomCollectionControl.REMOVE_ICON);
        this.view = view;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)), view.getTreeTableView());
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        NodeDescriptor node = view.getSelectedDescriptor();
        if (node instanceof GlassfishPrincipalNode) {
            final GenericDomValue<String> principal = ((GlassfishPrincipalNode) node).getElement();
            new WriteCommandAction<Object>(view.getProject()) {
                @Override
                protected void run(Result<Object> result) throws Throwable {
                    GlassfishSecurityRole role = (GlassfishSecurityRole) principal.getParent();
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
        event.getPresentation().setEnabled(view.getSelectedDescriptor() instanceof GlassfishPrincipalNode);
    }
}
