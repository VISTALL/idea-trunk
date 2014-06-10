package com.intellij.spring.model.actions.patterns.frameworks;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public abstract class FrameworkIntegrationAction extends AnAction {

  public void actionPerformed(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();

    final Module module = getModule(dataContext);
    final Editor editor = getEditor(dataContext);
    final XmlFile xmlFile = getXmlFile(dataContext);

    if (module != null && editor != null && xmlFile != null) {
      generateSpringBeans(module, editor, xmlFile);
    }
  }

  public void update(final AnActionEvent event) {
    Presentation presentation = event.getPresentation();
    DataContext dataContext = event.getDataContext();

    XmlFile file = getXmlFile(dataContext);

    final boolean isSpringBeanFile = file != null && SpringManager.getInstance(getProject(dataContext)).isSpringBeans(file);

    final boolean enabled = isSpringBeanFile && accept(file);

    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
    if (enabled) {
      event.getPresentation().setText(getDescription());
      event.getPresentation().setIcon(getIcon());
    }
  }

  @Nullable
  protected static XmlFile getXmlFile(final DataContext dataContext) {
    return getXmlFile(getProject(dataContext), getEditor(dataContext));
  }

  @Nullable
  protected static XmlFile getXmlFile(final Project project, final Editor editor) {
    if (project == null || editor == null) return null;

    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    return psiFile instanceof XmlFile ? (XmlFile)psiFile : null;
  }

  @Nullable
  protected static Editor getEditor(final DataContext dataContext) {
    return PlatformDataKeys.EDITOR.getData(dataContext);
  }

  @Nullable
  protected static Project getProject(final DataContext dataContext) {
    return PlatformDataKeys.PROJECT.getData(dataContext);
  }

  @Nullable
  protected static Module getModule(final DataContext dataContext) {
    return LangDataKeys.MODULE.getData(dataContext);
  }

  protected boolean accept(final XmlFile file) {
    return acceptBeansByClassNames(file, getBeansClassNames());
  }

  protected String[] getBeansClassNames() {
    return new String[0];
  }

  private static boolean acceptBeansByClassNames(XmlFile file, String... classNames) {
    if (classNames.length == 0) return true;

    final SpringModel model = SpringManager.getInstance(file.getProject()).getSpringModelByFile(file);
    final Collection<? extends SpringBeanPointer> allBeans = model.getAllCommonBeans();
    for (String className : classNames) {
      if (SpringUtils.findBeansByClassName(allBeans, className).size() > 0) return false;
    }
    return true;
  }

  protected abstract void generateSpringBeans(final Module module, final Editor editor, final XmlFile xmlFile);

  @Nullable
  protected Icon getIcon() {
    return null;
  }

  protected abstract String getDescription();

}
