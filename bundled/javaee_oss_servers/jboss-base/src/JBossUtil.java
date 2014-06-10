/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss;

import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.jboss.model.JBossAppRoot;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import com.intellij.javaee.facet.JavaeeFacet;
import org.jetbrains.annotations.Nullable;

public class JBossUtil {

    private JBossUtil() {
    }

    @Nullable
    public static JBossAppRoot getAppRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.APP.getRoot(facet, JBossAppRoot.class);
    }

    @Nullable
    public static JBossEjbRoot getEjbRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.EJB.getRoot(facet, JBossEjbRoot.class);
    }

    @Nullable
    public static JBossCmpRoot getCmpRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.CMP.getRoot(facet, JBossCmpRoot.class);
    }

    @Nullable
    public static JBossWebRoot getWebRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.WEB.getRoot(facet, JBossWebRoot.class);
    }
}
