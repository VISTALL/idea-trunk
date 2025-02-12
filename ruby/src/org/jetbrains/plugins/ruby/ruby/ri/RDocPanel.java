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

package org.jetbrains.plugins.ruby.ruby.ri;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.RBundle;

import javax.swing.*;
import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 09.09.2006
 */
class RDocPanel {
    private JTabbedPane myPane;
    private ProjectRootManager myProjectRootManager;
    private SettingsPane mySettingPane;
    private InfoPane myInfoPane;
    private AboutPane myAboutPane;
    private RDocSettings mySettings;
    private Project myProject;

    public RDocPanel(final Project project, final RDocSettings settings) {
        myProject = project;
        myProjectRootManager = ProjectRootManager.getInstance(project);
        mySettings = settings;
        createPanel();

// Adding project jdk listener
        ((ProjectRootManagerEx) myProjectRootManager).addProjectJdkListener(new ProjectRootManagerEx.ProjectJdkListener() {
            public void projectJdkChanged() {
                fireProjectJdkChanged();
            }
        });
        fireProjectJdkChanged();
    }

    private void fireProjectJdkChanged() {
        myInfoPane.fireJDKChanged();
        mySettingPane.fireJDKChanged();
        myAboutPane.fireJDKChanged();
    }

    private void createPanel() {
        myPane = new JTabbedPane();
        myInfoPane = new InfoPane(this);
        myPane.add(RBundle.message("ruby.ri.info.pane"), myInfoPane.getPanel());
        mySettingPane = new SettingsPane(this, mySettings);
        myPane.add(RBundle.message("ruby.ri.settings.pane"), mySettingPane.getPanel());
        myAboutPane = new AboutPane(this, myProject);
        myPane.add(RBundle.message("ruby.ri.about.pane"), myAboutPane.getPanel());
    }

    public JComponent getPanel() {
        return myPane;
    }

    public ProjectJdk getProjectJdk() {
        return myProjectRootManager.getProjectJdk();
    }


    public String lookup(@NotNull final String name) {
        boolean doUseDefaults = mySettingPane.doUseDefaults();
        int displayWidth = calculateInfoPaneWidth();
        final String item = name.trim();
        if (doUseDefaults) {
            return RIUtil.lookup(myProject, getProjectJdk(), item, true, new String[]{}, displayWidth);
        }
        return RIUtil.lookup(myProject, getProjectJdk(), item, false, mySettingPane.getSelectedDirs(), displayWidth);
    }

    /**
     * Calculates an approximate width of the InfoPane by dividing by ten
     * @return approximate width of the InfoPane
     */
    private int calculateInfoPaneWidth() {
        BigInteger width = BigInteger.valueOf(myInfoPane.getPanel().getWidth()).divide(BigInteger.TEN);
        return width.intValue();
    }

    public void showHelp(@NotNull final String name) {
        myPane.getModel().setSelectedIndex(0);
        myInfoPane.showHelp(name);
    }
}
