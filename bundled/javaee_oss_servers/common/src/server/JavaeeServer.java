/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentStatus;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.web.facet.WebFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public abstract class JavaeeServer {

    private String host;

    private int port;

    private String username;

    private String password;

    protected abstract boolean isConnected() throws Exception;

    @NotNull
    protected abstract DeploymentStatus handleDeployment(DeploymentModel deployment, File source, boolean deploy, boolean undeploy) throws Exception;

    @Nullable
    protected abstract String getContextRoot(JavaeeFacet facet);

    protected void getContextRoots(JavaeeFacet facet, Map<String, String> roots) {
    }

    protected boolean isStartupScriptTerminating() {
        return false;
    }

    protected String getHost() {
        return host;
    }

    protected int getPort() {
        return port;
    }

    protected String getUsername() {
        return username;
    }

    protected String getPassword() {
        return password;
    }

    protected boolean isAppModule(DeploymentModel deployment) {
        JavaeeFacet facet = deployment.getFacet();
        return (facet != null) && JavaeeApplicationFacet.ID.equals(facet.getTypeId());
    }

    protected boolean isEjbModule(DeploymentModel deployment) {
        JavaeeFacet facet = deployment.getFacet();
        return (facet != null) && EjbFacet.ID.equals(facet.getTypeId());
    }

    protected boolean isWebModule(DeploymentModel deployment) {
        JavaeeFacet facet = deployment.getFacet();
        return (facet != null) && WebFacet.ID.equals(facet.getTypeId());
    }

    protected boolean isRemote(DeploymentModel deployment) {
        return !deployment.getCommonModel().isLocal() && !"localhost".equals(deployment.getCommonModel().getHost());
    }

    void setHost(String host) {
        this.host = host;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setPassword(String password) {
        this.password = password;
    }
}
