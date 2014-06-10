/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.intellij.facet.pointers.FacetPointer;
import com.intellij.javaee.artifact.JavaeeArtifactUtil;
import com.intellij.javaee.deployment.DeploymentMethod;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentProviderEx;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.javaee.web.artifact.WebArtifactUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.packaging.artifacts.ArtifactPointer;
import com.intellij.packaging.artifacts.ArtifactType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class JavaeeDeploymentProvider extends DeploymentProviderEx {

    private static final DeploymentMethod[] METHODS = {};

    private final boolean deployArchivesOnly;

    public JavaeeDeploymentProvider(boolean deployArchivesOnly) {
        this.deployArchivesOnly = deployArchivesOnly;
    }

    public DeploymentMethod[] getAvailableMethods() {
        return METHODS;
    }

    @Nullable
    public SettingsEditor<DeploymentModel> createAdditionalDeploymentSettingsEditor(CommonModel config, JavaeeFacet facet) {
        return null;
    }

    @Override
    public DeploymentModel createNewDeploymentModel(CommonModel commonModel, ArtifactPointer artifactPointer) {
        return new JavaeeDeploymentModel(commonModel, artifactPointer);
    }

    public DeploymentModel createNewDeploymentModel(CommonModel config, FacetPointer<JavaeeFacet> pointer) {
        return new JavaeeDeploymentModel(config, pointer);
    }

    public void doDeploy(Project project, J2EEServerInstance instance, DeploymentModel deployment) {
        ((JavaeeServerInstance) instance).deploy(deployment);
    }

    public void startUndeploy(J2EEServerInstance instance, DeploymentModel deployment) {
        ((JavaeeServerInstance) instance).undeploy(deployment);
    }

    public void updateDeploymentStatus(J2EEServerInstance instance, DeploymentModel deployment) {
        ((JavaeeServerInstance) instance).updateDeploymentStatus(deployment);
    }

    @Override
    public Collection<? extends ArtifactType> getSupportedArtifactTypes() {
        if (deployArchivesOnly) {
            ArtifactType ear = JavaeeArtifactUtil.getInstance().getEarArtifactType();
            ArtifactType jar = JavaeeArtifactUtil.getInstance().getEjbJarArtifactType();
            ArtifactType war = WebArtifactUtil.getInstance().getWarArtifactType();
            return Arrays.asList(ear, jar, war);
        } else {
            return super.getSupportedArtifactTypes();
        }
    }

    @Nullable
    @NonNls
    public String getHelpId() {
        return null;
    }
}
