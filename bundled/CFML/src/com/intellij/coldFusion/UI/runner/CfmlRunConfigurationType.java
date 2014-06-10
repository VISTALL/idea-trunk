package com.intellij.coldFusion.UI.runner;

import com.intellij.coldFusion.UI.CfmlIcons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlRunConfigurationType implements ConfigurationType {
    private final CfmlRunConfigurationFactory myConfigurationFactory;

    CfmlRunConfigurationType() {
        myConfigurationFactory = new CfmlRunConfigurationFactory(this);
    }

    public String getDisplayName() {
        return "Cold Fusion";
    }

    public String getConfigurationTypeDescription() {
        return "Cold Fusion runner description";
    }

    public Icon getIcon() {
        return CfmlIcons.FILETYPE_ICON;
    }

    @NotNull
    public String getId() {
        return getConfigurationTypeDescription();
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myConfigurationFactory};
    }
}
