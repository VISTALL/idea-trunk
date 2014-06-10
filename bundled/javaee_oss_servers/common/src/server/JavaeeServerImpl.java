/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class JavaeeServerImpl extends JavaeeServer {

    @Override
    @NotNull
    protected DeploymentStatus handleDeployment(DeploymentModel deployment, File source, boolean deploy, boolean undeploy) throws Exception {
        DeploymentManager manager = getDeploymentManager();
        try {
            String name = getDeploymentId(deployment, source);
            ModuleType type = getModuleType(deployment);
            if (undeploy) {
                waitfor(manager.stop(getTargetModuleIDs(manager.getRunningModules(type, manager.getTargets()), name)));
                waitfor(manager.undeploy(getTargetModuleIDs(manager.getNonRunningModules(type, manager.getTargets()), name)));
            }
            if (deploy) {
                waitfor(manager.distribute(manager.getTargets(), source, null));
                waitfor(manager, type, name);
                waitfor(manager.start(getTargetModuleIDs(manager.getNonRunningModules(type, manager.getTargets()), name)));
            }
            return getDeploymentStatus(manager, type, name);
        } finally {
            manager.release();
        }
    }

    protected abstract DeploymentFactory getDeploymentFactory();

    @NonNls
    protected abstract String getDeployerUrl(String host, int port);

    @Nullable
    @NonNls
    protected abstract String getDeploymentId(DeploymentModel deployment, File source) throws Exception;

    protected boolean matches(TargetModuleID module, String name) {
        return module.getModuleID().equals(name);
    }

    private DeploymentManager getDeploymentManager() throws DeploymentManagerCreationException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            DeploymentFactoryManager factory = DeploymentFactoryManager.getInstance();
            factory.registerDeploymentFactory(getDeploymentFactory());
            return factory.getDeploymentManager(getDeployerUrl(getHost(), getPort()), getUsername(), getPassword());
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    @Nullable
    private ModuleType getModuleType(DeploymentModel deployment) {
        ModuleType type = null;
        if (isAppModule(deployment)) {
            type = ModuleType.EAR;
        } else if (isEjbModule(deployment)) {
            type = ModuleType.EJB;
        } else if (isWebModule(deployment)) {
            type = ModuleType.WAR;
        }
        return type;
    }

    private TargetModuleID[] getTargetModuleIDs(TargetModuleID[] modules, String name) {
        List<TargetModuleID> list = new ArrayList<TargetModuleID>();
        if ((modules != null) && (name != null)) {
            int pos = name.indexOf('|');
            if (pos >= 0) {
                String target = name.substring(0, pos);
                String id = name.substring(pos + 1);
                for (TargetModuleID module : modules) {
                    if (module.getTarget().getName().equals(target) && matches(module, id)) {
                        list.add(module);
                    }
                }
            }
            if (list.isEmpty()) {
                for (TargetModuleID module : modules) {
                    if (matches(module, name)) {
                        list.add(module);
                    }
                }
            }
        }
        return list.toArray(new TargetModuleID[list.size()]);
    }

    private void waitfor(ProgressObject progress) throws Exception {
        JavaeeProgressListener listener = new JavaeeProgressListener(progress);
        progress.addProgressListener(listener);
        try {
            listener.waitForCompletion();
        } finally {
            progress.removeProgressListener(listener);
        }
    }

    private void waitfor(DeploymentManager manager, ModuleType type, String name) {
        long end = System.currentTimeMillis() + 20000;
        do {
            try {
                if (getTargetModuleIDs(manager.getNonRunningModules(type, manager.getTargets()), name).length > 0) {
                    return;
                }
            } catch (Throwable ignore) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        } while (System.currentTimeMillis() < end);
    }

    private DeploymentStatus getDeploymentStatus(DeploymentManager manager, ModuleType type, String name) {
        DeploymentStatus status;
        try {
            if (getTargetModuleIDs(manager.getRunningModules(type, manager.getTargets()), name).length != 0) {
                status = DeploymentStatus.DEPLOYED;
            } else if (getTargetModuleIDs(manager.getNonRunningModules(type, manager.getTargets()), name).length != 0) {
                status = DeploymentStatus.PREPARED;
            } else {
                status = DeploymentStatus.NOT_DEPLOYED;
            }
        } catch (TargetException e) {
            status = DeploymentStatus.UNKNOWN;
        }
        return status;
    }
}
