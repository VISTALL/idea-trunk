/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.javaee.util.BooleanRenderer;
import com.fuhrer.idea.javaee.util.TreeExpanderImpl;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.fuhrer.idea.jboss.model.JBossProperty;
import com.fuhrer.idea.jboss.model.JBossValueClass;
import com.intellij.ide.CommonActionsManager;
import com.intellij.javaee.module.view.common.attributes.JavaeeTreeTableView;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JBossValueClassesEditor extends JavaeeTreeTableView {

    private final JBossCmpRoot cmp;

    public JBossValueClassesEditor(JBossCmpRoot cmp) {
        super(cmp.getManager().getProject(), new JBossValueClassesNode(cmp));
        this.cmp = cmp;
        getComponent().setBorder(BorderFactory.createTitledBorder(JBossBundle.getText("JBossValueClassesEditor.title")));
        getTreeTableView().setShowVerticalLines(true);
        getTreeTableView().setIntercellSpacing(new Dimension(1, 0));
        getTreeTableView().setDefaultRenderer(Boolean.class, new BooleanRenderer(getTreeTableView()));
        init();
    }

    @Override
    protected boolean isShowTree() {
        return !cmp.getDependentValueClasses().getDependentValueClasses().isEmpty();
    }

    @Override
    @NotNull
    protected String getEmptyPaneText() {
        return JBossBundle.getText("JBossValueClassesEditor.empty");
    }

    @Override
    protected ColumnInfo<?, ?>[] createColumnInfos() {
        return new ColumnInfo<?, ?>[]{new TreeColumnInfo(""), new JBossPersistentColumn(), new JBossTableColumn()};
    }

    @Override
    @Nullable
    protected ActionGroup createToolbarActions() {
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.add(new JBossAddAction(cmp, getTreeTableView()));
        actions.add(new JBossRemoveAction(cmp, getTreeTableView()));
        actions.add(new JBossEditAction(getTreeTableView()));
        actions.addSeparator();
        actions.add(CommonActionsManager.getInstance().createExpandAllAction(new TreeExpanderImpl(getTree()), getTree()));
        actions.add(CommonActionsManager.getInstance().createCollapseAllAction(new TreeExpanderImpl(getTree()), getTree()));
        return actions;
    }

    @Nullable
    static JBossProperty findProperty(JavaeeNodeDescriptor<?> node) {
        Object element = node.getElement();
        if (element instanceof JBossPropertyDescriptor) {
            String name = ((JBossPropertyDescriptor) element).getName();
            JBossValueClass valueClass = (JBossValueClass) node.getParentDescriptor().getElement();
            for (JBossProperty property : valueClass.getProperties()) {
                if (name.equals(property.getPropertyName().getValue())) {
                    return property;
                }
            }
        }
        return null;
    }
}
