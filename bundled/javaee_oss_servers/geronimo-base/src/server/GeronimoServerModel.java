/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.geronimo.model.GeronimoEnvironment;
import com.fuhrer.idea.geronimo.model.GeronimoModuleId;
import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeServerModel;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Set;

abstract class GeronimoServerModel extends JavaeeServerModel {

    @Override
    @NonNls
    protected String getDefaultUsername() {
        return "system";
    }

    @Override
    @NonNls
    protected String getDefaultPassword() {
        return "manager";
    }

    @Override
    @NonNls
    protected String getServerName() {
        if (getVersion().startsWith("1.0")) {
            return "com.fuhrer.idea.geronimo.server.GeronimoServer10";
        } else {
            return "com.fuhrer.idea.geronimo.server.GeronimoServer11";
        }
    }

    @Override
    protected List<File> getLibraries() {
        List<File> libraries = super.getLibraries();
        libraries.add(new File(getHome(), "lib"));
        libraries.add(new File(getHome(), "repository/org/apache/geronimo/specs"));
        libraries.add(new File(getHome(), "repository/org/apache/geronimo/modules"));
        libraries.add(new File(getHome(), "repository/org/apache/geronimo/framework"));
        return libraries;
    }

    @Override
    protected Set<Class<?>> getExcludes() {
        Set<Class<?>> excludes = super.getExcludes();
        excludes.add(GeronimoUtil.class);
        excludes.add(GeronimoCommonRoot.class);
        excludes.add(GeronimoEnvironment.class);
        excludes.add(GeronimoModuleId.class);
        excludes.add(GeronimoWebRoot.class);
        return excludes;
    }

    @Override
    @Nullable
    @NonNls
    protected String getContextPathFromWebFacet(WebFacet facet, DeploymentModel deployment) {
        GeronimoWebRoot root = GeronimoUtil.getWebRoot(facet);
        if ((root != null) && !StringUtil.isEmpty(root.getContextRoot().getValue())) {
            return root.getContextRoot().getValue();
        }
        return super.getContextPathFromWebFacet(facet, deployment);
    }
}
