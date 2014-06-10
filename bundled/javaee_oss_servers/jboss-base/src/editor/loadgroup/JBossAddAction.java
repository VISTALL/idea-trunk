/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import java.awt.event.*;

class JBossAddAction extends AnAction {

    private final JBossCmpBean bean;

    JBossAddAction(JBossCmpBean bean, JComponent component) {
        super(JavaeeBundle.getText("GenericAction.add"), null, DomCollectionControl.ADD_ICON);
        this.bean = bean;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)), component);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        new WriteCommandAction<Object>(bean.getManager().getProject()) {
            @Override
            protected void run(Result<Object> result) throws Throwable {
                bean.getLoadGroups().addLoadGroup();
            }
        }.execute();
    }
}
