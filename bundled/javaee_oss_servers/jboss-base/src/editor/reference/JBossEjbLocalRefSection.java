/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.javaee.editor.JavaeeSectionInfo;
import com.fuhrer.idea.javaee.editor.JavaeeSectionInfoEditable;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossEjbLocalRef;
import com.fuhrer.idea.jboss.model.JBossReferenceHolder;
import com.intellij.javaee.model.xml.EjbLocalRef;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class JBossEjbLocalRefSection extends JBossReferenceSection<EjbLocalRef> {

    private final JndiEnvironmentRefsGroup group;

    private final JBossReferenceHolder holder;

    JBossEjbLocalRefSection(JndiEnvironmentRefsGroup group, JBossReferenceHolder holder) {
        this.group = group;
        this.holder = holder;
    }

    public List<EjbLocalRef> getValues() {
        return group.getEjbLocalRefs();
    }

    @Override
    JavaeeSectionInfo<EjbLocalRef> createFirstColumn() {
        return new JavaeeSectionInfo<EjbLocalRef>(JBossBundle.getText("JBossReferenceEditor.ejb.local")) {
            @Override
            public String valueOf(EjbLocalRef source) {
                return source.getEjbRefName().getValue();
            }
        };
    }

    @Override
    JavaeeSectionInfo<EjbLocalRef> createSecondColumn() {
        return new JavaeeSectionInfoEditable<EjbLocalRef>(JBossBundle.getText("JBossReferenceEditor.jndi.name"), holder) {
            @Override
            @Nullable
            public String valueOf(EjbLocalRef source) {
                JBossEjbLocalRef target = JBossReferenceUtil.findEjbLocalRef(holder, source);
                return (target == null) ? null : target.getLocalJndiName().getValue();
            }

            @Override
            protected void write(EjbLocalRef item, String value) {
                JBossEjbLocalRef target = JBossReferenceUtil.findEjbLocalRef(holder, item);
                if (!StringUtil.isEmpty(value)) {
                    if (target == null) {
                        target = holder.addEjbLocalRef();
                        target.getEjbRefName().setValue(item);
                    }
                    target.getLocalJndiName().setValue(value);
                } else if (target != null) {
                    target.undefine();
                }
            }
        };
    }
}
