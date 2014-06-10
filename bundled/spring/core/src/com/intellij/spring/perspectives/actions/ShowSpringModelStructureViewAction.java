package com.intellij.spring.perspectives.actions;

import com.intellij.ide.util.FileStructureDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.structure.SpringStructureViewModel;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.perspectives.graph.SpringBeansDependencyGraphComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShowSpringModelStructureViewAction extends AnAction {
  public void actionPerformed(final AnActionEvent e) {
    final SpringBeansDependencyGraphComponent editorComponent = getSpringBeansStructureEditorComponent(e);
    if (editorComponent != null) {
      final XmlFile xmlFile = editorComponent.getXmlFile();
      final Project project = xmlFile.getProject();

      final SpringModel model = editorComponent.getDataModel().getModel();
      if (model != null) {
        final SpringStructureViewModel structureViewModel =
          new SpringStructureViewModel(xmlFile, editorComponent.getNavigationProvider(), false);

        FileStructureDialog dialog = new FileStructureDialog(structureViewModel, null, project, null, structureViewModel, false) {
          protected PsiFile getPsiFile(final Project project) {
            return xmlFile;
          }

          @Nullable
          protected PsiElement getCurrentElement(@Nullable final PsiFile psiFile) {
            final List<SpringBeanPointer> selectedBeans = editorComponent.getSelectedBeans();
            return selectedBeans.size() > 0 ? selectedBeans.get(0).getPsiElement() : null;
          }
        };
        dialog.setTitle(SpringBundle.message("spring.beans"));
        dialog.show();
      }
    }
  }

  public void update(final AnActionEvent e) {
    e.getPresentation().setEnabled(getSpringBeansStructureEditorComponent(e) != null);
  }

  @Nullable
  private static SpringBeansDependencyGraphComponent getSpringBeansStructureEditorComponent(final AnActionEvent e) {
    return (SpringBeansDependencyGraphComponent)e.getDataContext()
      .getData(SpringBeansDependencyGraphComponent.SPRING_BEAN_DEPENDENCIES_GRAPH_COMPONENT);
  }
}
