/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.model.converters.CustomConverterRegistry;
import com.intellij.util.xml.converters.values.GenericDomValueConvertersRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class SpringManager {
  
  public static SpringManager getInstance(Project project) {
    return ServiceManager.getService(project, SpringManager.class);
  }

  public abstract boolean isSpringBeans(@NotNull XmlFile file);

  @Nullable
  public abstract SpringModel getSpringModelByFile(@NotNull XmlFile file);

  @Nullable
  public abstract SpringModel getLocalSpringModel(@NotNull XmlFile file);

  /**
   * Returns all models configured in given module.
   *
   * @param module a module.
   * @return list of models; empty list if no models found.
   * @see #getCombinedModel(Module)
   */
  @NotNull
  public abstract List<SpringModel> getAllModels(@NotNull Module module);

  /**
   * Returns result of merging all models configured in given module.
   *
   * @param module a module.
   * @return null if no models found.
   * @see #getAllModels(Module)
   */
  @Nullable
  public abstract SpringModel getCombinedModel(@Nullable Module module);

  /**
   * Returns models provided by all {@link SpringModelProvider}s.
   * @param facet
   * @return models provided by {@link SpringModelProvider}.
   * @see SpringModelProvider
   */
  @NotNull
  public abstract List<SpringFileSet> getProvidedModels(final @NotNull SpringFacet facet);

  /**
   * Returns all configured and provided file sets.
   * @param facet
   * @return all working file sets for the module.
   * @see #getProvidedModels(com.intellij.spring.facet.SpringFacet)
   */
  @NotNull
  public abstract Set<SpringFileSet> getAllSets(final @NotNull SpringFacet facet);

  public abstract GenericDomValueConvertersRegistry getValueProvidersRegistry();

  public abstract CustomConverterRegistry getCustomConverterRegistry();

  @Nullable
  public abstract SpringModel createModel(SpringFileSet set, Module module);
}