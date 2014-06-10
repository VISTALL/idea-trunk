/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.execution.configurations.PredefinedLogFile;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.javaee.appServerIntegrations.ApplicationServer;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.artifact.JavaeeArtifactUtil;
import com.intellij.javaee.deployment.DeploymentManager;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentProvider;
import com.intellij.javaee.deployment.DeploymentSource;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.application.JavaeeModule;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.configuration.CommonStrategy;
import com.intellij.javaee.run.configuration.PredefinedLogFilesProvider;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.execution.DefaultOutputProcessor;
import com.intellij.javaee.run.execution.OutputProcessor;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.deployment.DeploymentUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

public abstract class JavaeeServerModel implements ServerModel, PredefinedLogFilesProvider {

    @NonNls
    private static final String LOG_FILE_ID = JavaeeIntegration.getInstance().getName();

    @NonNls
    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public String USERNAME = getDefaultUsername();

    @NonNls
    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public String PASSWORD = getDefaultPassword();

    private CommonModel config;

    public void setCommonModel(CommonModel config) {
        this.config = config;
    }

    public J2EEServerInstance createServerInstance() throws ExecutionException {
        try {
            ((JavaeeIntegration) config.getIntegration()).checkValidServerHome(getHome(), getVersion());
            ClassLoader loader = new JavaeeClassLoader(getLibraries(), getExcludes(), getClass().getClassLoader());
            Constructor<?> constructor = loader.loadClass(getServerName()).getDeclaredConstructor();
            constructor.setAccessible(true);
            JavaeeServer server = JavaeeServer.class.cast(constructor.newInstance());
            server.setHost(getServerHost());
            server.setPort(getServerPort());
            server.setUsername(USERNAME);
            server.setPassword(PASSWORD);
            return new JavaeeServerInstance(config, server);
        } catch (Exception e) {
            throw new ExecutionException(e.getMessage(), e);
        }
    }

    public DeploymentProvider getDeploymentProvider() {
        return new JavaeeDeploymentProvider(false);
    }

    public int getDefaultPort() {
        return 8080;
    }

    public int getLocalPort() {
        return getDefaultPort();
    }

    public String getDefaultUrlForBrowser() {
        return getDefaultUrlForBrowser(true);
    }

    public OutputProcessor createOutputProcessor(ProcessHandler handler, J2EEServerInstance instance) {
        return new DefaultOutputProcessor(handler);
    }

    @Nullable
    public List<Pair<String, Integer>> getAddressesToCheck() {
        return config.isLocal() ? Collections.singletonList(new Pair<String, Integer>(getServerHost(), getServerPort())) : null;
    }

    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isNotEmpty(USERNAME) && StringUtil.isEmpty(PASSWORD)) {
            throw new RuntimeConfigurationError(JavaeeBundle.getText("ServerModel.password"));
        }
    }

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    @Override
    public final Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getHome() {
        ApplicationServer server = config.getApplicationServer();
        return (server != null) ? ((JavaeePersistentData) server.getPersistentData()).HOME : "";
    }

    public String getVersion() {
        ApplicationServer server = config.getApplicationServer();
        return (server != null) ? ((JavaeePersistentData) server.getPersistentData()).VERSION : "";
    }

    public String getVmArguments() {
        return (config instanceof CommonStrategy) ? ((CommonStrategy) config).getSettingsBean().COMMON_VM_ARGUMENTS : "";
    }

    protected abstract boolean isDeploymentSourceSupported(DeploymentSource source);

    @NonNls
    protected abstract String getDefaultUsername();

    @NonNls
    protected abstract String getDefaultPassword();

    @NonNls
    protected abstract String getServerName();

    protected abstract int getServerPort();

    protected String getServerHost() {
        return config.getHost();
    }

    protected CommonModel getCommonModel() {
        return config;
    }

    protected List<File> getLibraries() {
        List<File> libraries = new ArrayList<File>();
        libraries.add(new File(PathUtil.getJarPathForClass(getClass())));
        for (VirtualFile file : config.getApplicationServer().getLibrary().getFiles(OrderRootType.CLASSES)) {
            libraries.add(new File(file.getPresentableUrl()));
        }
        return libraries;
    }

    protected Set<Class<?>> getExcludes() {
        Set<Class<?>> excludes = new HashSet<Class<?>>();
        excludes.add(JavaeeServer.class);
        excludes.add(JavaeeDescriptor.class);
        return excludes;
    }

    String getDefaultUrlForBrowser(boolean addContextPath) {
        @NonNls String url = "http://" + config.getHost() + ':' + config.getPort();
        if (addContextPath) {
            String path = getContextPathFromAppFacets();
            if (StringUtil.isEmpty(path)) {
                path = getContextPathFromWebFacets();
            }
            if (StringUtil.isNotEmpty(path)) {
                url = DeploymentUtil.concatPaths(url, path);
            }
            url = DeploymentUtil.concatPaths(url, "/");
        }
        return url;
    }

    @Nullable
    @NonNls
    private String getContextPathFromAppFacets() {
        List<JavaeeApplicationFacet> facets = new ArrayList<JavaeeApplicationFacet>();
        for (DeploymentModel deployment : config.getDeploymentModels()) {
            Artifact artifact = deployment.getArtifact();
            JavaeeFacet facet = deployment.getFacet();
            if (artifact != null) {
                facets.addAll(JavaeeArtifactUtil.getInstance().getFacetsIncludedInArtifact(config.getProject(), artifact, JavaeeApplicationFacet.ID));
            } else if (facet instanceof JavaeeApplicationFacet) {
                facets.add((JavaeeApplicationFacet) facet);
            }
        }
        for (JavaeeApplicationFacet facet : facets) {
            String path = getContextPathFromAppFacet(facet);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    @Nullable
    @NonNls
    private String getContextPathFromWebFacets() {
        for (DeploymentModel deployment : config.getDeploymentModels()) {
            Artifact artifact = deployment.getArtifact();
            JavaeeFacet facet = deployment.getFacet();
            if (artifact != null) {
                for (WebFacet webFacet : JavaeeArtifactUtil.getInstance().getFacetsIncludedInArtifact(config.getProject(), artifact, WebFacet.ID)) {
                    String path = getContextPathFromWebFacet(webFacet, deployment);
                    if (path != null) {
                        return path;
                    }
                }
            } else if (facet instanceof WebFacet) {
                String path = getContextPathFromWebFacet((WebFacet) facet, deployment);
                if (path != null) {
                    return path;
                }
            }
        }
        for (WebFacet facet : JavaeeFacetUtil.getInstance().getJavaeeFacets(WebFacet.ID, config.getProject())) {
            DeploymentModel model = config.getDeploymentModel(facet);
            if ((model instanceof JavaeeDeploymentModel) && model.DEPLOY) {
                String path = getContextPathFromWebFacet(facet, model);
                if (path != null) {
                    return path;
                }
            }
        }
        return null;
    }

    @Nullable
    @NonNls
    protected String getContextPathFromAppFacet(JavaeeApplicationFacet facet) {
        JavaeeApplication application = facet.getRoot();
        if (application != null) {
            for (JavaeeModule module : application.getModules()) {
                if (module.getWeb().getXmlTag() != null) {
                    return module.getWeb().getContextRoot().getValue();
                }
            }
        }
        return null;
    }

    @Nullable
    @NonNls
    protected String getContextPathFromWebFacet(WebFacet facet, DeploymentModel deployment) {
        DeploymentManager manager = DeploymentManager.getInstance(config.getProject());
        File source = manager.getDeploymentSource(deployment);
        return (source != null) ? StringUtil.trimEnd(source.getName(), ".war") : null;
    }

    @NotNull
    public PredefinedLogFile[] getPredefinedLogFiles() {
        return new PredefinedLogFile[]{new PredefinedLogFile(LOG_FILE_ID, true)};
    }

    @Nullable
    public LogFileOptions getOptionsForPredefinedLogFile(PredefinedLogFile file) {
        if (LOG_FILE_ID.equals(file.getId())) {
            String path = getLogFilePath(getHome());
            if (path != null) {
                String name = JavaeeBundle.getText("ServerModel.logfile", LOG_FILE_ID);
                return new LogFileOptions(name, path, file.isEnabled(), true, false);
            }
        }
        return null;
    }

    @Nullable
    protected String getLogFilePath(String home) {
        return null;
    }

    protected boolean isTruncateLogFile() {
        return false;
    }
}
