package com.intellij.seam.impl.model.xml;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.SeamDomModel;
import com.intellij.seam.model.xml.SeamDomModelFactory;
import com.intellij.seam.model.xml.SeamDomModelManager;
import com.intellij.seam.model.xml.components.SeamComponents;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.converters.values.GenericDomValueConvertersRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SeamDomModelManagerImpl extends SeamDomModelManager {
  private final SeamDomModelFactory myModelFactory;
  private final GenericDomValueConvertersRegistry myValueProvidersRegistry;
  private final DomManager myDomManager;

  public SeamDomModelManagerImpl(Project project) {
    myDomManager = DomManager.getDomManager(project);

    myModelFactory = new SeamDomModelFactory(project);
    myValueProvidersRegistry = new GenericDomValueConvertersRegistry();

    myValueProvidersRegistry.registerDefaultConverters();
  }

  public boolean isSeamComponents(@NotNull final XmlFile file) {
    return myDomManager.getFileElement(file, SeamComponents.class) != null;
  }

  @Nullable
  public SeamDomModel getSeamModel(@NotNull final XmlFile file) {
    return myModelFactory.getModelByConfigFile(file);
  }

  @Nullable
  public SeamDomModel getCombinedSeamModel(@NotNull final Module module) {
    return myModelFactory.getCombinedModel(module);
  }

  public List<SeamDomModel> getAllModels(@NotNull final Module module) {
    return myModelFactory.getAllModels(module);
  }

  public GenericDomValueConvertersRegistry getValueConvertersRegistry() {
    return myValueProvidersRegistry;
  }
}
