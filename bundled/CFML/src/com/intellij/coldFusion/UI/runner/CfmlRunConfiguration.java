package com.intellij.coldFusion.UI.runner;

import com.intellij.coldFusion.UI.facet.CfmlFacet;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlRunConfiguration extends RunConfigurationBase {
    private String ROOT_URL = "cfmlrooturl";
    private String START_FILE = "cfmlstartfile";

    private CfmlRunnerParameters myRunnerParameters = new CfmlRunnerParameters("", "");

    private CfmlRunConfigurationEditor myConfigurationEditor;

    protected CfmlRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
        myConfigurationEditor = new CfmlRunConfigurationEditor(project);
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return myConfigurationEditor;
    }

    public JDOMExternalizable createRunnerSettings(ConfigurationInfoProvider provider) {
        return null;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        myRunnerParameters.setRootURL(element.getAttribute(ROOT_URL).getValue());
        myRunnerParameters.setStartFile(element.getAttribute(START_FILE).getValue());
        myConfigurationEditor.setParameters(myRunnerParameters);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        element.setAttribute(ROOT_URL, myRunnerParameters.getRootURL());
        element.setAttribute(START_FILE, myRunnerParameters.getStartFile());
    }

    public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(ProgramRunner runner) {
        return null;
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new RunProfileState() {
            public ExecutionResult execute(Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
                return null;
            }

            public RunnerSettings getRunnerSettings() {
                return null;
            }

            public ConfigurationPerRunnerSettings getConfigurationSettings() {
                return new ConfigurationPerRunnerSettings("CfmlRunner", null);
            }
        };
    }

    public void checkConfiguration() throws RuntimeConfigurationException {
        // check URL for correctness
        try {
            new URL(getURL());
        }
        catch (MalformedURLException e) {
            throw new RuntimeConfigurationError("Incorrect URL");
        }
    }

    public CfmlRunnerParameters getRunnerParameters() {
        return myRunnerParameters;
    }

    public void setParameters(CfmlRunnerParameters params) {
        myRunnerParameters = params;
    }

    public String getModuleURL(Module module) {
        Collection<CfmlFacet> moduleFacet = FacetManager.getInstance(module).getFacetsByType(CfmlFacet.ID);
        String URLToOpen = "http://" + myRunnerParameters.getRootURL();
        // if there is a coldfusion facet, where the relative folder was specified, than adding /foldername to URL
        if (moduleFacet.size() > 0) {
            // getting the facet
            CfmlFacet facet = moduleFacet.iterator().next();
            // adding relative path (context)
            if (!facet.getConfiguration().getMyServerRelativePath().equals("")) {
                URLToOpen += "/" + facet.getConfiguration().getMyServerRelativePath();
            }
        }
        // adding path relative to project root
        URLToOpen += myRunnerParameters.getStartFile().substring(getProject().getBaseDir().getPath().length());
        return URLToOpen;
    }

    public String getURL() {
        String URLToOpen = "http://" + myRunnerParameters.getRootURL();
        // adding path relative to project root
        String startFile = myRunnerParameters.getStartFile();
        URLToOpen += myRunnerParameters.getStartFile().substring(getProject().getBaseDir().getPath().length());
        return URLToOpen;
    }
}
