/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.glassfish.model.GlassfishWebModule;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeServer;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentStatus;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.model.xml.application.JavaeeModule;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.sun.enterprise.deployment.client.DeploymentFacility;
import com.sun.enterprise.deployment.client.DeploymentFacilityFactory;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class GlassfishServer2 extends JavaeeServer {

    @NonNls
    private static final String SERVER = "amx:j2eeType=J2EEServer,name=server";

    @NonNls
    private static final String[] TARGETS = {"server"};

    @NonNls
    private MBeanServerConnection server;

    @Override
    protected boolean isConnected() throws Exception {
        try {
            return getServer().getAttribute(new ObjectName(SERVER), "state") instanceof Integer;
        } catch (MBeanException e) {
            throw e.getTargetException();
        }
    }

    @Override
    @NotNull
    protected DeploymentStatus handleDeployment(DeploymentModel deployment, File source, boolean deploy, boolean undeploy) throws Exception {
        DeploymentFacility facility = getDeploymentFacility();
        try {
            String name = FileUtil.getNameWithoutExtension(source);
            if (undeploy) {
                facility.waitFor(facility.undeploy(facility.createTargets(TARGETS), name));
            }
            if (deploy) {
                @NonNls String prefix = source.isDirectory() ? "file" : "jar";
                URI uri = new URI(prefix, "", source.toURI().getSchemeSpecificPart(), null, null);
                AbstractArchive archive = new ArchiveFactory().openArchive(uri);
                Properties options = new Properties();
                options.setProperty(DeploymentProperties.TARGET, TARGETS[0]);
                options.setProperty(DeploymentProperties.ARCHIVE_NAME, source.getAbsolutePath());
                options.setProperty(DeploymentProperties.NAME, name);
                setContextRoot(options, deployment, name);
                facility.waitFor(facility.deploy(facility.createTargets(TARGETS), archive, null, options));
            }
            return getDeploymentStatus(facility, name);
        } finally {
            facility.disconnect();
        }
    }

    private void setContextRoot(final Properties options, DeploymentModel deployment, final String name) {
        if (isRemote(deployment)) {
            final GlassfishWebRoot web = GlassfishUtil.getWebRoot(deployment.getFacet());
            if (web != null) {
                ApplicationManager.getApplication().runReadAction(new Runnable() {
                    public void run() {
                        if (StringUtil.isEmpty(web.getContextRoot().getValue())) {
                            options.setProperty(DeploymentProperties.CONTEXT_ROOT, name);
                        }
                    }
                });
            }
        }
    }

    @Override
    @Nullable
    protected String getContextRoot(JavaeeFacet facet) {
        GlassfishWebRoot web = GlassfishUtil.getWebRoot(facet);
        return web != null ? web.getContextRoot().getValue() : null;
    }

    @Override
    protected void getContextRoots(JavaeeFacet facet, Map<String, String> roots) {
        GlassfishAppRoot app = GlassfishUtil.getAppRoot(facet);
        if (app != null) {
            for (GlassfishWebModule web : app.getWebs()) {
                JavaeeModule source = web.getWebUri().getValue();
                if ((source != null) && !StringUtil.isEmpty(web.getContextRoot().getValue())) {
                    roots.put(source.getId().getValue(), web.getContextRoot().getValue());
                }
            }
        }
    }

    @Override
    protected boolean isStartupScriptTerminating() {
        return true;
    }

    @SuppressWarnings({"CallToNativeMethodWhileLocked"})
    private synchronized MBeanServerConnection getServer() throws IOException {
        if (server == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                @NonNls JMXServiceURL url = new JMXServiceURL("s1ashttp", getHost(), getPort());
                @NonNls Map<String, Object> env = new HashMap<String, Object>();
                env.put(JMXConnector.CREDENTIALS, new String[]{getUsername(), getPassword()});
                env.put("com.sun.enterprise.as.http.auth", "BASIC");
                env.put("USER", getUsername());
                env.put("PASSWORD", getPassword());
                env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "com.sun.enterprise.admin.jmx.remote.protocol");
                server = JMXConnectorFactory.connect(url, env).getMBeanServerConnection();
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }
        return server;
    }

    private DeploymentFacility getDeploymentFacility() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            DeploymentFacility facility = DeploymentFacilityFactory.getDeploymentFacility();
            ServerConnectionIdentifier identifier = new ServerConnectionIdentifier();
            identifier.setHostName(getHost());
            identifier.setHostPort(getPort());
            identifier.setUserName(getUsername());
            identifier.setPassword(getPassword());
            identifier.setSecure(false); // todo: should we support secure connections?
            facility.connect(identifier);
            return facility;
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    private DeploymentStatus getDeploymentStatus(DeploymentFacility facility, String name) {
        DeploymentStatus status = DeploymentStatus.NOT_DEPLOYED;
        try {
            TargetModuleID[] modules = facility.listAppRefs(TARGETS);
            for (TargetModuleID module : modules) {
                if (name.equals(module.getModuleID())) {
                    status = DeploymentStatus.DEPLOYED;
                }
            }
        } catch (IOException e) {
            status = DeploymentStatus.UNKNOWN;
        }
        return status;
    }
}
