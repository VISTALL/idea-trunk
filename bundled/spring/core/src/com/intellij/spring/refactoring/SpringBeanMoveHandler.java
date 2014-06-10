package com.intellij.spring.refactoring;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.move.MoveHandlerDelegate;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringValueHolder;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class SpringBeanMoveHandler extends MoveHandlerDelegate {
  @Override
  public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer) {
    for (PsiElement element : elements) {
      if (SpringUtils.findBeanFromPsiElement(element) == null) return false;
    }
    return super.canMove(elements, targetContainer);
  }


  public boolean isValidTarget(final PsiElement psiElement) {
    final SpringBean springBean = SpringUtils.findBeanFromPsiElement(psiElement);
    return springBean != null && (springBean.getParent() instanceof Beans || springBean.getParent() instanceof SpringValueHolder);
  }

  public boolean tryToMove(final PsiElement element, final Project project, final DataContext dataContext, @Nullable final PsiReference reference,
                           final Editor editor) {
    final SpringBean springBean = SpringUtils.findBeanFromPsiElement(element);
    if (springBean == null) {
      return false;
    }
    final PsiFile psiFile = element.getContainingFile();
    if (springBean.getParent() instanceof Beans) {  // top level
      new SpringBeanMoveDialog(project, springBean).show();
    } else {
      if (Messages.showYesNoDialog(project,
                                   SpringBundle.message("do.you.want.to.move.bean.to.the.top.level"),
                                   SpringBundle.message("move.bean.to.the.top.level"),
                                   Messages.getQuestionIcon()) == 0) {
        new WriteCommandAction.Simple(project, SpringBundle.message("move.bean.to.the.top.level"), psiFile) {
          protected void run() throws Throwable {
            SpringIntroduceBeanIntention.moveToTheTopLevel(project, editor, springBean);
          }
        }.execute();
      }
    }
    return true;
  }
}
