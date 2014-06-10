/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model.converter;

import com.fuhrer.idea.jboss.model.JBossEntityBean;
import com.fuhrer.idea.jboss.model.JBossMessageBean;
import com.fuhrer.idea.jboss.model.JBossSessionBean;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.Nullable;

class JBossReferenceUtil {

    private JBossReferenceUtil() {
    }

    @Nullable
    static JndiEnvironmentRefsGroup getReferenceHolder(ConvertContext context) {
        JndiEnvironmentRefsGroup holder = null;
        JavaeeFacet facet = JavaeeFacetUtil.getInstance().getJavaeeFacet(context);
        if (facet == null) {
            // ignore...
        } else if (WebFacet.ID.equals(facet.getTypeId())) {
            holder = ((WebFacet) facet).getRoot();
        } else if (EjbFacet.ID.equals(facet.getTypeId())) {
            DomElement parent = context.getInvocationElement().getParent();
            if (parent != null) {
                DomElement element = parent.getParent();
                if (element instanceof JBossEntityBean) {
                    holder = ((JBossEntityBean) element).getEjbName().getValue();
                } else if (element instanceof JBossSessionBean) {
                    holder = ((JBossSessionBean) element).getEjbName().getValue();
                } else if (element instanceof JBossMessageBean) {
                    holder = ((JBossMessageBean) element).getEjbName().getValue();
                }
            }
        }
        return holder;
    }
}
