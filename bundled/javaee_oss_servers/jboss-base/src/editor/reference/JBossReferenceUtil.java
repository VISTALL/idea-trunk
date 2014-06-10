/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.jboss.model.*;
import com.intellij.javaee.model.xml.*;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

class JBossReferenceUtil {

    private JBossReferenceUtil() {
    }

    @Nullable
    static JBossEjbRef findEjbRef(JBossReferenceHolder holder, final EjbRef source) {
        return (source == null) ? null : ContainerUtil.find(holder.getEjbRefs(), new Condition<JBossEjbRef>() {
            public boolean value(JBossEjbRef target) {
                return source.equals(target.getEjbRefName().getValue());
            }
        });
    }

    @Nullable
    static JBossEjbLocalRef findEjbLocalRef(JBossReferenceHolder holder, final EjbLocalRef source) {
        return (source == null) ? null : ContainerUtil.find(holder.getEjbLocalRefs(), new Condition<JBossEjbLocalRef>() {
            public boolean value(JBossEjbLocalRef target) {
                return source.equals(target.getEjbRefName().getValue());
            }
        });
    }

    @Nullable
    static JBossResourceRef findResourceRef(JBossReferenceHolder holder, final ResourceRef source) {
        return (source == null) ? null : ContainerUtil.find(holder.getResourceRefs(), new Condition<JBossResourceRef>() {
            public boolean value(JBossResourceRef target) {
                return source.equals(target.getResRefName().getValue());
            }
        });
    }

    @Nullable
    static JBossResourceEnvRef findResourceEnvRef(JBossReferenceHolder holder, final ResourceEnvRef source) {
        return (source == null) ? null : ContainerUtil.find(holder.getResourceEnvRefs(), new Condition<JBossResourceEnvRef>() {
            public boolean value(JBossResourceEnvRef target) {
                return source.equals(target.getResourceEnvRefName().getValue());
            }
        });
    }

    @Nullable
    static JBossMessageDestinationRef findMessageDestinationRef(JBossReferenceHolder holder, final MessageDestinationRef source) {
        return (source == null) ? null : ContainerUtil.find(holder.getMessageDestinationRefs(), new Condition<JBossMessageDestinationRef>() {
            public boolean value(JBossMessageDestinationRef target) {
                return source.equals(target.getMessageDestinationRefName().getValue());
            }
        });
    }
}
