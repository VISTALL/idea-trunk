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

package org.jetbrains.plugins.ruby.jruby.facet.ui;

import com.intellij.openapi.projectRoots.ProjectJdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.jruby.facet.ui.JRubySDKsComboboxWithBrowseButton;
import org.jetbrains.plugins.ruby.jruby.facet.JRubyFacetConfiguration;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: oleg, Roman.Chernyatchik
 * Date: Sep 11, 2007
 */
public class SelectJRubySdkPane {
    private JPanel myPanel;
    private JRubySDKsComboboxWithBrowseButton mySdksComponent;

    public SelectJRubySdkPane(@NotNull final JRubyFacetConfiguration configuration) {

        mySdksComponent.addComboboxActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                configuration.setSdk(getSelectedSdk());
            }
        });

        mySdksComponent.rebuildSdksListAndSelectSdk(configuration.getSdk());
    }

    public ProjectJdk getSelectedSdk(){
        final Object selectedObject = mySdksComponent.getComboBox().getSelectedItem();
        return selectedObject instanceof ProjectJdk ? (ProjectJdk)selectedObject : null;
    }

    public JPanel getPanel() {
        return myPanel;
    }

}
