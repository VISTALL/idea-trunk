/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.model.JBossProperty;
import com.fuhrer.idea.jboss.model.JBossValueClass;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeObjectDescriptor;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PropertyUtil;

import javax.swing.*;
import java.util.*;

class JBossValueClassNode extends JavaeeObjectDescriptor<JBossValueClass> {

    JBossValueClassNode(JavaeeNodeDescriptor<?> parent, JBossValueClass valueClass) {
        super(valueClass, parent, null);
    }

    @Override
    protected String getNewNodeText() {
        return getElement().getClassName().getStringValue();
    }

    @Override
    protected Icon getNewOpenIcon() {
        PsiClass psi = getElement().getClassName().getValue();
        return (psi != null) ? psi.getIcon(Iconable.ICON_FLAG_VISIBILITY) : JavaeeBundle.getIcon("/nodes/class.png");
    }

    @Override
    protected Icon getNewClosedIcon() {
        return getNewOpenIcon();
    }

    @Override
    public JavaeeNodeDescriptor<?>[] getChildren() {
        List<JavaeeNodeDescriptor<?>> list = new ArrayList<JavaeeNodeDescriptor<?>>();
        PsiClass psi = getElement().getClassName().getValue();
        if (psi != null) {
            Set<String> set = new HashSet<String>();
            set.addAll(Arrays.asList(PropertyUtil.getReadableProperties(psi, false)));
            set.addAll(Arrays.asList(PropertyUtil.getWritableProperties(psi, false)));
            for (JBossProperty property : getElement().getProperties()) {
                set.add(property.getPropertyName().getValue());
            }
            String[] names = set.toArray(new String[set.size()]);
            Arrays.sort(names);
            for (String name : names) {
                list.add(new JBossPropertyNode(this, new JBossPropertyDescriptor(name, psi)));
            }
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }
}
