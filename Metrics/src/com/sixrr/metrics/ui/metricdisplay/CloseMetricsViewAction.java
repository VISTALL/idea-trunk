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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import com.sixrr.metrics.utils.IconHelper;

import javax.swing.*;

class CloseMetricsViewAction extends AnAction {
    private static final Icon CLOSE_ICON =
            IconHelper.getIcon("/actions/cancel.png");
    private final MetricsToolWindow toolWindow;

    CloseMetricsViewAction(MetricsToolWindow toolWindow) {
        super(MetricsReloadedBundle.message("close.metrics.action"),
                MetricsReloadedBundle.message("close.metrics.description"), CLOSE_ICON);
        this.toolWindow = toolWindow;
    }

    public void actionPerformed(AnActionEvent event) {
        toolWindow.close();
    }
}
