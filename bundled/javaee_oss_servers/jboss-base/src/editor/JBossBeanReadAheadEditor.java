/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeEnableEditor;
import com.fuhrer.idea.javaee.util.ButtonGroupControl;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossBeanReadAhead;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.ComboControl;
import com.intellij.util.xml.ui.DomStringWrapper;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossBeanReadAheadEditor extends JavaeeEnableEditor<JBossBeanReadAhead, JBossCmpBean> {

    private JPanel panel;

    private JRadioButton none;

    private JRadioButton onLoad;

    private JRadioButton onFind;

    private JComboBox loadGroup;

    private TextPanel pageSize;

    JBossBeanReadAheadEditor(@NotNull JBossCmpBean cmp) {
        super(JBossBundle.getText("JBossBeanReadAheadEditor.title"), cmp);
        DomManager manager = cmp.getManager();
        addComponent(new ButtonGroupControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getStrategy();
            }
        })), none, onLoad, onFind)).bind(null);
        addComponent(new ComboControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<JBossLoadGroup>>() {
            public GenericDomValue<JBossLoadGroup> create() {
                return getElement().getEagerLoadGroup();
            }
        })), JBossDataFactory.getFactory(JBossDataFactory.getLoadGroups(cmp)))).bind(loadGroup);
        addComponent(DomUIFactory.createTextControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<Integer>>() {
            public GenericDomValue<Integer> create() {
                return getElement().getPageSize();
            }
        })))).bind(pageSize);
        setContent(panel);
    }

    @Override
    protected JBossBeanReadAhead createElement(@NotNull JBossCmpBean parent) {
        return parent.getReadAhead();
    }
}
