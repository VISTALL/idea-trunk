/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
package org.jetbrains.plugins.grails.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;
import org.jetbrains.plugins.grails.util.GrailsUtils;

/**
 & @author ven
 */
public class GrailsWarAction extends AnAction {
  @NonNls private final static String WAR_TARGET = "war";

  public void actionPerformed(AnActionEvent event) {
    final Module module = event.getData(DataKeys.MODULE);
    if (module == null) return;

    final Project project = event.getData(DataKeys.PROJECT);

    ProcessBuilder pb = GrailsUtils.createGrailsCommand(module, WAR_TARGET);

    MvcConsole.getInstance(project).executeProcess(module, pb, null, true);
  }

  public void update(AnActionEvent event) {
    final Module module = event.getData(DataKeys.MODULE);
    event.getPresentation().setEnabled(module != null && GrailsUtils.hasGrailsSupport(module));
  }
}
