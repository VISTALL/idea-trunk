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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.actions.GroovyTemplatesFactory;

import java.io.IOException;

public class GenerateControllerTestsAction extends AnAction implements DumbAware {
  private static final String CONTROLLER_TESTS_GROOVY = "ControllerTests.groovy";
  private final boolean myIntegration;

  public GenerateControllerTestsAction(boolean integration) {
    myIntegration = integration;
  }

  public void actionPerformed(final AnActionEvent event) {
    final Module module = event.getData(DataKeys.MODULE);

    assert module != null;
    final Project project = module.getProject();

    PsiFile psiFile = event.getData(DataKeys.PSI_FILE);
    final String name = GrailsUtils.getCategoryName(psiFile, module);
    if (name == null || GrailsUtils.findControllerClassFile(name, module) == null) {
      Messages.showErrorDialog(GrailsBundle.message("generate.controller.first", name), GrailsBundle.message("no.controller.found"));
      return;
    }

    final String plainName = StringUtil.getShortName(name);
    final String dir = getTestDirectory(module, name, myIntegration);
    if (dir != null) {
      Runnable runnable = new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              try {
                final VirtualFile targetDir = createDirectory(dir);
                if (targetDir != null) {
                  final PsiDirectoryFactory factory = PsiDirectoryFactory.getInstance(project);
                  final PsiDirectory psiDir = factory.createDirectory(targetDir);
                  final PsiFile newFile =
                    GroovyTemplatesFactory.createFromTemplate(psiDir, plainName, plainName + CONTROLLER_TESTS_GROOVY, "GroovyControllerTests.groovy");
                  if (newFile != null) {
                    VirtualFile virtualFile = newFile.getVirtualFile();
                    if (virtualFile != null) {
                      FileEditorManager.getInstance(project).openFile(virtualFile, true);
                    }
                  }
                }
              }
              catch (IncorrectOperationException e) {
                e.printStackTrace();
              }
              catch (IOException e) {
                e.printStackTrace();
              }
            }
          });
        }
      };
      CommandProcessor.getInstance().executeCommand(project, runnable, "Create " + plainName + "ControllerTests", null);
    } else {
      Messages
        .showErrorDialog("Test directory not found", GrailsBundle.message("no.dir.found"));
    }
  }

  @Nullable
  public static String getTestDirectory(Module module, String qname, final boolean integration) {
    final PsiDirectory directory = integration ? GrailsUtils.getIntegrationTestsDirectory(module) : GrailsUtils.getUnitTestsDirectory(module);
    if (directory == null) {
      return null;
    }

    return directory.getVirtualFile().getPath() + "/" + StringUtil.getPackageName(qname).replace('.', '/');
  }

  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    super.update(e);

    if (!presentation.isEnabled()) return;

    final Module module = e.getData(DataKeys.MODULE);
    if (module != null && GrailsUtils.hasGrailsSupport(module)) {
      PsiFile file = e.getData(DataKeys.PSI_FILE);
      String categoryName = GrailsUtils.getCategoryName(file, module);
      if (categoryName != null && JavaPsiFacade.getInstance(module.getProject()).findClass(categoryName + "ControllerTests", file.getResolveScope()) == null) {
        return;
      }
    }

    presentation.setEnabled(false);
    presentation.setVisible(false);
  }


  @Nullable
  public static VirtualFile createDirectory(final String dir) throws IOException {
    final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(dir);
    if (file == null) {
      int pos = dir.lastIndexOf('/');
      if (pos < 0) return null;
      VirtualFile parent = createDirectory(dir.substring(0, pos));
      if (parent == null) return null;
      final String dirName = dir.substring(pos + 1);
      return parent.createChildDirectory(LocalFileSystem.getInstance(), dirName);
    }
    return file;
  }
}
