package com.intellij.spring.model.xml.custom;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.spring.SpringBundle;
import org.jetbrains.annotations.NotNull;

public class SpringCustomBeanRenameHandler implements RenameHandler {

  public boolean isAvailableOnDataContext(DataContext dataContext) {
    return false;
  }

  public boolean isRenaming(DataContext dataContext) {
    PsiElement element = LangDataKeys.PSI_ELEMENT.getData(dataContext);
    return element instanceof CustomBeanFakePsiElement;
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final PsiElement element = LangDataKeys.PSI_ELEMENT.getData(dataContext);
    doInvoke(project, editor, element);
  }

  private static void doInvoke(final Project project, final Editor editor, final PsiElement element) {
    final XmlAttribute idAttribute = ((CustomBeanFakePsiElement)element).getBean().getIdAttribute();
    if (idAttribute == null) {
      final int i = Messages
          .showOkCancelDialog(project, SpringBundle.message("custom.bean.no.id"), SpringBundle.message("custom.bean.no.id.title"), Messages.getWarningIcon());
      if (i != 0) return;
    }

    PsiElementRenameHandler.invoke(element, project, element, editor);
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
  }

}