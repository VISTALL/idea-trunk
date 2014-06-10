package com.advancedtools.webservices.utils;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author maxim
 * Date: 21.01.2006
 */
public class BaseWSGenerateActionGroup extends DefaultActionGroup {
  public void update(AnActionEvent e) {
    super.update(e);

    updatePresentation(e);
  }

  static void updatePresentation(AnActionEvent e) {
    final Editor editor = (Editor)e.getDataContext().getData(DataConstants.EDITOR);
    final Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
    boolean enabled = false;

    if (editor != null && project != null) {
      final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

      if (psiFile != null) {
        final FileType fileType = psiFile.getFileType();
        Module module = psiFile.getVirtualFile() != null ?
          ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(psiFile.getVirtualFile()):
          null;

        if ( (fileType != StdFileTypes.JAVA && fileType != StdFileTypes.JSP && fileType != StdFileTypes.JSPX) ||
              ( EnvironmentFacade.isSelenaOrBetter() && module != null && EnvironmentFacade.getInstance().getEngineFromModule(module) == null &&
                !ActionPlaces.EDITOR_POPUP.equals(e.getPlace()) &&
                !ActionPlaces.MAIN_MENU.equals(e.getPlace())
              )
           ) {
          e.getPresentation().setVisible(false);
          return;
        }
        final PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());

        if (psiElement != null && psiElement.getLanguage() == StdLanguages.JAVA) {
          enabled = PsiTreeUtil.getParentOfType(psiElement, PsiCodeBlock.class) != null;
        }
      }

      e.getPresentation().setVisible(true);
      e.getPresentation().setEnabled(enabled);
    } else {
      e.getPresentation().setEnabled(false);
      e.getPresentation().setVisible(false);
    }
  }
}