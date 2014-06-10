package com.intellij.seam.model.xml;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.converters.values.GenericDomValueConvertersRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SeamDomModelManager {
  public static SeamDomModelManager getInstance(Project project) {
    return ServiceManager.getService(project, SeamDomModelManager.class);
  }

  public abstract boolean isSeamComponents(@NotNull XmlFile file);

  @Nullable
  public abstract SeamDomModel getSeamModel(@NotNull XmlFile file);

  @Nullable
  public abstract SeamDomModel getCombinedSeamModel(@NotNull Module module);

  public abstract List<SeamDomModel> getAllModels(@NotNull Module module);

  public abstract GenericDomValueConvertersRegistry getValueConvertersRegistry();
}
