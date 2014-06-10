/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.GeronimoBundle;
import com.fuhrer.idea.javaee.server.JavaeeDeploymentProvider;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.deployment.DeploymentProvider;
import com.intellij.javaee.deployment.DeploymentSource;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.options.SettingsEditor;

class GeronimoRemoteModel extends GeronimoServerModel {

    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public int JNDI_PORT = 1099;

    public SettingsEditor<CommonModel> getEditor() {
        return new GeronimoRemoteEditor();
    }

    @Override
    public DeploymentProvider getDeploymentProvider() {
        return new JavaeeDeploymentProvider(true);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (JNDI_PORT <= 0) {
            throw new RuntimeConfigurationError(GeronimoBundle.getText("GeronimoRemoteModel.error.jndi"));
        }
        super.checkConfiguration();
    }

    @Override
    protected boolean isDeploymentSourceSupported(DeploymentSource source) {
        return DeploymentSource.FROM_JAR.equals(source);
    }

    @Override
    protected int getServerPort() {
        return JNDI_PORT;
    }
}
