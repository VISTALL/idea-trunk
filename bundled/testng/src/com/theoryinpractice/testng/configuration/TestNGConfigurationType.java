/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Jul 2, 2005
 * Time: 12:10:47 AM
 */
package com.theoryinpractice.testng.configuration;

import com.intellij.execution.LocatableConfigurationType;
import com.intellij.execution.Location;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.theoryinpractice.testng.model.TestData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TestNGConfigurationType implements LocatableConfigurationType
{
    private static final Logger LOGGER = Logger.getInstance("TestNG Runner");
    private static final Icon ICON = IconLoader.getIcon("/resources/testNG.png");

    private final ConfigurationFactory myFactory;

    public TestNGConfigurationType() {

        myFactory = new ConfigurationFactory(this)
        {
            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                LOGGER.info("Create TestNG Template Configuration");
                return new TestNGConfiguration("", project, this);
            }

          @Override
          public Icon getIcon(@NotNull final RunConfiguration configuration) {
            return RunConfigurationExtension.getIcon((TestNGConfiguration)configuration, ICON);
          }
        };
    }

    public static TestNGConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(TestNGConfigurationType.class);
    }

    public RunnerAndConfigurationSettings createConfigurationByLocation(Location location) {
      return null;
    }

    public boolean isConfigurationByLocation(RunConfiguration runConfiguration, Location location) {
        TestNGConfiguration config = (TestNGConfiguration) runConfiguration;
        TestData testobject = config.getPersistantData();
        if (testobject == null)
            return false;
        else {
          final PsiElement element = location.getPsiElement();
          if (testobject.isConfiguredByElement(element)) {
            return element instanceof PsiPackage ||
                   Comparing.equal(ModuleUtil.findModuleForPsiElement(element),
                                   ((TestNGConfiguration)runConfiguration).getConfigurationModule().getModule());

          }
          else {
            return false;
          }
        }
    }

    public String getDisplayName() {
        return "TestNG";
    }

    public String getConfigurationTypeDescription() {
        return "TestNG Configuration";
    }

    public Icon getIcon() {
        return ICON;
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[] {myFactory};
    }

    @NotNull
    public String getId() {
        return "TestNG";
    }

}