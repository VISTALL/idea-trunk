/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.geronimo.model.GeronimoModuleId;
import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeServerImpl;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.jmx.KernelDelegate;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

class GeronimoServer11 extends JavaeeServerImpl {

    @NonNls
    private Kernel kernel;

    @Override
    protected boolean isConnected() throws Exception {
        return getKernel().isRunning() && isStarted(getKernel());
    }

    @Override
    protected DeploymentFactory getDeploymentFactory() {
        return new DeploymentFactoryImpl();
    }

    @Override
    @NonNls
    protected String getDeployerUrl(String host, int port) {
        return "deployer:geronimo:jmx://" + host + ':' + port;
    }

    @Override
    @Nullable
    @NonNls
    protected String getDeploymentId(final DeploymentModel deployment, File source) throws Exception {
        String id = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
            @Nullable
            public String compute() {
                GeronimoCommonRoot root = GeronimoUtil.getCommonRoot(deployment);
                if (root != null) {
                    GeronimoModuleId id = root.getEnvironment().getModuleId();
                    if (id.getXmlTag() != null) {
                        return getDeploymentId(id);
                    }
                }
                return null;
            }
        });
        if (id == null) {
            id = getDeploymentId(source);
        }
        return id;
    }

    @Override
    @Nullable
    protected String getContextRoot(JavaeeFacet facet) {
        GeronimoWebRoot web = GeronimoUtil.getWebRoot(facet);
        return (web != null) ? web.getContextRoot().getValue() : null;
    }

    @Override
    protected boolean matches(TargetModuleID module, String name) {
        return Artifact.create(name).matches(Artifact.create(module.getModuleID()));
    }

    @SuppressWarnings({"CallToNativeMethodWhileLocked"})
    private synchronized Kernel getKernel() throws IOException {
        if (kernel == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                String[] credentials = {getUsername(), getPassword()};
                Map<String, String[]> env = Collections.singletonMap("jmx.remote.credentials", credentials);
                @NonNls String url = "service:jmx:rmi://" + getHost() + "/jndi/rmi://" + getHost() + ':' + getPort() + "/JMXConnector";
                kernel = new KernelDelegate(JMXConnectorFactory.connect(new JMXServiceURL(url), env).getMBeanServerConnection());
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }
        return kernel;
    }

    @SuppressWarnings({"unchecked"})
    private boolean isStarted(Kernel k) throws Exception {
        Set<AbstractName> configs = k.listGBeans(new AbstractNameQuery((PersistentConfigurationList.class).getName()));
        return !configs.isEmpty() && Boolean.TRUE.equals(k.getAttribute(configs.iterator().next(), "kernelFullyStarted"));
    }

    private String getDeploymentId(GeronimoModuleId id) {
        StringBuilder str = new StringBuilder();
        String group = id.getGroupId().getValue();
        str.append((group == null) ? Artifact.DEFAULT_GROUP_ID : group);
        str.append('/');
        String artifact = id.getArtifactId().getValue();
        str.append((artifact == null) ? "" : artifact);
        str.append('/');
        String version = id.getVersion().getValue();
        str.append((version == null) ? "" : version);
        str.append('/');
        String type = id.getType().getValue();
        str.append((type == null) ? "" : type);
        return str.toString();
    }

    private String getDeploymentId(File file) {
        String name = file.getName();
        if (name.indexOf('.') > 0) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        return '/' + name + "//";
    }
}
