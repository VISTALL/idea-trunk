package com.intellij.seam.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.PagesDomModelManager;
import com.intellij.seam.model.xml.PagesModel;
import com.intellij.seam.model.xml.PagesModelFactory;
import com.intellij.seam.model.xml.pages.Pages;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class PagesDomModelManagerImpl extends PagesDomModelManager {
 private final PagesModelFactory myModelFactory;
 private final DomManager myDomManager;

  public PagesDomModelManagerImpl(final Project project) {
    myModelFactory = new PagesModelFactory(project);
    myDomManager = DomManager.getDomManager(project);
  }

  public boolean isPages(@NotNull final XmlFile file) {
    return myDomManager.getFileElement(file, Pages.class) != null;
  }

  public PagesModel getPagesModel(@NotNull final XmlFile file) {
    return myModelFactory.getModelByConfigFile(file);
  }

  public List<PagesModel> getAllModels(@NotNull final Module module) {
    return myModelFactory.getAllModels(module);
  }
}

