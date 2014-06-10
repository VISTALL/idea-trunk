/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.geronimo.model.GeronimoEntityBean;
import com.fuhrer.idea.geronimo.model.GeronimoMessageBean;
import com.fuhrer.idea.geronimo.model.GeronimoSessionBean;
import com.intellij.javaee.model.enums.PersistenceType;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

class GeronimoEjbUtil {

    private GeronimoEjbUtil() {
    }

    @Nullable
    static GeronimoEntityBean findEntityBean(GeronimoEjbRoot root, final EntityBean source) {
        if ((root == null) || (source == null)) {
            return null;
        }
        return ContainerUtil.find(root.getEnterpriseBeans().getEntityBeans(), new Condition<GeronimoEntityBean>() {
            public boolean value(GeronimoEntityBean target) {
                return source.equals(target.getEjbName().getValue());
            }
        });
    }

    @Nullable
    static GeronimoSessionBean findSessionBean(GeronimoEjbRoot root, final SessionBean source) {
        if ((root == null) || (source == null)) {
            return null;
        }
        return ContainerUtil.find(root.getEnterpriseBeans().getSessionBeans(), new Condition<GeronimoSessionBean>() {
            public boolean value(GeronimoSessionBean target) {
                return source.equals(target.getEjbName().getValue());
            }
        });
    }

    @Nullable
    static GeronimoMessageBean findMessageBean(GeronimoEjbRoot root, final MessageDrivenBean source) {
        if ((root == null) || (source == null)) {
            return null;
        }
        return ContainerUtil.find(root.getEnterpriseBeans().getMessageBeans(), new Condition<GeronimoMessageBean>() {
            public boolean value(GeronimoMessageBean target) {
                return source.equals(target.getEjbName().getValue());
            }
        });
    }

    static boolean isCmpBean(EntityBean bean) {
        return (bean != null) && (bean.getPersistenceType().getValue() == PersistenceType.CONTAINER);
    }
}
