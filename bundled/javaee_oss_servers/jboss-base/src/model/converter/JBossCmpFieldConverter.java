/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model.converter;

import com.fuhrer.idea.javaee.converter.CmpFieldConverter;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.Nullable;

public class JBossCmpFieldConverter extends CmpFieldConverter {

    @Override
    @Nullable
    protected EntityBean getEntityBean(ConvertContext context) {
        JBossCmpBean cmp = context.getInvocationElement().getParentOfType(JBossCmpBean.class, false);
        return (cmp != null) ? cmp.getEjbName().getValue() : null;
    }
}
