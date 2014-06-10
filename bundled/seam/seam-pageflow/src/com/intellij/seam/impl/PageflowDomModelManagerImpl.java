package com.intellij.seam.impl;

import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.PageflowDomModelManager;
import com.intellij.seam.model.xml.PageflowModel;
import com.intellij.seam.model.xml.PageflowModelFactory;
import com.intellij.seam.model.xml.pageflow.PageflowDefinition;
import com.intellij.util.xml.DomManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowDomModelManagerImpl extends PageflowDomModelManager {
  private final PageflowModelFactory myModelFactory;
  private final DomManager myDomManager;

  public PageflowDomModelManagerImpl(final Project project, DomManager domManager) {
    myDomManager = domManager;
    myModelFactory = new PageflowModelFactory(project);
  }

  public boolean isPageflow(@NotNull final XmlFile file) {
    return myDomManager.getFileElement(file, PageflowDefinition.class) != null;
  }

  public PageflowModel getPageflowModel(@NotNull final XmlFile file) {
    return myModelFactory.getModelByConfigFile(file);
  }

  public List<PageflowModel> getAllModels(@NotNull final Module module) {
    return myModelFactory.getAllModels(module);
  }
}
