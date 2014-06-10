/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeServerImpl;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.jmx.KernelDelegate;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

class GeronimoServer10 extends JavaeeServerImpl {

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
        return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
            @Nullable
            public String compute() {
                GeronimoCommonRoot root = GeronimoUtil.getCommonRoot(deployment);
                return (root != null) ? root.getConfigId().getStringValue() : null;
            }
        });
    }

    @Override
    @Nullable
    protected String getContextRoot(JavaeeFacet facet) {
        GeronimoWebRoot web = GeronimoUtil.getWebRoot(facet);
        return (web != null) ? web.getContextRoot().getValue() : null;
    }

    private synchronized Kernel getKernel() throws IOException {
        if (kernel == null) {
            String[] credentials = {getUsername(), getPassword()};
            Map<String, String[]> env = Collections.singletonMap("jmx.remote.credentials", credentials);
            @NonNls String url = "service:jmx:rmi://" + getHost() + "/jndi/rmi://" + getHost() + ':' + getPort() + "/JMXConnector";
            kernel = new KernelDelegate(JMXConnectorFactory.connect(new JMXServiceURL(url), env).getMBeanServerConnection());
        }
        return kernel;
    }

    @SuppressWarnings({"unchecked"})
    private boolean isStarted(Kernel k) throws Exception {
        Set<ObjectName> configs = k.listGBeans(new GBeanQuery(null, PersistentConfigurationList.class.getName()));
        return !configs.isEmpty() && Boolean.TRUE.equals(k.getAttribute(configs.iterator().next(), "kernelFullyStarted"));
    }
}
