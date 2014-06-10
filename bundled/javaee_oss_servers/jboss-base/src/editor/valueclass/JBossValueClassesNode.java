/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.fuhrer.idea.jboss.model.JBossValueClass;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class JBossValueClassesNode extends JavaeeNodeDescriptor<JBossCmpRoot> {

    JBossValueClassesNode(JBossCmpRoot cmp) {
        super(cmp.getManager().getProject(), null, null, cmp);
    }

    @Override
    @Nullable
    protected String getNewNodeText() {
        return null;
    }

    @Override
    public JavaeeNodeDescriptor<?>[] getChildren() {
        List<JavaeeNodeDescriptor<?>> list = new ArrayList<JavaeeNodeDescriptor<?>>();
        for (JBossValueClass valueClass : getElement().getDependentValueClasses().getDependentValueClasses()) {
            list.add(new JBossValueClassNode(this, (JBossValueClass) valueClass.createStableCopy()));
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }
}
