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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RubyRunConfigurationEditor extends SettingsEditor<RubyRunConfiguration> {
    protected RubyRunConfigurationForm myForm;


    public RubyRunConfigurationEditor(final Project project, final RubyRunConfiguration runConfiguration) {
        myForm = new RubyRunConfigurationForm(project, runConfiguration);
    }

    protected RubyRunConfigurationForm getForm() {
        return myForm;
    }

    protected void resetEditorFrom(RubyRunConfiguration config) {
        RubyRunConfiguration.copyParams(config, myForm);
    }

    protected void applyEditorTo(RubyRunConfiguration config) throws ConfigurationException {
        RubyRunConfiguration.copyParams(myForm, config);
    }

    @NotNull
    protected JComponent createEditor() {
        return myForm.getPanel();
    }

    protected void disposeEditor() {
    }
}

