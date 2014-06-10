/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.intellij.facet.pointers.FacetPointer;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentSource;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.packaging.artifacts.ArtifactPointer;

class JavaeeDeploymentModel extends DeploymentModel {

    JavaeeDeploymentModel(CommonModel config, FacetPointer<JavaeeFacet> pointer) {
        super(config, pointer);
    }

    JavaeeDeploymentModel(CommonModel config, ArtifactPointer pointer) {
        super(config, pointer);
    }

    @Override
    public boolean isDeploymentSourceSupported(DeploymentSource source) {
        return ((JavaeeServerModel) getCommonModel().getServerModel()).isDeploymentSourceSupported(source);
    }
}
