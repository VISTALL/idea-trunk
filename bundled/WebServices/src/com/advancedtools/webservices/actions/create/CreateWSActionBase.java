package com.advancedtools.webservices.actions.create;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.FileUtils;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Maxim
 */
abstract class CreateWSActionBase extends CreateElementActionBase {
  public CreateWSActionBase(String textKey, String descriptionKey, Icon icon) {
    super(WSBundle.message(textKey), WSBundle.message(descriptionKey), icon);
  }

  protected String getErrorTitle() {
    return WSBundle.message("title.cannot.create.filetype", getKindName());
  }

  protected String getCommandName() {
    return WSBundle.message("command.name.create.new.file", getKindName());
  }

  protected String getActionName(PsiDirectory psiDirectory, String s) {
    return WSBundle.message("progress.creating.filetype.in.directory", getKindName(), s, psiDirectory.getName());
  }

  protected abstract String getKindName();

  @NotNull
  public PsiElement[] create(String className, PsiDirectory psiDirectory) throws Exception {
    final PsiFile file = psiDirectory.createFile(className + ".java");
    String text;

    final String packageQName = EnvironmentFacade.getInstance().getPackageFor(psiDirectory).getQualifiedName();

    text = buildText(packageQName, className);

    final VirtualFile virtualFile = file.getVirtualFile();
    FileUtils.saveText(virtualFile, text);

    final FileEditor[] fileEditors = FileEditorManager.getInstance(psiDirectory.getProject()).openFile(virtualFile, true);
    Editor editor = null;

    for(FileEditor fe:fileEditors) {
      if (fe instanceof TextEditor) {
        editor = ((TextEditor)fe).getEditor();
        break;
      }
    }

    createAdditionalFiles(className, packageQName, psiDirectory, editor, virtualFile);

    return new PsiElement[] { file };
  }

  protected void createAdditionalFiles(String className, String packageQName, PsiDirectory psiDirectory, Editor editor, VirtualFile vfile) throws Exception {

  }

  protected abstract String buildText(String packageNamen, String className);

  @NotNull
  protected PsiElement[] invokeDialog(Project project, PsiDirectory psiDirectory) {
    CreateElementActionBase.MyInputValidator validator = new MyInputValidator(project, psiDirectory);
    Messages.showInputDialog(
      project,
      WSBundle.message("prompt.enter.new.0",getKindName()),
      WSBundle.message("title.new.filetype",getKindName()),
      Messages.getQuestionIcon(),
      "",
      validator
    );
    return validator.getCreatedElements();
  }


  public void update(final AnActionEvent e) {
    super.update(e);

    DataContext dataContext = e.getDataContext();
    Project project = (Project)dataContext.getData(DataConstants.PROJECT);
    Presentation presentation = e.getPresentation();

    if (presentation.isEnabled()) {
      ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
      PsiDirectory[] dirs = ((IdeView)dataContext.getData(DataConstants.IDE_VIEW)).getDirectories();

      for(PsiDirectory dir:dirs) {
        if (fileIndex.isInSourceContent(dir.getVirtualFile()) && EnvironmentFacade.getInstance().getPackageFor(dir) != null) return;
      }

      presentation.setEnabled(false);
      presentation.setVisible(false);
    }
  }

  protected void checkBeforeCreate(String s, PsiDirectory psiDirectory) throws IncorrectOperationException {
    EnvironmentFacade.getInstance().checkCreateClass(psiDirectory, s);
  }

}
