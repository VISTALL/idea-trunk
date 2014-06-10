/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.security;

import com.fuhrer.idea.javaee.util.TableSourceAction;
import com.fuhrer.idea.javaee.util.TreeExpanderImpl;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossSecurityRoleHolder;
import com.intellij.ide.CommonActionsManager;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.ejb.EjbJar;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.module.view.common.attributes.JavaeeTreeTableView;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JBossSecurityRolesEditor extends JavaeeTreeTableView {

    private final JBossSecurityRoleWrapper wrapper;

    private final JBossSecurityRoleHolder holder;

    public static JBossSecurityRolesEditor get(JavaeeApplication app, JBossSecurityRoleHolder holder) {
        return new JBossSecurityRolesEditor(JBossSecurityRoleWrapper.get(app), holder);
    }

    public static JBossSecurityRolesEditor get(EjbJar ejb, JBossSecurityRoleHolder holder) {
        return new JBossSecurityRolesEditor(JBossSecurityRoleWrapper.get(ejb), holder);
    }

    public static JBossSecurityRolesEditor get(WebApp web, JBossSecurityRoleHolder holder) {
        return new JBossSecurityRolesEditor(JBossSecurityRoleWrapper.get(web), holder);
    }

    JBossSecurityRolesEditor(JBossSecurityRoleWrapper wrapper, JBossSecurityRoleHolder holder) {
        super(holder.getManager().getProject(), new JBossSecurityRolesNode(wrapper, holder));
        this.wrapper = wrapper;
        this.holder = holder;
        getComponent().setBorder(BorderFactory.createTitledBorder(JBossBundle.getText("JBossSecurityRolesEditor.title")));
        init();
    }

    @Override
    protected boolean isShowTree() {
        return !wrapper.getSecurityRoles().isEmpty();
    }

    @Override
    @NotNull
    protected String getEmptyPaneText() {
        return JBossBundle.getText("JBossSecurityRolesEditor.empty");
    }

    @Override
    protected ColumnInfo<?, ?>[] createColumnInfos() {
        return new ColumnInfo<?, ?>[]{new JBossPrincipalColumn(getProject())};
    }

    @Override
    @Nullable
    protected ActionGroup createToolbarActions() {
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.add(new JBossAddAction(holder, this));
        actions.add(new JBossRemoveAction(this));
        actions.addSeparator();
        actions.add(CommonActionsManager.getInstance().createExpandAllAction(new TreeExpanderImpl(getTree()), getTree()));
        actions.add(CommonActionsManager.getInstance().createCollapseAllAction(new TreeExpanderImpl(getTree()), getTree()));
        actions.addSeparator();
        actions.add(new TableSourceAction(getTreeTableView()));
        return actions;
    }
}
