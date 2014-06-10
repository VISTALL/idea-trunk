/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.GeronimoBundle;
import com.fuhrer.idea.javaee.server.JavaeePortConfig;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.deployment.DeploymentSource;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;

class GeronimoLocalModel extends GeronimoServerModel {

    public SettingsEditor<CommonModel> getEditor() {
        return new GeronimoLocalEditor();
    }

    @Override
    public int getLocalPort() {
        return GeronimoPortConfig.getLocal(this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (getServerPort() == JavaeePortConfig.INVALID_PORT) {
            throw new RuntimeConfigurationError(GeronimoBundle.getText("GeronimoLocalModel.error.invalid"));
        } else if (getServerPort() <= 0) {
            throw new RuntimeConfigurationError(GeronimoBundle.getText("GeronimoLocalModel.error.disabled"));
        }
        super.checkConfiguration();
    }

    @Override
    protected boolean isDeploymentSourceSupported(DeploymentSource source) {
        return true;
    }

    @Override
    protected int getServerPort() {
        return GeronimoPortConfig.getAdmin(this);
    }

    @Override
    @Nullable
    protected String getLogFilePath(String home) {
        if (!StringUtil.isEmpty(home)) {
            return new File(home, "var/log/geronimo.log").getAbsolutePath();
        }
        return null;
    }
}
