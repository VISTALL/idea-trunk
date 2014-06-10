/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.javaee.editor.JavaeeSectionInfo;
import com.fuhrer.idea.javaee.editor.JavaeeSectionInfoEditable;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossMessageDestinationRef;
import com.fuhrer.idea.jboss.model.JBossReferenceHolder;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.javaee.model.xml.MessageDestinationRef;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class JBossMessageDestinationRefSection extends JBossReferenceSection<MessageDestinationRef> {

    private final JndiEnvironmentRefsGroup group;

    private final JBossReferenceHolder holder;

    JBossMessageDestinationRefSection(JndiEnvironmentRefsGroup group, JBossReferenceHolder holder) {
        this.group = group;
        this.holder = holder;
    }

    public List<MessageDestinationRef> getValues() {
        return group.getMessageDestinationRefs();
    }

    @Override
    JavaeeSectionInfo<MessageDestinationRef> createFirstColumn() {
        return new JavaeeSectionInfo<MessageDestinationRef>(JBossBundle.getText("JBossReferenceEditor.message.destination")) {
            @Override
            public String valueOf(MessageDestinationRef source) {
                return source.getMessageDestinationRefName().getValue();
            }
        };
    }

    @Override
    JavaeeSectionInfo<MessageDestinationRef> createSecondColumn() {
        return new JavaeeSectionInfoEditable<MessageDestinationRef>(JBossBundle.getText("JBossReferenceEditor.jndi.name"), holder) {
            @Override
            @Nullable
            public String valueOf(MessageDestinationRef source) {
                JBossMessageDestinationRef target = JBossReferenceUtil.findMessageDestinationRef(holder, source);
                return (target == null) ? null : target.getJndiName().getValue();
            }

            @Override
            protected void write(MessageDestinationRef item, String value) {
                JBossMessageDestinationRef target = JBossReferenceUtil.findMessageDestinationRef(holder, item);
                if (!StringUtil.isEmpty(value)) {
                    if (target == null) {
                        target = holder.addMessageDestinationRef();
                        target.getMessageDestinationRefName().setValue(item);
                    }
                    target.getJndiName().setValue(value);
                } else if (target != null) {
                    target.undefine();
                }
            }
        };
    }
}
