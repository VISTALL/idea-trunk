/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeResetEditor;
import com.fuhrer.idea.javaee.util.DomBooleanWrapper;
import com.fuhrer.idea.javaee.util.TripleCheckBox;
import com.fuhrer.idea.javaee.util.TripleCheckBoxControl;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.ComboControl;
import com.intellij.util.xml.ui.DomStringWrapper;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class JBossBeanDefaultsEditor extends JavaeeResetEditor<JBossCmpBean> {

    private JPanel panel;

    private TextPanel datasource;

    private JComboBox datasourceMapping;

    private TextPanel tableName;

    private TextPanel readTimeOut;

    private TripleCheckBox createTable;

    private TripleCheckBox rowLocking;

    private TripleCheckBox removeTable;

    private TripleCheckBox cleanReadAheadOnLoad;

    private TripleCheckBox pkConstraint;

    private TripleCheckBox readOnly;

    private TextPanel listCacheMax;

    private TextPanel fetchSize;

    JBossBeanDefaultsEditor(JBossCmpBean cmp) {
        super(cmp);
        DomManager manager = cmp.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getDatasource();
            }
        }))).bind(datasource);
        addComponent(new ComboControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getDatasourceMapping();
            }
        })), JBossDataFactory.getFactory(JBossDataFactory.getMappings()))).bind(datasourceMapping);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getTableName();
            }
        }))).bind(tableName);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getCreateTable();
            }
        })))).bind(createTable);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getRemoveTable();
            }
        })))).bind(removeTable);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getPkConstraint();
            }
        })))).bind(pkConstraint);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getRowLocking();
            }
        })))).bind(rowLocking);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return getElement().getCleanReadAheadOnLoad();
            }
        })))).bind(cleanReadAheadOnLoad);
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
        addComponent(DomUIFactory.createTextControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<Integer>>() {
            public GenericDomValue<Integer> create() {
                return getElement().getListCacheMax();
            }
        })))).bind(listCacheMax);
        addComponent(DomUIFactory.createTextControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<Integer>>() {
            public GenericDomValue<Integer> create() {
                return getElement().getFetchSize();
            }
        })))).bind(fetchSize);
        // todo: bind post create SQL
        setContent(panel);
    }

    @Override
    protected void reset(JBossCmpBean element) {
        element.getDatasource().setValue(null);
        element.getDatasourceMapping().setValue(null);
        element.getTableName().setValue(null);
        element.getCreateTable().setValue(null);
        element.getRemoveTable().setValue(null);
        element.getPkConstraint().setValue(null);
        element.getRowLocking().setValue(null);
        element.getCleanReadAheadOnLoad().setValue(null);
        element.getReadOnly().setValue(null);
        element.getReadTimeOut().setValue(null);
        element.getListCacheMax().setValue(null);
        element.getFetchSize().setValue(null);
    }
}
