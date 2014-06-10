/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.jboss.JBossBundle;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.options.SettingsEditor;

class JBossRemoteModel extends JBossServerModel {

    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public int JNDI_PORT = 1099;

    public SettingsEditor<CommonModel> getEditor() {
        return new JBossRemoteEditor();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (JNDI_PORT <= 0) {
            throw new RuntimeConfigurationError(JBossBundle.getText("JBossRemoteModel.error.jndi"));
        }
        super.checkConfiguration();
    }

    @Override
    protected int getServerPort() {
        return JNDI_PORT;
    }
}
