/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class IDESettingsEditor implements Configurable, Configurable.Composite, ProjectSettingsAwareEditor, ApplicationSettingsAwareEditor {
    public IDESettingsEditor(FrameworkDefinitionsEditor frameworkDefinitionsEditor, LibraryBundlingEditor libraryBundlingEditor) {
        this.frameworkDefinitionsEditor = frameworkDefinitionsEditor;
        this.libraryBundlingEditor = libraryBundlingEditor;
    }

    @Nls
    public String getDisplayName() {
        return "IDE Settings";
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return mainPanel;
    }

    public boolean isModified() {
        return false;
    }

    public void apply() throws ConfigurationException {
    }

    public void reset() {
    }

    public void disposeUIResources() {
    }

    public Configurable[] getConfigurables() {
        return new Configurable[]{frameworkDefinitionsEditor, libraryBundlingEditor};
    }

    public void setProjectSettingsProvider(@NotNull ProjectSettingsProvider projectSettingsProvider) {
        frameworkDefinitionsEditor.setProjectSettingsProvider(projectSettingsProvider);
    }

    public void setApplicationSettingsProvider(
            @NotNull ApplicationSettingsProvider applicationSettingsProvider) {
        frameworkDefinitionsEditor.setApplicationSettingsProvider(applicationSettingsProvider);
        libraryBundlingEditor.setApplicationSettingsProvider(applicationSettingsProvider);
    }

    private JPanel mainPanel;
    private FrameworkDefinitionsEditor frameworkDefinitionsEditor;
    private LibraryBundlingEditor libraryBundlingEditor;
}
