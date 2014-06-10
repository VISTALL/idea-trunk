/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.javaee.editor.JavaeeSectionInfo;
import com.fuhrer.idea.javaee.editor.JavaeeSectionInfoEditable;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossReferenceHolder;
import com.fuhrer.idea.jboss.model.JBossResourceRef;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.javaee.model.xml.ResourceRef;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class JBossResourceRefSection extends JBossReferenceSection<ResourceRef> {

    private final JndiEnvironmentRefsGroup group;

    private final JBossReferenceHolder holder;

    JBossResourceRefSection(JndiEnvironmentRefsGroup group, JBossReferenceHolder holder) {
        this.group = group;
        this.holder = holder;
    }

    public List<ResourceRef> getValues() {
        return group.getResourceRefs();
    }

    @Override
    JavaeeSectionInfo<ResourceRef> createFirstColumn() {
        return new JavaeeSectionInfo<ResourceRef>(JBossBundle.getText("JBossReferenceEditor.resource")) {
            @Override
            public String valueOf(ResourceRef source) {
                return source.getResRefName().getValue();
            }
        };
    }

    @Override
    JavaeeSectionInfo<ResourceRef> createSecondColumn() {
        return new JavaeeSectionInfoEditable<ResourceRef>(JBossBundle.getText("JBossReferenceEditor.jndi.name"), holder) {
            @Override
            @Nullable
            public String valueOf(ResourceRef source) {
                JBossResourceRef target = JBossReferenceUtil.findResourceRef(holder, source);
                return (target == null) ? null : target.getJndiName().getValue();
            }

            @Override
            protected void write(ResourceRef item, String value) {
                JBossResourceRef target = JBossReferenceUtil.findResourceRef(holder, item);
                if (!StringUtil.isEmpty(value)) {
                    if (target == null) {
                        target = holder.addResourceRef();
                        target.getResRefName().setValue(item);
                    }
                    target.getJndiName().setValue(value);
                } else if (target != null) {
                    target.undefine();
                }
            }
        };
    }
}
