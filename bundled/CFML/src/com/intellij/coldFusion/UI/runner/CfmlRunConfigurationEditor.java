package com.intellij.coldFusion.UI.runner;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlRunConfigurationEditor extends SettingsEditor<CfmlRunConfiguration> {
    private CfmlRunConfigForm myConfigForm;

    public CfmlRunConfigurationEditor(Project project) {
        myConfigForm = new CfmlRunConfigForm(project);
    }

    protected void resetEditorFrom(CfmlRunConfiguration s) {
    }

    protected void applyEditorTo(CfmlRunConfiguration s) throws ConfigurationException {
        s.setParameters(new CfmlRunnerParameters(myConfigForm.getRootURL(), myConfigForm.getFileToView()));
    }

    @NotNull
    protected JComponent createEditor() {
        return myConfigForm.getMainPanel();
    }

    protected void disposeEditor() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setParameters(CfmlRunnerParameters params) {
        myConfigForm.setRootURL(params.getRootURL());
        myConfigForm.setFileToView(params.getStartFile());
    }

    /*
    public String getRootURL() {
        return myConfigForm.getRootURL();
    }

    public void setRootURL(String s) {
        myConfigForm.setRootURL(s);
    }

    public String getFileToView() {
        return myConfigForm.getFileToView();
    }

    public void setFileToView(String s) {
        myConfigForm.setFileToView(s);
    }
    */
}
