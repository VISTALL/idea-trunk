/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeeServer;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentStatus;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.util.text.StringUtil;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jnp.interfaces.NamingContextFactory;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Hashtable;

class JBossServer extends JavaeeServer {

    @NonNls
    private static final String SERVER = "jboss.system:type=Server";

    @NonNls
    private static final String ADAPTOR = "jmx/invoker/RMIAdaptor";

    @NonNls
    private static final String DEPLOYER = "jboss.system:service=MainDeployer";

    private MBeanServerConnection server;

    private boolean connected;

    @Override
    protected boolean isConnected() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return Boolean.TRUE.equals(getServer().getAttribute(new ObjectName(SERVER), "Started"));
        } catch (MBeanException e) {
            throw e.getTargetException();
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    @Override
    @NotNull
    protected DeploymentStatus handleDeployment(DeploymentModel deployment, File source, boolean deploy, boolean undeploy) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Object[] target = {source.toURL()};
            String[] signature = {URL.class.getName()};
            if (undeploy) {
                getServer().invoke(new ObjectName(DEPLOYER), "undeploy", target, signature);
            }
            if (deploy) {
                getServer().invoke(new ObjectName(DEPLOYER), "deploy", target, signature);
            }
            DeploymentStatus status = DeploymentStatus.UNKNOWN;
            Object result = getServer().invoke(new ObjectName(DEPLOYER), "isDeployed", target, signature);
            if (Boolean.TRUE.equals(result)) {
                status = DeploymentStatus.DEPLOYED;
            } else if (Boolean.FALSE.equals(result)) {
                status = DeploymentStatus.NOT_DEPLOYED;
            }
            return status;
        } catch (MBeanException e) {
            throw e.getTargetException();
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    @Override
    @Nullable
    protected String getContextRoot(JavaeeFacet facet) {
        JBossWebRoot web = JBossUtil.getWebRoot(facet);
        return (web != null) ? web.getContextRoot().getValue() : null;
    }

    @NotNull
    private synchronized MBeanServerConnection getServer() throws Exception {
        if (server == null) {
            ensureConnected();
            server = (MBeanServerConnection) getInitialContext().lookup(ADAPTOR);
        }
        return server;
    }

    private void ensureConnected() throws IOException {
        if (!connected) {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(getHost(), getPort()));
                socket.setSoTimeout(10000);
                InputStream in = socket.getInputStream();
                byte[] buf = new byte[1024];
                while (in.read(buf) > 0) {
                }
                in.close();
            } finally {
                socket.close();
            }
            connected = true;
        }
    }

    @NotNull
    @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "UseOfObsoleteCollectionType"})
    private Context getInitialContext() throws NamingException {
        if (StringUtil.isNotEmpty(getUsername()) && StringUtil.isNotEmpty(getPassword())) {
            SecurityAssociation.setPrincipal(new SimplePrincipal(getUsername()));
            SecurityAssociation.setCredential(getPassword());
        }
        @NonNls Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, NamingContextFactory.class.getName());
        env.put(Context.PROVIDER_URL, "jnp://" + getHost() + ':' + getPort());
        return new InitialContext(env);
    }
}
