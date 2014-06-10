/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.cmpfield;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.util.IconTableCellRenderer;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossCmpField;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.*;

class JBossTableColumn extends ColumnInfo<JavaeeNodeDescriptor<?>, String> {

    private final TableCellRenderer renderer = new IconTableCellRenderer(JavaeeBundle.getIcon("/nodes/DataTables.png"));

    JBossTableColumn() {
        super(JBossBundle.getText("JBossCmpFieldsEditor.table.column"));
    }

    @Override
    public TableCellRenderer getRenderer(JavaeeNodeDescriptor<?> item) {
        return renderer;
    }

    @Override
    public String valueOf(JavaeeNodeDescriptor<?> item) {
        String value = null;
        Object element = item.getElement();
        if (element instanceof CmpField) {
            JBossCmpField field = findCmpField(item, (CmpField) element);
            if (field != null) {
                value = field.getColumnName().getValue();
            }
        }
        //if (element instanceof JBossPropertyDescriptor) {
        //    JBossProperty property = JBossValueClassesEditor.findProperty(item);
        //    if (property != null) {
        //        value = property.getColumnName().getValue();
        //    }
        //}
        return value;
    }

    @Override
    public void setValue(final JavaeeNodeDescriptor<?> item, final String value) {
        final Object element = item.getElement();
        if (element instanceof CmpField) {
            new WriteCommandAction<Object>(item.getProject()) {
                @Override
                protected void run(Result<Object> result) throws Throwable {
                    JBossCmpField field = findCmpField(item, (CmpField) element);
                    if ((field == null) && !StringUtil.isEmpty(value)) {
                        field = ((JBossCmpBean) item.getParentDescriptor().getElement()).addCmpField();
                        field.getFieldName().setValue((CmpField) element);
                    }
                    if (field != null) {
                        field.getColumnName().setValue(value);
                    }
                }
            }.execute();
        }
        //if (element instanceof JBossPropertyDescriptor) {
        //    final JBossProperty property = JBossValueClassesEditor.findProperty(item);
        //    if (property != null) {
        //        new WriteCommandAction<Object>(item.getProject()) {
        //            @Override
        //            protected void run(Result<Object> result) throws Throwable {
        //                property.getColumnName().setValue("".equals(value) ? null : value);
        //            }
        //        }.execute();
        //    }
        //}
    }

    @Override
    public boolean isCellEditable(JavaeeNodeDescriptor<?> item) {
        return item.getElement() instanceof CmpField;
    }

    @Nullable
    private JBossCmpField findCmpField(JavaeeNodeDescriptor<?> node, CmpField source) {
        JBossCmpBean bean = (JBossCmpBean) node.getParentDescriptor().getElement();
        for (JBossCmpField target : bean.getCmpFields()) {
            if (source.equals(target.getFieldName().getValue())) {
                return target;
            }
        }
        return null;
    }
}
