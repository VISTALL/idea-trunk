/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.javaee.editor.JavaeeSectionInfo;
import com.fuhrer.idea.javaee.editor.JavaeeSectionInfoEditable;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossReferenceHolder;
import com.fuhrer.idea.jboss.model.JBossResourceEnvRef;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.javaee.model.xml.ResourceEnvRef;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class JBossResourceEnvRefSection extends JBossReferenceSection<ResourceEnvRef> {

    private final JndiEnvironmentRefsGroup group;

    private final JBossReferenceHolder holder;

    JBossResourceEnvRefSection(JndiEnvironmentRefsGroup group, JBossReferenceHolder holder) {
        this.group = group;
        this.holder = holder;
    }

    public List<ResourceEnvRef> getValues() {
        return group.getResourceEnvRefs();
    }

    @Override
    JavaeeSectionInfo<ResourceEnvRef> createFirstColumn() {
        return new JavaeeSectionInfo<ResourceEnvRef>(JBossBundle.getText("JBossReferenceEditor.resource.environment")) {
            @Override
            public String valueOf(ResourceEnvRef source) {
                return source.getResourceEnvRefName().getValue();
            }
        };
    }

    @Override
    JavaeeSectionInfo<ResourceEnvRef> createSecondColumn() {
        return new JavaeeSectionInfoEditable<ResourceEnvRef>(JBossBundle.getText("JBossReferenceEditor.jndi.name"), holder) {
            @Override
            @Nullable
            public String valueOf(ResourceEnvRef source) {
                JBossResourceEnvRef target = JBossReferenceUtil.findResourceEnvRef(holder, source);
                return (target == null) ? null : target.getJndiName().getValue();
            }

            @Override
            protected void write(ResourceEnvRef item, String value) {
                JBossResourceEnvRef target = JBossReferenceUtil.findResourceEnvRef(holder, item);
                if (!StringUtil.isEmpty(value)) {
                    if (target == null) {
                        target = holder.addResourceEnvRef();
                        target.getResourceEnvRefName().setValue(item);
                    }
                    target.getJndiName().setValue(value);
                } else if (target != null) {
                    target.undefine();
                }
            }
        };
    }
}
