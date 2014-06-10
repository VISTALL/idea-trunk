package com.intellij.seam.el.typedHandler;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.openapi.project.Project;
import com.intellij.seam.model.xml.SeamDomModelManager;
import com.intellij.seam.model.xml.PageflowDomModelManager;
import com.intellij.seam.model.xml.PagesDomModelManager;
import com.intellij.seam.model.xml.pages.Page;
import com.intellij.util.xml.DomManager;

public class SeamElXmlTypedHandler extends BasicSeamElTypedHandler {
  protected boolean isElContainerFile(final PsiFile originalFile) {
    return isSeamFacetDetected(originalFile) && isSeamConfig(originalFile);
  }

  private static boolean isSeamConfig(final PsiFile originalFile) {
    if (originalFile instanceof XmlFile) {
      final XmlFile xmlFile = (XmlFile)originalFile;
      final Project project = xmlFile.getProject();

      return SeamDomModelManager.getInstance(project).isSeamComponents(xmlFile) ||
             PageflowDomModelManager.getInstance(project).isPageflow(xmlFile) ||
             DomManager.getDomManager(project).getFileElement(xmlFile, Page.class) != null ||
             PagesDomModelManager.getInstance(project).isPages(xmlFile);
    }

    return false;
  }
}
