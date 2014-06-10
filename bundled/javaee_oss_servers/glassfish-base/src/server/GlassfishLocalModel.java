/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishBundle;
import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.javaee.server.JavaeePortConfig;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;

class GlassfishLocalModel extends GlassfishServerModel {

    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public String DOMAIN = "";

    public SettingsEditor<CommonModel> getEditor() {
        return new GlassfishLocalEditor();
    }

    @Override
    public int getLocalPort() {
        return GlassfishPortConfig.getLocal(this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isEmpty(DOMAIN)) {
            throw new RuntimeConfigurationError(GlassfishBundle.getText("GlassfishLocalModel.error.missing"));
        }
        if (getServerPort() == JavaeePortConfig.INVALID_PORT) {
            throw new RuntimeConfigurationError(GlassfishBundle.getText("GlassfishLocalModel.error.invalid"));
        } else if (getServerPort() <= 0) {
            throw new RuntimeConfigurationError(GlassfishBundle.getText("GlassfishLocalModel.error.disabled"));
        }
        super.checkConfiguration();
    }

    @Override
    protected int getServerPort() {
        return GlassfishPortConfig.getAdmin(this);
    }

    @Override
    @Nullable
    protected String getLogFilePath(String home) {
        if (!StringUtil.isEmpty(home) && !StringUtil.isEmpty(DOMAIN)) {
            String path = "domains/" + DOMAIN + "/logs/server.log";
            if (GlassfishUtil.isGlassfish3(getVersion())) {
                path = "glassfish/" + path;
            }
            return new File(home, path).getAbsolutePath();
        }
        return null;
    }
}
