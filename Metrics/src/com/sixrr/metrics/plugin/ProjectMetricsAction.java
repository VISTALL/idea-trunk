/*
 * Copyright 2005, Sixth and Red River Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.metrics.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.utils.IconHelper;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectMetricsAction extends BaseAnalysisAction {

    public ProjectMetricsAction() {
        super(MetricsReloadedBundle.message("metrics.calculation"), MetricsReloadedBundle.message("metrics"));
    }

    protected void analyze(Project project, AnalysisScope analysisScope) {
        final MetricsPlugin plugin = project.getComponent(MetricsPlugin.class);
        final MetricsProfileRepository repository = plugin.getProfileRepository();
        final MetricsProfile profile = repository.getCurrentProfile();
        final MetricsToolWindow toolWindow = plugin.getMetricsToolWindow();
        final MetricsExecutionContextImpl executionContext = new MetricsExecutionContextImpl(project, analysisScope);
        final MetricsRunImpl metricsRun = new MetricsRunImpl();
        final boolean cancelled = executionContext.execute(profile, metricsRun);
        final boolean showOnlyWarnings = plugin.getConfiguration().isShowOnlyWarnings();
        if (cancelled) {
            return;
        }
        if(!metricsRun.hasWarnings(profile)  && showOnlyWarnings)
        {
            final ImageIcon icon = IconHelper.getIcon("/general/informationDialog.png");
            Messages.showMessageDialog(project, "No metrics warnings found", "No metrics warnings found", icon);
            return;
        }
        final String profileName = profile.getName();
        metricsRun.setProfileName(profileName);
        metricsRun.setContext(analysisScope);
        metricsRun.setTimestamp(new TimeStamp());
        toolWindow.show(metricsRun, profile, analysisScope, showOnlyWarnings);
    }

    @Nullable
    protected JComponent getAdditionalActionSettings(Project project,
                                                     BaseAnalysisActionDialog baseAnalysisActionDialog) {
        final MetricsPlugin plugin = project.getComponent(MetricsPlugin.class);
        final MetricsProfileRepository repository = plugin.getProfileRepository();
        final ProfileSelectionPanel profileSelectionPanel = new ProfileSelectionPanel(project, repository);
        return profileSelectionPanel.getPanel();
    }
}
