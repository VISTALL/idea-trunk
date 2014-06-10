/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeePortConfig;
import com.fuhrer.idea.jboss.JBossBundle;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;

class JBossLocalModel extends JBossServerModel {

    @NonNls
    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public String SERVER = "";

    public SettingsEditor<CommonModel> getEditor() {
        return new JBossLocalEditor();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isEmpty(SERVER)) {
            throw new RuntimeConfigurationError(JBossBundle.getText("JBossLocalModel.error.missing"));
        }
        if (getServerPort() == JavaeePortConfig.INVALID_PORT) {
            throw new RuntimeConfigurationError(JBossBundle.getText("JBossLocalModel.error.invalid"));
        } else if (getServerPort() <= 0) {
            throw new RuntimeConfigurationError(JBossBundle.getText("JBossLocalModel.error.disabled"));
        }
        super.checkConfiguration();
    }

    @Override
    protected int getServerPort() {
        return JBossPortConfig.get(this);
    }

    @Override
    @Nullable
    protected String getLogFilePath(String home) {
        if (!StringUtil.isEmpty(home) && !StringUtil.isEmpty(SERVER)) {
            return new File(home, "server/" + SERVER + "/log/server.log").getAbsolutePath();
        }
        return null;
    }

    @Override
    protected boolean isTruncateLogFile() {
        return "5.1".compareTo(getVersion()) > 0;
    }
}
