/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeeServerModel;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.deployment.DeploymentManager;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentSource;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Set;

abstract class JBossServerModel extends JavaeeServerModel {

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();
        DeploymentManager manager = DeploymentManager.getInstance(getCommonModel().getProject());
        for (final JavaeeFacet facet : JavaeeFacetUtil.getInstance().getAllJavaeeFacets(getCommonModel().getModules())) {
            DeploymentModel deployment = manager.getModelForFacet(getCommonModel(), facet);
            if (deployment != null) {
                File source = manager.getDeploymentSource(deployment);
                if ((source != null) && !JBossExtensions.getInstance().isValidExtension(facet, source)) {
                    String message = JBossBundle.getText("JBossServerModel.error.extension", facet.getName());
                    RuntimeConfigurationError error = new RuntimeConfigurationError(message);
                    error.setQuickFix(new Runnable() {
                        public void run() {
                            JavaeeFacetUtil.getInstance().showFacetBuildSettingsEditor(facet);
                        }
                    });
                    throw error;
                }
            }
        }
    }

    @Override
    protected boolean isDeploymentSourceSupported(DeploymentSource source) {
        return true;
    }

    @Override
    @NonNls
    protected String getDefaultUsername() {
        return "";
    }

    @Override
    @NonNls
    protected String getDefaultPassword() {
        return "";
    }

    @Override
    @NonNls
    protected String getServerName() {
        return "com.fuhrer.idea.jboss.server.JBossServer";
    }

    @Override
    protected List<File> getLibraries() {
        List<File> libraries = super.getLibraries();
        libraries.add(new File(getHome(), "client/jbossall-client.jar"));
        return libraries;
    }

    @Override
    protected Set<Class<?>> getExcludes() {
        Set<Class<?>> excludes = super.getExcludes();
        excludes.add(JBossUtil.class);
        excludes.add(JBossWebRoot.class);
        return excludes;
    }

    @Override
    @Nullable
    @NonNls
    protected String getContextPathFromWebFacet(WebFacet facet, DeploymentModel deployment) {
        JBossWebRoot root = JBossUtil.getWebRoot(facet);
        if ((root != null) && !StringUtil.isEmpty(root.getContextRoot().getValue())) {
            return root.getContextRoot().getValue();
        }
        return super.getContextPathFromWebFacet(facet, deployment);
    }
}
