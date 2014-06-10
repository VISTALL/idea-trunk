/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeEnableEditor;
import com.fuhrer.idea.javaee.util.DomBooleanWrapper;
import com.fuhrer.idea.javaee.util.TripleCheckBox;
import com.fuhrer.idea.javaee.util.TripleCheckBoxControl;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossUnknownPk;
import com.fuhrer.idea.jboss.model.JBossUnknownPkHolder;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossUnknownPkEditor extends JavaeeEnableEditor<JBossUnknownPk, JBossUnknownPkHolder> {

    private JPanel panel;

    private TextPanel keyGeneratorFactory;

    private PsiClassPanel unknownPkClass;

    private TextPanel fieldName;

    private TextPanel columnName;

    private JComboBox jdbcType;

    private TextPanel sqlType;

    private JCheckBox autoIncrement;

    private TripleCheckBox readOnly;

    private TextPanel readTimeOut;

    JBossUnknownPkEditor(@NotNull JBossUnknownPkHolder holder) {
        super(JBossBundle.getText("JBossUnknownPkEditor.title"), holder);
        DomManager manager = holder.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getKeyGeneratorFactory();
            }
        }))).bind(keyGeneratorFactory);
        addComponent(DomUIFactory.getDomUIFactory().createCustomControl(PsiClass.class, new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<PsiClass>>() {
            public GenericDomValue<PsiClass> create() {
                return getElement().getUnknownPkClass();
            }
        })), false)).bind(unknownPkClass);
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
        addComponent(new BooleanControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getAutoIncrement();
            }
        })))).bind(autoIncrement);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getReadOnly();
            }
        })))).bind(readOnly);
        addComponent(DomUIFactory.createTextControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<Integer>>() {
            public GenericDomValue<Integer> create() {
                return getElement().getReadTimeOut();
            }
        })))).bind(readTimeOut);
        setContent(panel);
    }

    @Override
    protected JBossUnknownPk createElement(@NotNull JBossUnknownPkHolder parent) {
        return parent.getUnknownPk();
    }
}
