/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish;

import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.glassfish.model.GlassfishCmpRoot;
import com.fuhrer.idea.glassfish.model.GlassfishEjbRoot;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.intellij.javaee.facet.JavaeeFacet;
import org.jetbrains.annotations.Nullable;

public class GlassfishUtil {

    private GlassfishUtil() {
    }

    @Nullable
    public static GlassfishAppRoot getAppRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.APP.getRoot(facet, GlassfishAppRoot.class);
    }

    @Nullable
    public static GlassfishEjbRoot getEjbRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.EJB.getRoot(facet, GlassfishEjbRoot.class);
    }

    @Nullable
    public static GlassfishCmpRoot getCmpRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.CMP.getRoot(facet, GlassfishCmpRoot.class);
    }

    @Nullable
    public static GlassfishWebRoot getWebRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.WEB.getRoot(facet, GlassfishWebRoot.class);
    }

    public static boolean isGlassfish3(String version) {
        return version.startsWith("10.") || version.startsWith("3.");
    }
}
