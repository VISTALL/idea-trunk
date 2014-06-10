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

package org.jetbrains.plugins.grails.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;

public class GenerateViewsAction extends AnAction implements DumbAware {

  public void actionPerformed(final AnActionEvent e) {
    final Module module = e.getData(DataKeys.MODULE);

    final Project project = module.getProject();

    PsiFile psiFile = e.getData(DataKeys.PSI_FILE);
    final String name = GrailsUtils.getCategoryName(psiFile, module);
    if (name == null) return;

    ProcessBuilder pb = GrailsUtils.createGrailsCommand(module, getCommand(), name);

    MvcConsole.getInstance(project).executeProcess(module, pb, new Runnable() {
      public void run() {
        VirtualFile dir = GrailsUtils.findGrailsAppRoot(module);
        assert dir != null;
        VirtualFile file = dir.findFileByRelativePath("grails-app/controllers/" + name + "Controller.groovy");
        if (file == null)
          return;

        FileEditorManager.getInstance(project).openFile(file, true);
      }
    }, true);
  }

  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    super.update(e);

    if (!presentation.isEnabled())
      return;

    final Module module = e.getData(DataKeys.MODULE);

    if (module != null && GrailsUtils.hasGrailsSupport(module)) {
      PsiFile file = e.getData(DataKeys.PSI_FILE);
      String categoryName = GrailsUtils.getCategoryName(file, module);
      if (categoryName == null) return;
      if (GrailsUtils.findDomainClassFile(categoryName, module) != null) {
        return;
      }
    }

    presentation.setEnabled(false);
    presentation.setVisible(false);
  }

  private static String getCommand() {
    return "generate-views";
  }

}
