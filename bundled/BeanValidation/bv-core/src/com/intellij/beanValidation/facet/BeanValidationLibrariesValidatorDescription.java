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

package com.intellij.beanValidation.facet;

import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.beanValidation.resources.BVBundle;
import org.jetbrains.annotations.NonNls;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationLibrariesValidatorDescription extends FacetLibrariesValidatorDescription {
  private final BeanValidationFeaturesEditor myEditor;

  public BeanValidationLibrariesValidatorDescription(BeanValidationFeaturesEditor editor) {
    super(BVBundle.message("bv.framework.name"));
    myEditor = editor;
  }

  @NonNls
  public String getDefaultLibraryName() {
    LibraryVersionInfo libraryVersionInfo = myEditor.getCurrentLibraryVersionInfo();
    if (libraryVersionInfo != null) {
      String ri = libraryVersionInfo.getRI();
      String version = libraryVersionInfo.getVersion();

      return StringUtil.isEmptyOrSpaces(ri) ? version : ri +"." + version;
    }

    return super.getDefaultLibraryName();
  }
}

