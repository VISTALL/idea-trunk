/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.javaee.util.BooleanRenderer;
import com.fuhrer.idea.javaee.util.TreeExpanderImpl;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.intellij.ide.CommonActionsManager;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.module.view.common.attributes.JavaeeTreeTableView;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class JBossLoadGroupsEditor extends JavaeeTreeTableView {

    private final JBossCmpBean cmp;

    public JBossLoadGroupsEditor(EntityBean bean, JBossCmpBean cmp) {
        super(cmp.getManager().getProject(), new JBossLoadGroupsNode(bean, cmp));
        this.cmp = cmp;
        getTreeTableView().setShowVerticalLines(true);
        getTreeTableView().setIntercellSpacing(new Dimension(1, 0));
        getTreeTableView().setDefaultRenderer(Boolean.class, new BooleanRenderer(getTreeTableView()));
        init();
    }

    @Override
    protected boolean isShowTree() {
        return !cmp.getLoadGroups().getLoadGroups().isEmpty();
    }

    @Override
    @NotNull
    protected String getEmptyPaneText() {
        return JBossBundle.getText("JBossLoadGroupsEditor.empty");
    }

    @Override
    protected ColumnInfo<?, ?>[] createColumnInfos() {
        return new ColumnInfo<?, ?>[]{
                new JBossLoadGroupColumn(getProject()), new JBossEagerColumn(cmp), new JBossLazyColumn(cmp), new JBossContainsColumn()
        };
    }

    @Override
    @Nullable
    protected ActionGroup createToolbarActions() {
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.add(new JBossAddAction(cmp, getComponent()));
        //actions.add(new JBossEditAction(getTreeTableView()));
        actions.add(new JBossRemoveAction(cmp, getTreeTableView()));
        actions.addSeparator();
        actions.add(CommonActionsManager.getInstance().createExpandAllAction(new TreeExpanderImpl(getTree()), getTree()));
        actions.add(CommonActionsManager.getInstance().createCollapseAllAction(new TreeExpanderImpl(getTree()), getTree()));
        return actions;
    }
}
