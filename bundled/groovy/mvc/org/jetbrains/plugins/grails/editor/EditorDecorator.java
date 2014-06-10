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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class EditorDecorator implements ProjectComponent, FileEditorManagerListener {
  private final Project project;
  private final FileEditorManager myEditorManager;

  public EditorDecorator(Project project) {
    this.project = project;
    myEditorManager = FileEditorManager.getInstance(project);
    project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return "GrailsEditorDecorator";
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  public void fileOpened(FileEditorManager source, VirtualFile file) {
    final Module module = ModuleUtil.findModuleForFile(file, project);
    if (module == null) return;

    VirtualFile appRoot = GrailsUtils.findGrailsAppRoot(module);
    if (appRoot == null) return;

    if (file.getFileType() == GroovyFileType.GROOVY_FILE_TYPE) {
      decorateGroovyFile(file, appRoot);
    }
    else if (file.getFileType() == GspFileType.GSP_FILE_TYPE || "jsp".equals(file.getExtension())) {
      decorateGspFile(file, appRoot);
    }
  }

  private void decorateGroovyFile(VirtualFile file, VirtualFile appRoot) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof GroovyFile)) return;

    String shortName = file.getNameWithoutExtension();

    if (StringUtil.isEmpty(shortName) || !Character.isUpperCase(shortName.charAt(0))) return;

    String relativePath = ((GroovyFile) psiFile).getPackageName().replace('.', '/') + "/" + file.getName();

    if (file == VfsUtil.findRelativeFile("grails-app/domain/" + relativePath, appRoot)) {
      decorate(shortName, appRoot, file, null);
      return;
    }

    if (shortName.endsWith("Controller") && file == VfsUtil.findRelativeFile("grails-app/controllers/" + relativePath, appRoot)) {
      decorate(StringUtil.trimEnd(shortName, "Controller"), appRoot, file, null);
      return;
    }

    if (shortName.endsWith("Tests") && file == VfsUtil.findRelativeFile("test/integration/" + relativePath, appRoot) ||
        file == VfsUtil.findRelativeFile("test/unit/" + relativePath, appRoot)) {
      decorate(StringUtil.trimEnd(StringUtil.trimEnd(shortName, "Tests"), "Controller"), appRoot, file, null);
    }
  }

  private void decorateGspFile(VirtualFile file, VirtualFile appRoot) {
    VirtualFile parent = file.getParent();
    if (parent == null) return;

    String parentName = parent.getName();
    if (parent == VfsUtil.findRelativeFile("grails-app/views/" + parentName, appRoot)) {
      decorate(StringUtil.capitalize(parentName), appRoot, file, file.getNameWithoutExtension());
    }
  }

  private void decorate(final String artifactName, final VirtualFile appRootDir, VirtualFile file, @Nullable String gspName) {
    JPanel panel = new JPanel();

    panel.add(new OpenDomainClassAction(artifactName, appRootDir));
    panel.add(new OpenControllerAction(artifactName, appRootDir, gspName));
    panel.add(new OpenViewsAction(artifactName, appRootDir));
    panel.add(new OpenTestsAction(artifactName, appRootDir));

    panel.setBorder(BorderFactory.createEtchedBorder());
    panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
    final FileEditor[] editors = myEditorManager.getEditors(file);
    if (editors.length != 0) {
      myEditorManager.addTopComponent(editors[0], panel);
    }
  }

  private static void addChildrenRecursively(final String suffix, final List<VirtualFile> list, final VirtualFile dir) {
    for (final VirtualFile child : dir.getChildren()) {
      if (child.isDirectory()) {
        addChildrenRecursively(suffix, list, child);
      }
      else if (nameEnds(child, suffix)) {
        list.add(child);
      }
    }
  }

  private static boolean nameEnds(VirtualFile virtualFile, String suffix) {
    final String name = virtualFile.getName();
    if (SystemInfo.isFileSystemCaseSensitive) {
      return name.endsWith(suffix);
    }

    return name.toLowerCase().endsWith(suffix.toLowerCase());
  }

  public void fileClosed(FileEditorManager source, VirtualFile file) {
  }

  public void selectionChanged(FileEditorManagerEvent event) {
  }

  private class OpenControllerAction extends ChooseFileAction {
    private final String myName;
    @Nullable private final String myActionName;

    public OpenControllerAction(String name, VirtualFile moduleFileDir, @Nullable String actionName) {
      super(name + "Controller" + (actionName == null ? "" : ":" + actionName), moduleFileDir, GrailsIcons.CONTROLLER, true);
      myName = name;
      myActionName = actionName;
    }

    protected List<VirtualFile> getFilesToNavigate() {
      List<VirtualFile> toNavigate = new ArrayList<VirtualFile>();
      addFilesInDir(toNavigate, "grails-app/controllers/", myName + "Controller.groovy");
      return toNavigate;
    }

    @Override
    protected void navigate(VirtualFile file) {
      if (myActionName != null) {
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile instanceof GroovyFile) {
          final String controllerName = myName + "Controller";
          for (final PsiClass psiClass : ((GroovyFile)psiFile).getClasses()) {
            if (controllerName.equals(psiClass.getName())) {
              final PsiMember member = findActionPsiElement(psiClass, myActionName);
              if (member != null) {
                member.navigate(true);
                return;
              }
            }
          }
        }
      }

      super.navigate(file);
    }

    @Nullable
    private PsiMember findActionPsiElement(PsiClass controllerClass, String actionName) {
      final PsiField field = controllerClass.findFieldByName(actionName, true);
      if (field != null) {
        return field;
      }
      for (PsiMethod method : controllerClass.findMethodsByName(actionName, true)) {
        if (method.getParameterList().getParametersCount() == 0) {
          return method;
        }
      }
      return null;
    }

    @Override
    protected void addGenerateAction(DefaultActionGroup group) {
      addGenerateAction(group, "Generate controller", new GenerateControllerAction());
    }

  }

  private class OpenTestsAction extends ChooseFileAction {
    private final String myName;

    public OpenTestsAction(String name, VirtualFile moduleFileDir) {
      super("Tests", moduleFileDir, GrailsIcons.GRAILS_TEST_RUN_CONFIGURATION, false);
      myName = name;
    }

    protected List<VirtualFile> getFilesToNavigate() {
      final ArrayList<VirtualFile> files = new ArrayList<VirtualFile>();
      final String plainSuffix = myName + "Tests.groovy";
      final String controllerSuffix = myName + "ControllerTests.groovy";
      addFilesInDir(files, "test/unit/", plainSuffix);
      addFilesInDir(files, "test/unit/", controllerSuffix);
      addFilesInDir(files, "test/integration/", plainSuffix);
      addFilesInDir(files, "test/integration/", controllerSuffix);
      return files;
    }

    @Override
    protected void addGenerateAction(DefaultActionGroup group) {
      addGenerateAction(group, "Generate " + myName + "Tests... (unit)", new GenerateTestsAction(false));
      addGenerateAction(group, "Generate " + myName + "Tests... (integration)", new GenerateTestsAction(true));
      addGenerateAction(group, "Generate " + myName + "ControllerTests... (unit)", new GenerateControllerTestsAction(false));
      addGenerateAction(group, "Generate " + myName + "ControllerTests... (integration)", new GenerateControllerTestsAction(true));
    }
  }

  private class OpenViewsAction extends ChooseFileAction {
    private final String myName;

    public OpenViewsAction(String name, VirtualFile appRoot) {
      super(name + " Views", appRoot, GrailsIcons.GSP_FILE_TYPE, false);
      myName = name;
    }

    protected Collection<VirtualFile> getFilesToNavigate() {
      VirtualFile views = VfsUtil.findRelativeFile("grails-app/views/" + StringUtil.decapitalize(myName), appRoot);
      if (views == null) return Collections.emptyList();

      TreeMap<String, VirtualFile> viewsMap = new TreeMap<String, VirtualFile>();
      for (VirtualFile child : views.getChildren()) {
        final String name = child.getName().toLowerCase();
        if (name.endsWith(".gsp") || name.endsWith(".jsp")) {
          viewsMap.put(getPresentableText(child), child);
        }
      }
      return viewsMap.values();

    }

    @Override
    protected void addGenerateAction(DefaultActionGroup group) {
      addGenerateAction(group, "Generate views", new GenerateViewsAction());
    }

  }

  class OpenDomainClassAction extends ChooseFileAction {
    private final String myName;

    public OpenDomainClassAction(String name, VirtualFile moduleFileDir) {
      super(name, moduleFileDir, GrailsIcons.DOMAIN_CLASS, true);
      myName = name;
    }

    protected List<VirtualFile> getFilesToNavigate() {
      List<VirtualFile> toNavigate = new ArrayList<VirtualFile>();
      addFilesInDir(toNavigate, "grails-app/domain/", myName + ".groovy");
      return toNavigate;
    }

    @Override
    protected void addGenerateAction(DefaultActionGroup group) {
      addGenerateAction(group, "Generate domain class", new GenerateDomainClassAction());
    }
  }

  private abstract class ChooseFileAction extends JLabel {
    protected final VirtualFile appRoot;
    private final boolean myOpenSingleFile;

    public ChooseFileAction(String actionName, VirtualFile appRoot, final Icon icon, boolean openSingleFile) {
      super(actionName);
      this.appRoot = appRoot;
      myOpenSingleFile = openSingleFile;

      setForeground(Color.BLACK);
      setOpaque(true);
      setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
      setIcon(icon);
      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          actionPerformed();
        }

        public void mouseEntered(MouseEvent e) {
          setBackground(getBackground().darker());
        }

        public void mouseExited(MouseEvent e) {
          setBackground(getParent().getBackground());
        }
      });

      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    protected abstract void addGenerateAction(DefaultActionGroup group);

    private void actionPerformed() {
      setBackground(getParent().getBackground());
      final Collection<VirtualFile> files = getFilesToNavigate();
      if (files.size() == 1 && myOpenSingleFile) {
        navigate(files.iterator().next());
        return;
      }

      final DefaultActionGroup actionGroup = new DefaultActionGroup();

      for (final VirtualFile file : files) {
        final AnAction action = new AnAction() {
          public void actionPerformed(final AnActionEvent e) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                if (file.isValid() && !project.isDisposed()) {
                  navigate(file);
                }
              }
            });
          }
        };
        final Presentation presentation = action.getTemplatePresentation();
        presentation.setText(getPresentableText(file));
        presentation.setIcon(file.getIcon());
        actionGroup.add(action);
      }

      if (!files.isEmpty()) {
        actionGroup.addSeparator();
      }
      addGenerateAction(actionGroup);
      JBPopupFactory.getInstance().createActionGroupPopup(null, actionGroup, DataManager.getInstance().getDataContext(this),
                                                          JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false).showUnderneathOf(this);
    }

    protected void navigate(VirtualFile file) {
      new OpenFileDescriptor(project, file).navigate(true);
    }

    protected String getPresentableText(VirtualFile file) {
      String actionText;
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (psiFile instanceof GroovyFile) {
        final String name = ((GroovyFile)psiFile).getPackageName();
        actionText = (name.length() > 0 ? name + "." : "") + file.getNameWithoutExtension();
      }
      else {
        actionText = file.getName();
      }
      return actionText;
    }

    protected final void addFilesInDir(List<VirtualFile> to, String path, final String suffix) {
      final VirtualFile dir = VfsUtil.findRelativeFile(path, appRoot);
      if (dir != null) {
        addChildrenRecursively(suffix, to, dir);
      }
    }

    protected abstract Collection<VirtualFile> getFilesToNavigate();

    protected void addGenerateAction(DefaultActionGroup group, String text, AnAction anAction) {
      anAction.getTemplatePresentation().setText(text);
      anAction.getTemplatePresentation().setIcon(getIcon());
      group.add(anAction);
    }
  }

}
