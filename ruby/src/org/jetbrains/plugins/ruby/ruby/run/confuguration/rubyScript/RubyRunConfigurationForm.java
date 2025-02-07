/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript;

import com.intellij.execution.junit2.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUIUtil;

import javax.swing.*;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 27, 2006
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class RubyRunConfigurationForm implements RubyRunConfigurationParams {
    protected JPanel generatedPanel;

    protected TextFieldWithBrowseButton scriptPathTextField;
    protected LabeledComponent scriptComponent;

    private RawCommandLineEditor scriptArgsEditor;
    protected LabeledComponent scriptArgsComponent;

    protected TextFieldWithBrowseButton workDirTextField;
    protected LabeledComponent workingDirComponent;

    protected RawCommandLineEditor rubyArgsEditor;
    protected LabeledComponent rubyArgsComponent;

    protected JComboBox myModulesComboBox;
    protected LabeledComponent modulesComponent;

    private JComboBox myAlternativeSdksComboBox;
    private LabeledComponent myAlternativeSdksComponent;
    private JCheckBox myUseAlternativeSdkCB;

    //System Environment
    protected EnvironmentVariablesComponent myEnvVariablesComponent;


    private final Project myProject;
    private final RubyRunConfiguration myConfiguration;

    public RubyRunConfigurationForm(@NotNull final Project project, @NotNull final RubyRunConfiguration configuration){
        myProject = project;
        myConfiguration = configuration;
        initComponents();
    }

    protected void initComponents() {
        RubyRunConfigurationUIUtil.initCommonComponents(myConfiguration, myModulesComboBox, myAlternativeSdksComboBox);

// adding browse action to script chooser
        String title = RBundle.message("run.configuration.messages.select.ruby.scipt.path");
        RubyRunConfigurationUIUtil.addFileChooser(title, scriptPathTextField, myProject);

// adding browse action to working dir chooser
        title = RBundle.message("run.configuration.messages.select.working.dir");
        RubyRunConfigurationUIUtil.addFileChooser(title, workDirTextField, myProject);

        RubyRunConfigurationUIUtil.addAlternativeSDKActionListener(myUseAlternativeSdkCB, myAlternativeSdksComponent, myModulesComboBox);
        setShouldUseAlternativeSdk(false);
    }

    protected LabeledComponent createScriptArgsComponent() {
        final String dialogCaption = RBundle.message("run.configuration.messages.edit.script.args");
        final String text = RBundle.message("run.configuration.messages.script.args");

        final Ref<RawCommandLineEditor> scriptArgsEditorWrapper = new Ref<RawCommandLineEditor>();
        LabeledComponent<RawCommandLineEditor> myComponent = RubyRunConfigurationUIUtil.createRawEditorComponent(scriptArgsEditorWrapper, dialogCaption, text);
        scriptArgsEditor = scriptArgsEditorWrapper.get();

        return myComponent;
    }

    private void createUIComponents() {
        scriptArgsComponent = createScriptArgsComponent();

        final Ref<TextFieldWithBrowseButton> scriptTextFieldWrapper = new Ref<TextFieldWithBrowseButton>();
        scriptComponent = RubyRunConfigurationUIUtil.createScriptPathComponent(scriptTextFieldWrapper, RBundle.message("run.configuration.messages.script.path"));
        scriptPathTextField = scriptTextFieldWrapper.get();

        final Ref<TextFieldWithBrowseButton> wordDirComponentWrapper = new Ref<TextFieldWithBrowseButton>();
        workingDirComponent = RubyRunConfigurationUIUtil.createWorkDirComponent(wordDirComponentWrapper);
        workDirTextField = wordDirComponentWrapper.get();

        final Ref<RawCommandLineEditor> rubyArgsEditorWrapper = new Ref<RawCommandLineEditor>();
        rubyArgsComponent = RubyRunConfigurationUIUtil.createRubyArgsComponent(rubyArgsEditorWrapper);
        rubyArgsEditor = rubyArgsEditorWrapper.get();

        final Ref<JComboBox> modulesComboBoxWrapper = new Ref<JComboBox>();
        modulesComponent = RubyRunConfigurationUIUtil.createModulesComponent(modulesComboBoxWrapper);
        myModulesComboBox = modulesComboBoxWrapper.get();

        final Ref<JComboBox> altSdksComboBoxWrapper = new Ref<JComboBox>();
        myAlternativeSdksComponent = RubyRunConfigurationUIUtil.createAlternativeSdksComponent(altSdksComboBoxWrapper);
        myAlternativeSdksComboBox = altSdksComboBoxWrapper.get();
    }

    /**
     * @return Selected alternativ sdk
     */
    @Nullable
    public ProjectJdk getAlternativeSdk(){
        final Object selectedObject = myAlternativeSdksComboBox.getSelectedItem();
        return selectedObject instanceof ProjectJdk ? (ProjectJdk)selectedObject : null;
    }


    public boolean shouldUseAlternativeSdk() {
        return myUseAlternativeSdkCB.isSelected();
    }


    public void setShouldUseAlternativeSdk(final boolean shouldUse) {
        RubyRunConfigurationUIUtil.setShouldUseAlternSdk(shouldUse, myUseAlternativeSdkCB, myAlternativeSdksComboBox, myModulesComboBox);
    }

    public void setAlternativeSdk(@Nullable final ProjectJdk sdk){
        myAlternativeSdksComboBox.setSelectedItem(sdk);
    }

    public JComponent getPanel(){
        return generatedPanel;
    }

    public String getScriptPath(){
        return FileUtil.toSystemIndependentName(scriptPathTextField.getText().trim());
    }

    public String getScriptArgs(){
        return scriptArgsEditor.getText().trim();
    }

    public String getRubyArgs(){
        return rubyArgsEditor.getText().trim();
    }

    public String getWorkingDirectory(){
        return FileUtil.toSystemIndependentName(workDirTextField.getText().trim());
    }

    public Module getModule(){
        final Object selectedObject = myModulesComboBox.getSelectedItem();
        return selectedObject instanceof Module ? (Module)selectedObject : null;
    }

    public void setScriptPath(final String value){
        scriptPathTextField.setText(FileUtil.toSystemDependentName(TextUtil.getAsNotNull(value)));
    }

    public void setScriptArgs(final String value){
        scriptArgsEditor.setText(value);
    }

    public void setRubyArgs(final String value){
        rubyArgsEditor.setText(value);
    }

    public void setWorkingDirectory(final String value){
        workDirTextField.setText(FileUtil.toSystemDependentName(TextUtil.getAsNotNull(value)));
    }

    public void setModule(@Nullable final Module module){
        myModulesComboBox.setSelectedItem(module);
    }

    protected RubyRunConfiguration getConfiguration() {
        return myConfiguration;
    }

    public void setEnvs(final Map<String, String> envs) {
        myEnvVariablesComponent.setEnvs(envs);
    }

    public void setPassParentEnvs(final boolean passParentEnvs) {
        myEnvVariablesComponent.setPassParentEnvs(passParentEnvs);
    }

    public Map<String, String> getEnvs() {
        return myEnvVariablesComponent.getEnvs();
    }

    public boolean isPassParentEnvs() {
        return myEnvVariablesComponent.isPassParentEnvs();
    }
}
