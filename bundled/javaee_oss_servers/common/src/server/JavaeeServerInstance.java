/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.PositionManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessAdapter;
import com.intellij.debugger.engine.DefaultJSPPositionManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.PredefinedLogFile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.facet.pointers.FacetPointer;
import com.intellij.javaee.J2EEBundle;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.artifact.JavaeeArtifactUtil;
import com.intellij.javaee.deployment.DeploymentManager;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentStatus;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.application.JavaeeModule;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.localRun.ExecutableObject;
import com.intellij.javaee.serverInstances.DefaultJ2EEServerEvent;
import com.intellij.javaee.serverInstances.DefaultServerInstance;
import com.intellij.javaee.web.artifact.WebArtifactUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.packaging.artifacts.Artifact;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class JavaeeServerInstance extends DefaultServerInstance {

    private final Map<String, String> roots = new HashMap<String, String>();

    @NotNull
    private final JavaeeServer server;

    private final AtomicBoolean connected = new AtomicBoolean(false);

    protected JavaeeServerInstance(@NotNull CommonModel config, @NotNull JavaeeServer server) {
        super(config);
        this.server = server;
    }

    @Override
    public void start(ProcessHandler handler) {
        super.start(handler);
        JavaeeServerModel model = (JavaeeServerModel) getCommonModel().getServerModel();
        if (model.isTruncateLogFile()) {
            for (PredefinedLogFile file : model.getPredefinedLogFiles()) {
                try {
                    new FileOutputStream(model.getOptionsForPredefinedLogFile(file).getPathPattern()).close();
                } catch (Throwable ignore) {
                }
            }
        }
        DebuggerManager.getInstance(getCommonModel().getProject()).addDebugProcessListener(handler, new DebugProcessAdapter() {
            @Override
            public void processAttached(DebugProcess process) {
                process.appendPositionManager(createPositionManager(process));
            }
        });
    }

    @Override
    public boolean connect() throws Exception {
        boolean ok = isConnected(true);
        if (!ok && !getCommonModel().isLocal()) {
            String address = server.getHost() + ':' + server.getPort();
            throw new ExecutionException(J2EEBundle.message("message.text.unable.to.connect", address));
        }
        return ok;
    }

    @Override
    public boolean isConnected() {
        return isConnected(false);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean isStartupScriptTerminatesAfterServerStartup(@NotNull ExecutableObject startupScript) {
        return server.isStartupScriptTerminating();
    }

    @Override
    public void registerServerError(Throwable t) {
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(text);
        t.printStackTrace(out);
        out.close();
        getProcessHandler().notifyTextAvailable(text.toString(), ProcessOutputTypes.STDERR);
    }

    void deploy(final DeploymentModel deployment) {
        if (isConnected()) {
            final File source = getDeploymentSource(deployment);
            if (source != null) {
                ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                    public void run() {
                        try {
                            setDeploymentStatus(deployment, DeploymentStatus.ACTIVATING);
                            setDeploymentStatus(deployment, server.handleDeployment(deployment, source, true, true));
                        } catch (Throwable t) {
                            registerServerError(t);
                            setDeploymentStatus(deployment, DeploymentStatus.FAILED);
                        }
                    }
                });
            }
        } else {
            setDeploymentStatus(deployment, DeploymentStatus.DISCONNECTED);
        }
    }

    void undeploy(final DeploymentModel deployment) {
        if (isConnected()) {
            final File source = getDeploymentSource(deployment);
            if (source != null) {
                ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                    public void run() {
                        try {
                            setDeploymentStatus(deployment, DeploymentStatus.DEACTIVATING);
                            setDeploymentStatus(deployment, server.handleDeployment(deployment, source, false, true));
                        } catch (Throwable t) {
                            registerServerError(t);
                            setDeploymentStatus(deployment, DeploymentStatus.UNKNOWN);
                        }
                    }
                });
            }
        } else {
            setDeploymentStatus(deployment, DeploymentStatus.DISCONNECTED);
        }
    }

    void updateDeploymentStatus(final DeploymentModel deployment) {
        if (isConnected()) {
            final File source = getDeploymentSource(deployment);
            if (source != null) {
                ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                    public void run() {
                        try {
                            setDeploymentStatus(deployment, server.handleDeployment(deployment, source, false, false));
                        } catch (Throwable t) {
                            registerServerError(t);
                            setDeploymentStatus(deployment, DeploymentStatus.UNKNOWN);
                        }
                    }
                });
            }
        } else {
            setDeploymentStatus(deployment, DeploymentStatus.DISCONNECTED);
        }
    }

    String getContextRoot(JavaeeFacet javaeeFacet) {
        return roots.get(javaeeFacet.getName());
    }

    private PositionManager createPositionManager(DebugProcess process) {
        return new DefaultJSPPositionManager(process, getScopeFacetsWithIncluded(getCommonModel())) {
            @Override
            @NonNls
            protected String getGeneratedClassesPackage() {
                return "org.apache.jsp";
            }
        };
    }

    private boolean isConnected(boolean force) {
        if (force || !EventQueue.isDispatchThread()) {
            final AtomicBoolean result = new AtomicBoolean(false);
            Future<?> future = ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                public void run() {
                    try {
                        result.set(server.isConnected());
                    } catch (Exception ignore) {
                    }
                }
            });
            try {
                future.get(5L, TimeUnit.SECONDS);
                if (connected.compareAndSet(!result.get(), result.get())) {
                    fireServerListeners(new DefaultJ2EEServerEvent(connected.get(), !connected.get()));
                }
            } catch (Exception ignore) {
            }
        }
        return connected.get();
    }

    @Nullable
    private File getDeploymentSource(DeploymentModel deployment) {
        File source = DeploymentManager.getInstance(getCommonModel().getProject()).getDeploymentSource(deployment);
        if (source == null) {
            String text;
            FacetPointer<JavaeeFacet> facet = deployment.getFacetPointer();
            if (facet != null) {
                text = JavaeeBundle.getText("ServerInstance.facet.nosource", facet.getFacetName());
            } else {
                text = JavaeeBundle.getText("ServerInstance.artifact.nosource", deployment.getArtifactPointer().getName());
            }
            getProcessHandler().notifyTextAvailable(text, ProcessOutputTypes.STDERR);
            setDeploymentStatus(deployment, DeploymentStatus.FAILED);
        }
        return source;
    }

    private void setDeploymentStatus(final DeploymentModel deployment, final DeploymentStatus status) {
        DeploymentManager manager = DeploymentManager.getInstance(getCommonModel().getProject());
        manager.setDeploymentStatus(deployment.getFacet(), status, getCommonModel(), this);
        String text;
        FacetPointer<JavaeeFacet> facet = deployment.getFacetPointer();
        if (facet != null) {
            text = JavaeeBundle.getText("ServerInstance.facet.status", new Date(), facet.getFacetName(), status.getDescription());
        } else {
            text = JavaeeBundle.getText("ServerInstance.artifact.status", new Date(), deployment.getArtifactPointer().getName(), status.getDescription());
        }
        getProcessHandler().notifyTextAvailable(text, ProcessOutputTypes.SYSTEM);
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                setContextRoot(deployment, status == DeploymentStatus.DEPLOYED);
            }
        });
    }

    private void setContextRoot(DeploymentModel deployment, boolean deployed) {
        Artifact artifact = deployment.getArtifact();
        if (artifact != null) {
            Project project = getCommonModel().getProject();
            JavaeeArtifactUtil util = JavaeeArtifactUtil.getInstance();
            if (util.isJavaeeApplication(artifact.getArtifactType())) {
                for (JavaeeApplicationFacet facet : util.getFacetsIncludedInArtifact(project, artifact, JavaeeApplicationFacet.ID)) {
                    setContextRoot(facet, deployed);
                }
            } else if (WebArtifactUtil.getInstance().isWebApplication(artifact.getArtifactType())) {
                for (WebFacet facet : util.getFacetsIncludedInArtifact(project, artifact, WebFacet.ID)) {
                    setContextRoot(facet, deployment, deployed);
                }
            }
        } else {
            JavaeeFacet facet = deployment.getFacet();
            if (facet != null) {
                if (JavaeeApplicationFacet.ID.equals(facet.getTypeId())) {
                    setContextRoot((JavaeeApplicationFacet) facet, deployed);
                } else if (WebFacet.ID.equals(facet.getTypeId())) {
                    setContextRoot(facet, deployment, deployed);
                }
            }
        }
    }

    private void setContextRoot(JavaeeApplicationFacet facet, boolean deployed) {
        JavaeeApplication application = facet.getRoot();
        if (application != null) {
            for (JavaeeModule module : application.getModules()) {
                if (module.getWeb().getXmlTag() != null) {
                    if (deployed) {
                        roots.put(module.getId().getValue(), module.getWeb().getContextRoot().getValue());
                        server.getContextRoots(facet, roots);
                    } else {
                        roots.remove(module.getId().getValue());
                    }
                }
            }
        }
    }

    private void setContextRoot(JavaeeFacet facet, DeploymentModel deployment, boolean deployed) {
        if (deployed) {
            String root = server.getContextRoot(facet);
            if (root == null) {
                Project project = facet.getModule().getProject();
                File source = DeploymentManager.getInstance(project).getDeploymentSource(deployment);
                if (source != null) {
                    root = StringUtil.trimEnd(source.getName(), ".war");
                }
            }
            roots.put(facet.getName(), root);
        } else {
            roots.remove(facet.getName());
        }
    }
}
