/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.javaee.appServerIntegrations.AppServerDeployedFileUrlProvider;
import com.intellij.javaee.appServerIntegrations.AppServerIntegration;
import com.intellij.javaee.appServerIntegrations.ApplicationServerHelper;
import com.intellij.javaee.appServerIntegrations.ApplicationServerUrlMapping;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.openapi.ex.AppServerIntegrationsManager;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.openapi.deployment.DeploymentUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public abstract class JavaeeIntegration extends AppServerIntegration {

    @SuppressWarnings({"StaticNonFinalField"})
    private static JavaeeIntegration instance;

    @SuppressWarnings({"NonThreadSafeLazyInitialization"})
    public static JavaeeIntegration getInstance() {
        if (instance == null) {
            instance = AppServerIntegrationsManager.getInstance().getIntegration(JavaeeIntegration.class);
        }
        return instance;
    }

    @NotNull
    @NonNls
    public String getComponentName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getPresentableName() {
        return JavaeeBundle.getText("Integration.name", getName());
    }

    @Override
    @Nullable
    public ApplicationServerHelper getApplicationServerHelper() {
        return new JavaeeServerHelper();
    }

    @Override
    @NotNull
    public AppServerDeployedFileUrlProvider getDeployedFileUrlProvider() {
        return new ApplicationServerUrlMapping() {
            @Override
            @Nullable
            public String getUrlForDeployedFile(@NotNull J2EEServerInstance instance, @NotNull DeploymentModel deployment,
                                                @NotNull JavaeeFacet facet, @NotNull String path) {
                if (instance instanceof JavaeeServerInstance) {
                    String root = ((JavaeeServerInstance) instance).getContextRoot(facet);
                    if (root != null) {
                        JavaeeServerModel model = (JavaeeServerModel) instance.getCommonModel().getServerModel();
                        return DeploymentUtil.concatPaths(model.getDefaultUrlForBrowser(false), root, path);
                    }
                }
                return null;
            }
        };
    }

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract Icon getIcon();

    @NotNull
    public abstract Icon getBigIcon();

    public abstract void registerDescriptors();

    @Nullable
    @NonNls
    public abstract String getNameFromTemplate(String template) throws Exception;

    @Nullable
    @NonNls
    public abstract String getVersionFromTemplate(String template) throws Exception;

    @NotNull
    @NonNls
    protected abstract String getServerVersion(String home) throws Exception;

    protected abstract void checkValidServerHome(String home, String version) throws Exception;

    protected abstract void addLibraryLocations(String home, List<File> locations);

    protected boolean allLibrariesFound(Collection<String> classes, Function<String, String> mapper) {
        return classes.isEmpty();
    }

    protected void checkFile(@NonNls String home, @NonNls String path) throws IOException {
        if (!new File(home, path).exists()) {
            throw new FileNotFoundException(JavaeeBundle.getText("Error.fileNotFound", path));
        }
    }
}
