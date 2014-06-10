package com.intellij.spring.model.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.util.xml.ui.actions.generate.GenerateDomElementAction;
import com.intellij.util.xml.ui.actions.generate.GenerateDomElementProvider;

import javax.swing.*;

public class GenerateSpringDomElementAction extends GenerateDomElementAction {

  public GenerateSpringDomElementAction(final GenerateDomElementProvider provider) {
    super(provider);
  }
  
  public GenerateSpringDomElementAction(final GenerateDomElementProvider provider, Icon icon) {
    super(provider);
    getTemplatePresentation().setIcon(icon);
  }

  public boolean isValidForFile(Project project, Editor editor, PsiFile file) {
    return super.isValidForFile(project, editor, file) &&
           file instanceof XmlFile &&
           SpringManager.getInstance(project).isSpringBeans((XmlFile)file);
  }
}