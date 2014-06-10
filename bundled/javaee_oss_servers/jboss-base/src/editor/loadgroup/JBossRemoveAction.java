/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.ui.dualView.TreeTableView;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.util.Collection;

class JBossRemoveAction extends AnAction {

    private final JBossCmpBean bean;

    private final TreeTableView view;

    JBossRemoveAction(JBossCmpBean bean, TreeTableView view) {
        super(JavaeeBundle.getText("GenericAction.remove"), null, DomCollectionControl.REMOVE_ICON);
        this.bean = bean;
        this.view = view;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)), view);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Collection<?> selection = view.getSelection();
        if (!selection.isEmpty()) {
            Object selected = selection.iterator().next();
            if (selected instanceof DefaultMutableTreeNode) {
                Object node = ((DefaultMutableTreeNode) selected).getUserObject();
                if (node instanceof JavaeeNodeDescriptor) {
                    Object element = ((NodeDescriptor<?>) node).getElement();
                    if (element instanceof CmpField) {
                        element = ((NodeDescriptor<?>) node).getParentDescriptor().getElement();
                    }
                    if (element instanceof JBossLoadGroup) {
                        final JBossLoadGroup group = (JBossLoadGroup) element;
                        new WriteCommandAction<Object>(bean.getManager().getProject()) {
                            @Override
                            protected void run(Result<Object> result) throws Throwable {
                                group.undefine();
                                if (bean.getLoadGroups().getLoadGroups().isEmpty()) {
                                    bean.getLoadGroups().undefine();
                                }
                            }
                        }.execute();
                    }
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(!view.getSelection().isEmpty());
    }
}
