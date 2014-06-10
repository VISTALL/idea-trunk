/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.javaee.editor.JavaeeSectionInfo;
import com.fuhrer.idea.javaee.editor.JavaeeSectionInfoEditable;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossEjbRef;
import com.fuhrer.idea.jboss.model.JBossReferenceHolder;
import com.intellij.javaee.model.xml.EjbRef;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class JBossEjbRefSection extends JBossReferenceSection<EjbRef> {

    private final JndiEnvironmentRefsGroup group;

    private final JBossReferenceHolder holder;

    JBossEjbRefSection(JndiEnvironmentRefsGroup group, JBossReferenceHolder holder) {
        this.group = group;
        this.holder = holder;
    }

    public List<EjbRef> getValues() {
        return group.getEjbRefs();
    }

    @Override
    JavaeeSectionInfo<EjbRef> createFirstColumn() {
        return new JavaeeSectionInfo<EjbRef>(JBossBundle.getText("JBossReferenceEditor.ejb.remote")) {
            @Override
            public String valueOf(EjbRef source) {
                return source.getEjbRefName().getValue();
            }
        };
    }

    @Override
    JavaeeSectionInfo<EjbRef> createSecondColumn() {
        return new JavaeeSectionInfoEditable<EjbRef>(JBossBundle.getText("JBossReferenceEditor.jndi.name"), holder) {
            @Override
            @Nullable
            public String valueOf(EjbRef source) {
                JBossEjbRef target = JBossReferenceUtil.findEjbRef(holder, source);
                return (target == null) ? null : target.getJndiName().getValue();
            }

            @Override
            protected void write(EjbRef item, String value) {
                JBossEjbRef target = JBossReferenceUtil.findEjbRef(holder, item);
                if (!StringUtil.isEmpty(value)) {
                    if (target == null) {
                        target = holder.addEjbRef();
                        target.getEjbRefName().setValue(item);
                    }
                    target.getJndiName().setValue(value);
                } else if (target != null) {
                    target.undefine();
                }
            }
        };
    }
}
