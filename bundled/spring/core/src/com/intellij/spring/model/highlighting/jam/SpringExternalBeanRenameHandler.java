package com.intellij.spring.model.highlighting.jam;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.refactoring.openapi.impl.JavaRenameRefactoringImpl;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.spring.model.jam.utils.SpringJamUtils;
import com.intellij.spring.model.jam.javaConfig.SpringJavaExternalBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SpringExternalBeanRenameHandler implements RenameHandler {

  public boolean isAvailableOnDataContext(DataContext dataContext) {
    return false;
  }

  public boolean isRenaming(DataContext dataContext) {
    return getExternalBean(dataContext) != null;
  }

  @Nullable
  private SpringJavaExternalBean getExternalBean(final DataContext dataContext) {
    PsiElement element = LangDataKeys.PSI_ELEMENT.getData(dataContext);

    return element instanceof PsiMethod ? SpringJamUtils.getExternalBean((PsiMethod)element) : null;
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final SpringJavaExternalBean externalBean = getExternalBean(dataContext);

    if (externalBean != null) {
      final ExternalBeanRenameDialog dialog = new ExternalBeanRenameDialog(externalBean, editor);
      dialog.show();
    }
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
  }

  public static void doRename(final SpringJavaExternalBean externalBean, final String newName, final boolean searchInComments) {
    final PsiMethod psiElement = externalBean.getPsiElement();

    if (psiElement == null) return;

    final RenameRefactoring rename = new JavaRenameRefactoringImpl(psiElement.getProject(), psiElement, newName, searchInComments, false);
    Set<PsiElement> psiElements = new HashSet<PsiElement>();
    for (SpringBaseBeanPointer springBean : SpringJamUtils.findExternalBeans(psiElement)) {
      final XmlTag tag = springBean.getSpringBean().getXmlTag();
      if (tag != null) {
        psiElements.add(tag);
        for (SpringJavaExternalBean javaExternalBean : SpringJamUtils.findExternalBeanReferences(springBean.getSpringBean())) {
          if (javaExternalBean.equals(externalBean)) continue;
          final PsiMethod psiMethod = javaExternalBean.getPsiElement();
          if (psiMethod != null) {
            psiElements.add(psiMethod);
          }
        }
      }
    }
    for (PsiElement element : psiElements) {
      rename.addElement(element, newName);
    }
    rename.run();
  }

  private static class ExternalBeanRenameDialog extends RenameDialog {

    private final SpringJavaExternalBean myExternalBean;

    protected ExternalBeanRenameDialog(SpringJavaExternalBean externalBean, final Editor editor) {
      super(externalBean.getPsiElement().getProject(), externalBean.getPsiElement(), null, editor);
      myExternalBean = externalBean;
    }

    protected void doAction() {
      final String newName = getNewName();
      final boolean searchInComments = isSearchInComments();
      doRename(myExternalBean, newName, searchInComments);
      close(DialogWrapper.OK_EXIT_CODE);
    }

  }
}
