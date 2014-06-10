package com.intellij.spring.model.highlighting.jam;

import com.intellij.openapi.actionSystem.DataContext;
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
import com.intellij.spring.model.converters.SpringBeanUtil;
import com.intellij.spring.model.jam.javaConfig.SpringJavaExternalBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpringExternalBeanReferencesRenameHandler implements RenameHandler {

  public boolean isAvailableOnDataContext(DataContext dataContext) {
    return false;
  }

  public boolean isRenaming(DataContext dataContext) {
    final DomSpringBean springBean = SpringBeanUtil.getTargetSpringBean();
    if (springBean == null) return false;

    final List<SpringJavaExternalBean> list = SpringJamUtils.findExternalBeanReferences(springBean);

    return list.size() > 0;
  }



  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final DomSpringBean springBean = SpringBeanUtil.getTargetSpringBean();
    if (springBean != null) {
      final ExternalBeanRenameDialog dialog = new ExternalBeanRenameDialog(springBean, editor);
      dialog.show();
    }
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
  }

  public static void doRename(final DomSpringBean externalBean, final String newName, final boolean searchInComments) {
    final XmlTag psiElement = externalBean.getXmlTag();

    if (psiElement == null) return;

    final RenameRefactoring rename = new JavaRenameRefactoringImpl(psiElement.getProject(), psiElement, newName, searchInComments, false);

    Set<PsiElement> psiElements = new HashSet<PsiElement>();

    final List<SpringJavaExternalBean> list = SpringJamUtils.findExternalBeanReferences(externalBean);
    for (SpringJavaExternalBean springBean : list) {
      final PsiMethod psiMethod = springBean.getPsiElement();
      if (psiMethod != null) {
        psiElements.add(psiMethod);
        for (SpringBaseBeanPointer pointer : SpringJamUtils.findExternalBeans(psiMethod)) {
          if (!pointer.isReferenceTo(externalBean)) {
            ContainerUtil.addIfNotNull(pointer.getPsiElement(), psiElements);
          }
        }
      }
      for (PsiElement element : psiElements) {
        rename.addElement(element, newName);
      }
    }

    rename.run();
  }

  private static class ExternalBeanRenameDialog extends RenameDialog {

    private final DomSpringBean myExternalBean;

    protected ExternalBeanRenameDialog(DomSpringBean externalBean, final Editor editor) {
      super(editor.getProject(), externalBean.getXmlTag(), null, editor);
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
