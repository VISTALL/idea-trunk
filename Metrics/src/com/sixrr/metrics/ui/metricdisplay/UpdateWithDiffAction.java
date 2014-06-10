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

package com.sixrr.metrics.ui.metricdisplay;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.utils.IconHelper;

import javax.swing.*;

class UpdateWithDiffAction extends AnAction {
    private static final Icon RELOAD_ICON = IconHelper.getIcon("/actions/diff.png");
    private final Project project;
    private final MetricsToolWindow toolWindow;

    UpdateWithDiffAction(MetricsToolWindow toolWindow, Project project) {
        super(MetricsReloadedBundle.message("update.with.differences.action"),
                MetricsReloadedBundle.message("update.with.differences.description"), RELOAD_ICON);
        this.project = project;
        this.toolWindow = toolWindow;
    }

    public void actionPerformed(AnActionEvent event) {
        final AnalysisScope scope = toolWindow.getCurrentScope();
        final MetricsExecutionContextImpl executionContext = new MetricsExecutionContextImpl(project, scope);
        final MetricsProfile currentProfile = toolWindow.getCurrentProfile();
        final MetricsRunImpl metricsRun = new MetricsRunImpl();
        final boolean cancelled = executionContext.execute(currentProfile, metricsRun);
        if (!cancelled) {
            metricsRun.setContext(scope);
            metricsRun.setProfileName(currentProfile.getName());
            metricsRun.setTimestamp(new TimeStamp());
            toolWindow.updateWithDiff(metricsRun);
        }
    }
}
