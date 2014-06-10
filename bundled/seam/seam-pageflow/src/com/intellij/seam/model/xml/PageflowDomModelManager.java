package com.intellij.seam.model.xml;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public abstract class PageflowDomModelManager {
  public static PageflowDomModelManager getInstance(Project project) {
    return ServiceManager.getService(project, PageflowDomModelManager.class);
  }

  public abstract boolean isPageflow(@NotNull final XmlFile file) ;

  public abstract PageflowModel getPageflowModel(@NotNull final XmlFile file);

   public abstract List<PageflowModel> getAllModels(@NotNull Module module);
}
