/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation;

import com.intellij.beanValidation.facet.BeanValidationFacetType;
import com.intellij.beanValidation.highlighting.BvConstraintMappingsInspection;
import com.intellij.beanValidation.highlighting.ConstraintValidatorCreator;
import com.intellij.beanValidation.highlighting.MinMaxValuesInspection;
import com.intellij.beanValidation.highlighting.BvConfigDomInspection;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.ui.libraries.versions.LibrariesConfigurationManager;
import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationApplicationComponent
  implements ApplicationComponent, FileTemplateGroupDescriptorFactory, InspectionToolProvider, Disposable {

  private Map<LibraryVersionInfo, List<LibraryInfo>> myLibraries;

  public static BeanValidationApplicationComponent getInstance() {
    return ApplicationManager.getApplication().getComponent(BeanValidationApplicationComponent.class);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {
    FacetTypeRegistry.getInstance().registerFacetType(BeanValidationFacetType.INSTANCE);
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  public Class[] getInspectionClasses() {
    return new Class[] {
      ConstraintValidatorCreator.class,
      MinMaxValuesInspection.class,
      BvConstraintMappingsInspection.class,
      BvConfigDomInspection.class
    };
  }

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    return new FileTemplateGroupDescriptor(BVBundle.message("bv.framework.name"), BVIcons.BEAN_VALIDATION_ICON);
  }

  public Map<LibraryVersionInfo, List<LibraryInfo>> getLibraries() {
    if (myLibraries == null) {
      myLibraries = LibrariesConfigurationManager.getLibraries(getLibrariesUrl("/resources/versions/libraries_hibernate.xml"));
    }
    return myLibraries;
  }

  private static URL getLibrariesUrl(String url) {
    return BeanValidationApplicationComponent.class.getResource(url);
  }
}

