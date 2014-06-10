/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.RowIcon;
import com.intellij.util.VisibilityIcons;

import javax.swing.*;

class JBossPropertyNode extends JavaeeNodeDescriptor<JBossPropertyDescriptor> {

    JBossPropertyNode(JBossValueClassNode parent, JBossPropertyDescriptor element) {
        super(parent.getProject(), parent, null, element);
    }

    @Override
    protected String getNewNodeText() {
        return getElement().getName();
    }

    @Override
    protected Icon getNewOpenIcon() {
        RowIcon icon = new RowIcon(2);
        PsiMethod getter = getElement().getGetter();
        PsiMethod setter = getElement().getSetter();
        if ((getter != null) && (setter != null)) {
            icon.setIcon(JavaeeBundle.getIcon("/nodes/propertyReadWrite.png"), 0);
            VisibilityIcons.setVisibilityIcon(getter.getModifierList(), icon);
        } else if (getter != null) {
            icon.setIcon(JavaeeBundle.getIcon("/nodes/propertyRead.png"), 0);
            VisibilityIcons.setVisibilityIcon(getter.getModifierList(), icon);
        } else if (setter != null) {
            icon.setIcon(JavaeeBundle.getIcon("/nodes/propertyWrite.png"), 0);
            VisibilityIcons.setVisibilityIcon(setter.getModifierList(), icon);
        } else {
            icon.setIcon(JavaeeBundle.getTransparentIcon("/nodes/property.png"), 0);
            VisibilityIcons.setVisibilityIcon(null, icon);
        }
        return icon;
    }

    @Override
    protected Icon getNewClosedIcon() {
        return getNewOpenIcon();
    }
}
