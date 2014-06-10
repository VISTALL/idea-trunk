/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.jboss.model.*;
import com.intellij.javaee.model.enums.PersistenceType;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

class JBossEjbUtil {

    private JBossEjbUtil() {
    }

    @Nullable
    static JBossEntityBean findEntityBean(JBossEjbRoot root, final EntityBean source) {
        if ((root == null) || (source == null)) {
            return null;
        }
        return ContainerUtil.find(root.getEnterpriseBeans().getEntityBeans(), new Condition<JBossEntityBean>() {
            public boolean value(JBossEntityBean target) {
                return source.equals(target.getEjbName().getValue());
            }
        });
    }

    @Nullable
    static JBossSessionBean findSessionBean(JBossEjbRoot root, final SessionBean source) {
        if ((root == null) || (source == null)) {
            return null;
        }
        return ContainerUtil.find(root.getEnterpriseBeans().getSessionBeans(), new Condition<JBossSessionBean>() {
            public boolean value(JBossSessionBean target) {
                return source.equals(target.getEjbName().getValue());
            }
        });
    }

    @Nullable
    static JBossMessageBean findMessageBean(JBossEjbRoot root, final MessageDrivenBean source) {
        if ((root == null) || (source == null)) {
            return null;
        }
        return ContainerUtil.find(root.getEnterpriseBeans().getMessageBeans(), new Condition<JBossMessageBean>() {
            public boolean value(JBossMessageBean target) {
                return source.equals(target.getEjbName().getValue());
            }
        });
    }

    @Nullable
    static JBossCmpBean findCmpBean(JBossCmpRoot root, final EntityBean source) {
        if ((root == null) || (source == null)) {
            return null;
        }
        return ContainerUtil.find(root.getEnterpriseBeans().getCmpBeans(), new Condition<JBossCmpBean>() {
            public boolean value(JBossCmpBean target) {
                return source.equals(target.getEjbName().getValue());
            }
        });
    }

    static boolean isCmpBean(EntityBean bean) {
        return (bean != null) && (bean.getPersistenceType().getValue() == PersistenceType.CONTAINER);
    }
}
