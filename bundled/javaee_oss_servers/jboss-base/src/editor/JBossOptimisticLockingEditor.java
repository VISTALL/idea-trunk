/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeEnableEditor;
import com.fuhrer.idea.javaee.util.DomBooleanWrapper;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.fuhrer.idea.jboss.model.JBossOptimisticLocking;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossOptimisticLockingEditor extends JavaeeEnableEditor<JBossOptimisticLocking, JBossCmpBean> {

    private JPanel panel;

    private JComboBox groupName;

    private JCheckBox modifiedStrategy;

    private JCheckBox versionColumn;

    private JCheckBox readStrategy;

    private JCheckBox timestampColumn;

    private TextPanel keyFactory;

    private PsiClassPanel fieldType;

    private TextPanel fieldName;

    private TextPanel columnName;

    private JComboBox jdbcType;

    private TextPanel sqlType;

    JBossOptimisticLockingEditor(@NotNull JBossCmpBean cmp) {
        super(JBossBundle.getText("JBossOptimisticLockingEditor.title"), cmp);
        DomManager manager = cmp.getManager();
        addComponent(new ComboControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<JBossLoadGroup>>() {
            public GenericDomValue<JBossLoadGroup> create() {
                return getElement().getGroupName();
            }
        })), JBossDataFactory.getFactory(JBossDataFactory.getLoadGroups(cmp)))).bind(groupName);
        addComponent(new BooleanControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getModifiedStrategy();
            }
        })))).bind(modifiedStrategy);
        addComponent(new BooleanControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getReadStrategy();
            }
        })))).bind(readStrategy);
        addComponent(new BooleanControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getVersionColumn();
            }
        })))).bind(versionColumn);
        addComponent(new BooleanControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getTimestampColumn();
            }
        })))).bind(timestampColumn);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getKeyGeneratorFactory();
            }
        }))).bind(keyFactory);
        addComponent(DomUIFactory.getDomUIFactory().createCustomControl(PsiClass.class, new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<PsiClass>>() {
            public GenericDomValue<PsiClass> create() {
                return getElement().getFieldType();
            }
        })), false)).bind(fieldType);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getFieldName();
            }
        }))).bind(fieldName);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getColumnName();
            }
        }))).bind(columnName);
        addComponent(new ComboControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getJdbcType();
            }
        })), JBossDataFactory.getFactory(JBossDataFactory.getTypes()))).bind(jdbcType);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getSqlType();
            }
        }))).bind(sqlType);
        setContent(panel);
    }

    @Override
    protected JBossOptimisticLocking createElement(@NotNull JBossCmpBean parent) {
        return parent.getOptimisticLocking();
    }
}
