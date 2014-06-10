/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.localRun.ExecutableObjectStartupPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class JavaeeConfigurationType implements ConfigurationType {

    private final ConfigurationFactory local = new JavaeeConfigurationFactory(this, JavaeeBundle.getText("ConfigurationType.local"), getLocalIcon(), true) {
        @Override
        @NotNull
        protected ServerModel createServerModel() {
            return createLocalModel();
        }

        @Override
        protected ExecutableObjectStartupPolicy createPolicy() {
            return createStartupPolicy();
        }
    };

    private final ConfigurationFactory remote = new JavaeeConfigurationFactory(this, JavaeeBundle.getText("ConfigurationType.remote"), getRemoteIcon(), false) {
        @Override
        @NotNull
        protected ServerModel createServerModel() {
            return createRemoteModel();
        }

        @Override
        @Nullable
        protected ExecutableObjectStartupPolicy createPolicy() {
            return null;
        }
    };

    @NotNull
    public String getId() {
        return getClass().getSimpleName();
    }

    public Icon getIcon() {
        return JavaeeIntegration.getInstance().getIcon();
    }

    public String getDisplayName() {
        return JavaeeBundle.getText("ConfigurationType.name", JavaeeIntegration.getInstance().getName());
    }

    public String getConfigurationTypeDescription() {
        return JavaeeBundle.getText("ConfigurationType.description", JavaeeIntegration.getInstance().getName());
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{local, remote};
    }

    @NotNull
    protected Icon getLocalIcon() {
        return getIcon();
    }

    @NotNull
    protected Icon getRemoteIcon() {
        return getIcon();
    }

    @NotNull
    protected abstract ServerModel createLocalModel();

    @NotNull
    protected abstract ServerModel createRemoteModel();

    @NotNull
    protected abstract ExecutableObjectStartupPolicy createStartupPolicy();
}
