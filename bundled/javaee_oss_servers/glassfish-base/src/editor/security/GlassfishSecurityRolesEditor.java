/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.fuhrer.idea.glassfish.GlassfishBundle;
import com.fuhrer.idea.glassfish.model.GlassfishSecurityRoleHolder;
import com.fuhrer.idea.javaee.util.TableSourceAction;
import com.fuhrer.idea.javaee.util.TreeExpanderImpl;
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

public class GlassfishSecurityRolesEditor extends JavaeeTreeTableView {

    private final GlassfishSecurityRoleWrapper wrapper;

    private final GlassfishSecurityRoleHolder holder;

    public static GlassfishSecurityRolesEditor get(JavaeeApplication app, GlassfishSecurityRoleHolder holder) {
        return new GlassfishSecurityRolesEditor(GlassfishSecurityRoleWrapper.get(app), holder);
    }

    public static GlassfishSecurityRolesEditor get(EjbJar ejb, GlassfishSecurityRoleHolder holder) {
        return new GlassfishSecurityRolesEditor(GlassfishSecurityRoleWrapper.get(ejb), holder);
    }

    public static GlassfishSecurityRolesEditor get(WebApp web, GlassfishSecurityRoleHolder holder) {
        return new GlassfishSecurityRolesEditor(GlassfishSecurityRoleWrapper.get(web), holder);
    }

    GlassfishSecurityRolesEditor(GlassfishSecurityRoleWrapper wrapper, GlassfishSecurityRoleHolder holder) {
        super(holder.getManager().getProject(), new GlassfishSecurityRolesNode(wrapper, holder));
        this.wrapper = wrapper;
        this.holder = holder;
        getComponent().setBorder(BorderFactory.createTitledBorder(GlassfishBundle.getText("GlassfishSecurityRolesEditor.title")));
        init();
    }

    @Override
    protected boolean isShowTree() {
        return !wrapper.getSecurityRoles().isEmpty();
    }

    @Override
    @NotNull
    protected String getEmptyPaneText() {
        return GlassfishBundle.getText("GlassfishSecurityRolesEditor.empty");
    }

    @Override
    protected ColumnInfo<?, ?>[] createColumnInfos() {
        return new ColumnInfo<?, ?>[]{new GlassfishPrincipalColumn(getProject())};
    }

    @Override
    @Nullable
    protected ActionGroup createToolbarActions() {
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.add(new GlassfishAddAction(holder, this));
        actions.add(new GlassfishRemoveAction(this));
        actions.addSeparator();
        actions.add(CommonActionsManager.getInstance().createExpandAllAction(new TreeExpanderImpl(getTree()), getTree()));
        actions.add(CommonActionsManager.getInstance().createCollapseAllAction(new TreeExpanderImpl(getTree()), getTree()));
        actions.addSeparator();
        actions.add(new TableSourceAction(getTreeTableView()));
        return actions;
    }
}
