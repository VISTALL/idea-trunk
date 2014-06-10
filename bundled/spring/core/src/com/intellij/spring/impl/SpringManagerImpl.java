/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.javaee.model.xml.impl.RootBaseImpl;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiType;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.SpringModelProvider;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.converters.CustomConverterRegistry;
import com.intellij.spring.model.values.converters.*;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.converters.values.GenericDomValueConvertersRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringManagerImpl extends SpringManager {

  private final SpringModelFactory myModelFactory;
  private final GenericDomValueConvertersRegistry myValueProvidersRegistry;
  private final CustomConverterRegistry myCustomConvertersRegistry;

  public SpringManagerImpl(Project project) {
    myModelFactory = new SpringModelFactory(project);
    myValueProvidersRegistry = new GenericDomValueConvertersRegistry();
    myCustomConvertersRegistry = new CustomConverterRegistry();

    registerValueConverters();
  }

  private void registerValueConverters() {
    myValueProvidersRegistry
      .registerConverter(new PlaceholderPropertiesConverter(), new PlaceholderPropertiesConverter.PlaceholderPropertiesCondition());

    myValueProvidersRegistry.registerConverter(new SpringBooleanValueConverter(false), PsiType.BOOLEAN);
    myValueProvidersRegistry.registerConverter(new SpringBooleanValueConverter(true), Boolean.class);

    myValueProvidersRegistry.registerNumberValueConverters();
    myValueProvidersRegistry.registerCharacterConverter();

    myValueProvidersRegistry.registerClassValueConverters();

    myValueProvidersRegistry.registerConverter(new ResourceValueConverter(), new ResourceValueConverter.ResourceValueConverterCondition());
    myValueProvidersRegistry.registerConverter(new FieldRetrievingFactoryBeanConverter(),
                                               new FieldRetrievingFactoryBeanConverter.FactoryClassAndPropertyCondition());
    myValueProvidersRegistry.registerConverter(new EnumValueConverter(), new EnumValueConverter.TypeCondition());
  }

  @NotNull
  public List<SpringModel> getAllModels(@NotNull Module module) {
    final List<SpringModel> list = myModelFactory.getAllModels(module);
    for (final SpringModel springModel : list) {
      for (final DomFileElement<Beans> element : springModel.getRoots()) {
        ((RootBaseImpl)element.getRootElement()).registerDomModule(module);
      }
    }
    return list;
  }

  @Nullable
  public SpringModel getCombinedModel(final Module module) {
    return myModelFactory.getCombinedModel(module);
  }

  public GenericDomValueConvertersRegistry getValueProvidersRegistry() {
    return myValueProvidersRegistry;
  }

  public CustomConverterRegistry getCustomConverterRegistry() {
    return myCustomConvertersRegistry;
  }

  @Nullable
  public SpringModel createModel(final SpringFileSet set, final Module module) {
    return myModelFactory.createModel(set, module);
  }

  public boolean isSpringBeans(@NotNull XmlFile file) {
    return DomManager.getDomManager(file.getProject()).getFileElement(file, Beans.class) != null;
  }

  @Nullable
  public SpringModel getSpringModelByFile(@NotNull XmlFile file) {
    return myModelFactory.getModelByConfigFile(file);
  }

  @Nullable
  public SpringModel getLocalSpringModel(@NotNull XmlFile file) {
    final DomFileElement<Beans> beans = myModelFactory.getDomRoot(file);
    return beans == null ? null : new SpringModelImpl(beans, Collections.singleton(file), ModuleUtil.findModuleForPsiElement(file), null);
  }


  @NotNull
  public List<SpringFileSet> getProvidedModels(@NotNull SpringFacet facet) {
    List<SpringFileSet> result = null;

    for (SpringModelProvider modelProvider : Extensions.getExtensions(SpringModelProvider.EP_NAME)) {
      final List<SpringFileSet> list = modelProvider.getFilesets(facet);
      if (list.size() > 0) {
        if (result == null) {
          result = list;
        }
        else {
          result.addAll(list);
        }
      }
    }
    return result == null ? Collections.<SpringFileSet>emptyList() : result;
  }

  @NotNull
  public Set<SpringFileSet> getAllSets(final @NotNull SpringFacet facet) {
    final Set<SpringFileSet> fileSets = new HashSet<SpringFileSet>(facet.getConfiguration().getFileSets());
    final List<SpringFileSet> providedModels = getProvidedModels(facet);
    fileSets.addAll(providedModels);
    return fileSets;
  }
}
