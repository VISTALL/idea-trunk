/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.util;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.util.OpenSourceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OpenSourceAction extends AnAction {

    private final Component component;

    public OpenSourceAction(JComponent component) {
        super(JavaeeBundle.getText("GenericAction.source"), null, JavaeeBundle.getIcon("/actions/editSource.png"));
        this.component = component;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)), component);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        OpenSourceUtil.openSourcesFrom(DataManager.getInstance().getDataContext(component), true);
    }
}
