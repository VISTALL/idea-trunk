package org.intellij.j2ee.web.resin;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.javaee.run.configuration.J2EEConfigurationFactory;
import com.intellij.javaee.run.configuration.J2EEConfigurationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ResinConfigurationType extends J2EEConfigurationType {

    public ResinConfigurationType() {
    }

    @NotNull
    public String getId() {
        return getClass().getSimpleName();
    }

    public String getDisplayName() {
        return ResinBundle.message("run.config.tab.title.resin");
    }

    public String getConfigurationTypeDescription() {
        return ResinBundle.message("run.config.tab.description.resin");
    }

    public Icon getIcon() {
        return ResinManager.ICON_RESIN;
    }

    @NotNull
    public String getComponentName() {
        return ResinBundle.message("run.config.tab.component.resin");
    }

    protected RunConfiguration createJ2EEConfigurationTemplate(final ConfigurationFactory factory, final Project project,
                                                               final boolean isLocal) {
        return isLocal
               ? J2EEConfigurationFactory.getInstance().createJ2EERunConfiguration(factory, project,
                new ResinModel(),
                ResinManager.getInstance(),
                isLocal,
                new ResinStartupPolicy())
               : J2EEConfigurationFactory.getInstance().createJ2EERunConfiguration(factory, project,
                new ResinModel(),
                ResinManager.getInstance(),
                isLocal,
                new ResinRemoteStartupPolicy());
    }

    public static ResinConfigurationType getInstance() {
        return ApplicationManager.getApplication().getComponent(ResinConfigurationType.class);
    }
}
