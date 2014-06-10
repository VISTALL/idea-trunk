/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.util.GenericColumnInfo;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import java.awt.event.*;

class JBossVirtualHostsEditor extends DomCollectionControl<GenericDomValue<String>> {

    JBossVirtualHostsEditor(JBossWebRoot web) {
        super(web, "virtual-host");
        getComponent().setBorder(BorderFactory.createTitledBorder(JBossBundle.getText("JBossVirtualHostsEditor.title")));
    }

    @Override
    protected String getEmptyPaneText() {
        return JBossBundle.getText("JBossVirtualHostsEditor.empty");
    }

    @Override
    protected ColumnInfo<?, ?>[] createColumnInfos(DomElement parent) {
        return new ColumnInfo[]{new GenericColumnInfo<String>("", String.class, JavaeeBundle.getIcon("/webreferences/server.png"))};
    }

    @Override
    protected AnAction[] createAdditionActions() {
        AnAction action = new ControlAddAction() {
            @Override
            protected void afterAddition(JTable table, int index) {
                table.editCellAt(index, 0);
            }
        };
        action.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)), getComponent());
        return new AnAction[]{action};
    }
}
