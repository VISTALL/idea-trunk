/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishBundle;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.options.SettingsEditor;

class GlassfishRemoteModel extends GlassfishServerModel {

    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public int ADMIN_PORT = 4848;

    public SettingsEditor<CommonModel> getEditor() {
        return new GlassfishRemoteEditor();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (ADMIN_PORT <= 0) {
            throw new RuntimeConfigurationError(GlassfishBundle.getText("GlassfishRemoteModel.error.admin"));
        }
        super.checkConfiguration();
    }

    @Override
    protected int getServerPort() {
        return ADMIN_PORT;
    }
}
