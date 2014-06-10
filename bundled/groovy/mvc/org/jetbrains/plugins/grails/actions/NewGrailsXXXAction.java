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

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NewGrailsXXXAction extends AnAction implements DumbAware {

  protected NewGrailsXXXAction(String text, String description, Icon icon) {
    super(text, description, icon);
  }

  protected static String canonicalize(String name) {
    if (name == null || name.length() == 0) return "";
    final int i = name.lastIndexOf(".");
    if (i > 0 && i < name.length() - 1) {
      final String tail = name.substring(i + 1);
      final String head = name.substring(0, i);
      return (head + "/" + StringUtil.capitalize(tail)).replace('.', '/');
    }
    name = name.replace('.', '/');
    return StringUtil.capitalize(name);
  }

  public void actionPerformed(final AnActionEvent e) {
    final Module module = e.getData(DataKeys.MODULE);
    if (module == null) return;

    final Project project = module.getProject();

    final DataContext dataContext = e.getDataContext();

    final IdeView view = DataKeys.IDE_VIEW.getData(dataContext);
    if (view == null) return;


    final String inputTitle = "New " + e.getPresentation().getText();
    String name =
      Messages.showInputDialog(project, "Name:", inputTitle, Messages.getQuestionIcon(), "", new MyInputValidator(project, inputTitle));
    if (name == null) return;

    final PsiDirectory[] dirs = view.getDirectories();
    final PsiDirectory dir = dirs.length == 1 ? dirs[0] : null;
    if (dir != null) {
      PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(dir);
      if (aPackage != null) {
        String qName = aPackage.getQualifiedName();
        if (qName.length() > 0) {
          name = qName + "." + name;
        }
      }
    }

    if (checkExisting(module, name)) return;

    doAction(module, project, name);
  }

  protected void doAction(final Module module, final Project project, final String name) {
    ProcessBuilder pb = GrailsFramework.INSTANCE.createCommand(module, false, getCommand(), name);
    MvcConsole.getInstance(project).executeProcess(module, pb, new Runnable() {
      public void run() {
        List<VirtualFile> files = findExistingGeneratedFiles(module, name);
        if (files.size() > 0) FileEditorManager.getInstance(project).openFile(files.get(0), true);
      }
    }, true);
  }

  protected abstract String getCommand();

  @Nullable
  protected abstract VirtualFile getTargetDirectory(@NotNull Module module);

  private boolean isEnabled(AnActionEvent e) {
    final Module module = e.getData(DataKeys.MODULE);
    if (module == null ||
        !GrailsFramework.INSTANCE.hasSupport(module) ||
        ModuleRootManager.getInstance(module).getSdk() == null){
      return false;
    }

    if (e.getData(DataKeys.IDE_VIEW) == null) {
      return false;
    }

    if (e.getData(DataKeys.MODULE_CONTEXT) == module) {
      return true;
    }

    VirtualFile vfile = e.getData(DataKeys.VIRTUAL_FILE);
    if (vfile == null) {
      return false;
    }
    if (!vfile.isDirectory()) {
      vfile = vfile.getParent();
      if (vfile == null) {
        return false;
      }
    }

    final VirtualFile targetDirectory = getTargetDirectory(module);
    if (targetDirectory == null) {
      return false;
    }

    return VfsUtil.isAncestor(targetDirectory, vfile, false) || VfsUtil.isAncestor(vfile, targetDirectory, false);
  }

  public void update(AnActionEvent e) {
    final Presentation presentation = e.getPresentation();

    final boolean enabled = isEnabled(e);
    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
    presentation.setWeight(Presentation.HIGHER_WEIGHT);
  }

  private class MyInputValidator implements InputValidator {
    private final Project myProject;
    private final String myInputTitle;

    private MyInputValidator(Project project, String inputTitle) {
      myProject = project;
      myInputTitle = inputTitle;
    }

    public boolean canClose(String inputString) {
      if (!checkInput(inputString)) return false;
      if (!isValidIdentifier(inputString, myProject)) {
        Messages.showErrorDialog(myProject, GrailsBundle.message("invalid.name.entered"), myInputTitle);
        return false;
      }
      return true;
    }

    public boolean checkInput(String inputString) {
      return inputString.length() != 0;
    }
  }

  protected boolean isValidIdentifier(final String inputString, final Project project) {
    return JavaPsiFacade.getInstance(project).getNameHelper().isIdentifier(inputString);
  }

  //return if canceled
  protected boolean checkExisting(Module module, String name) {
    final List<VirtualFile> existing = findExistingGeneratedFiles(module, name);
    if (existing.isEmpty()) {
      return false;
    }
    StringBuilder message = new StringBuilder();
    message.append(GrailsBundle.message("generate.dlg.exist")).append("\n");
    for (VirtualFile file : existing) {
      message.append("   ").append(file.getPath()).append("\n");
    }

    message.append(GrailsBundle.message("generate.dlg.overwrite"));

    return Messages.showYesNoDialog(module.getProject(), message.toString(), "Conflict", Messages.getQuestionIcon()) != 0;

  }

   protected List<VirtualFile> findExistingGeneratedFiles(Module module, String name) {
    List<String> list = getGeneratedFileNames(name);
    ArrayList<VirtualFile> files = new ArrayList<VirtualFile>();
    VirtualFile dir = GrailsUtils.findGrailsAppRoot(module);
    if (dir == null) return Collections.emptyList();
    for (String fileName : list) {
      VirtualFile file = dir.findFileByRelativePath(fileName);
      if (file != null) files.add(file);
    }
    return files;
  }

  private List<String> getGeneratedFileNames(String name) {
    ArrayList<String> names = new ArrayList<String>();
    fillGeneratedNamesList(name, names);
    return names;
  }

  protected abstract void fillGeneratedNamesList(String name, List<String> names);

}
