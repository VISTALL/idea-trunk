/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.javaee.model.common.ejb.EntityBean;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeObjectDescriptor;

import javax.swing.*;

class JBossCmpFieldNode extends JavaeeObjectDescriptor<CmpField> {

    JBossCmpFieldNode(JavaeeNodeDescriptor<?> parent, CmpField field) {
        super(field, parent, null);
    }

    @Override
    protected String getNewNodeText() {
        return getElement().getFieldName().getValue();
    }

    @Override
    protected Icon getNewOpenIcon() {
        boolean pk = false;
        String name = getElement().getFieldName().getValue();
        if (name != null) {
            EntityBean bean = getElement().getEntityBean();
            if (bean != null) {
                com.intellij.javaee.model.common.ejb.CmpField field = bean.getPrimkeyField().getValue();
                if (field != null) {
                    pk = name.equals(field.getFieldName().getValue());
                }
            }
        }
        return pk ? JavaeeBundle.getIcon("/nodes/fieldPK.png") : JavaeeBundle.getIcon("/nodes/ejbCmpField.png");
    }

    @Override
    protected Icon getNewClosedIcon() {
        return getNewOpenIcon();
    }
}
