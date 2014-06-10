/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.glassfish.model.GlassfishWebModule;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeServerModel;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentSource;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Set;

public abstract class GlassfishServerModel extends JavaeeServerModel {

    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public boolean PRESERVE;

    @Override
    @NonNls
    protected String getDefaultUsername() {
        return "admin";
    }

    @Override
    @NonNls
    protected String getDefaultPassword() {
        return "adminadmin";
    }

    @Override
    @NonNls
    protected String getServerName() {
        if (GlassfishUtil.isGlassfish3(getVersion())) {
            return "com.fuhrer.idea.glassfish.server.GlassfishServer3";
        } else {
            return "com.fuhrer.idea.glassfish.server.GlassfishServer2";
        }
    }

    @Override
    protected boolean isDeploymentSourceSupported(DeploymentSource source) {
        return true;
    }

    @Override
    protected List<File> getLibraries() {
        List<File> libraries = super.getLibraries();
        libraries.add(new File(getHome(), "lib/appserv-deployment-client.jar"));
        libraries.add(new File(getHome(), "lib/appserv-ext.jar"));
        libraries.add(new File(getHome(), "glassfish/modules"));
        return libraries;
    }

    @Override
    protected Set<Class<?>> getExcludes() {
        Set<Class<?>> excludes = super.getExcludes();
        excludes.add(GlassfishUtil.class);
        excludes.add(GlassfishAppRoot.class);
        excludes.add(GlassfishWebRoot.class);
        excludes.add(GlassfishWebModule.class);
        excludes.add(GlassfishServerModel.class);
        return excludes;
    }

    @Override
    @Nullable
    @NonNls
    protected String getContextPathFromAppFacet(JavaeeApplicationFacet facet) {
        GlassfishAppRoot root = GlassfishUtil.getAppRoot(facet);
        if (root != null) {
            for (GlassfishWebModule module : root.getWebs()) {
                if (!StringUtil.isEmpty(module.getContextRoot().getValue())) {
                    return module.getContextRoot().getValue();
                }
            }
        }
        return super.getContextPathFromAppFacet(facet);
    }

    @Override
    @Nullable
    @NonNls
    protected String getContextPathFromWebFacet(WebFacet facet, DeploymentModel deployment) {
        GlassfishWebRoot root = GlassfishUtil.getWebRoot(facet);
        if ((root != null) && !StringUtil.isEmpty(root.getContextRoot().getValue())) {
            return root.getContextRoot().getValue();
        }
        return super.getContextPathFromWebFacet(facet, deployment);
    }
}
