package com.intellij.spring.model.actions.patterns;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstarctSpringConfigActionGroup extends DefaultActionGroup {

  public AbstarctSpringConfigActionGroup() {
    setPopup(true);
  }

  public void update(final AnActionEvent event) {
    Presentation presentation = event.getPresentation();
    DataContext dataContext = event.getDataContext();

    Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    if (project == null || editor == null) {
      presentation.setVisible(false);
      return;
    }

    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    final boolean enabled =
      file instanceof XmlFile && SpringManager.getInstance(project).isSpringBeans((XmlFile)file) && isInsideRootTag(editor, (XmlFile)file);

    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
    if (enabled) {
      event.getPresentation().setText(getDescription());
      event.getPresentation().setIcon(getIcon());
    }
  }

  private static boolean isInsideRootTag(final Editor editor, final XmlFile xmlFile) {
    XmlDocument document = xmlFile.getDocument();
    if (document != null) {
      XmlTag tag = document.getRootTag();
      if (tag != null) {
        TextRange textRange = tag.getTextRange();
        if (textRange.contains(editor.getCaretModel().getOffset()))  {
          return true;
        }
      }
    }
     return false;
  }

  @Nullable
  protected Icon getIcon() {
    return null;
  }

  protected abstract String getDescription();
}