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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.utils.IconHelper;

import javax.swing.*;

class ToggleAutoscrollAction extends ToggleAction {
    private static final Icon toggleAutoscrollIcon =
            IconHelper.getIcon("/general/autoscrollToSource.png");

    private final MetricsReloadedConfig configuration;

    ToggleAutoscrollAction(MetricsReloadedConfig configuration) {
        super(MetricsReloadedBundle.message("autoscroll.to.source.action"),
                MetricsReloadedBundle.message("autoscroll.to.source.description"),
                toggleAutoscrollIcon);
        this.configuration = configuration;
    }

    public boolean isSelected(AnActionEvent event) {
        return configuration.isAutoscroll();
    }

    public void setSelected(AnActionEvent event, boolean b) {
        configuration.setAutoscroll(b);
    }
}
