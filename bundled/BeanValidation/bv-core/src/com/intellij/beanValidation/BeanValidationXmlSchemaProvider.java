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

import com.intellij.beanValidation.facet.BeanValidationFacet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationXmlSchemaProvider extends XmlSchemaProvider implements DumbAware {
  private static final List<String> SCHEMAS = new ArrayList<String>();
  static {
    SCHEMAS.add("validation-mapping-1.0.xsd");
    SCHEMAS.add("validation-configuration-1.0.xsd");
  }
  @Override
  public XmlFile getSchema(@NotNull @NonNls String url, @Nullable Module module, @NotNull PsiFile baseFile) {
    if (module == null || !SCHEMAS.contains(url)) return null;
    final PsiDirectory directory = baseFile.getContainingDirectory();
    if (directory != null && directory.findFile(url) != null) return null;
    final BeanValidationFacet facet = FacetManager.getInstance(module).findFacet(BeanValidationFacet.FACET_TYPE_ID, "Bean Validation");
    if (facet == null) return null;
    final PsiFile[] files =
      FilenameIndex.getFilesByName(module.getProject(), url, module.getModuleWithDependenciesAndLibrariesScope(false));
    return files != null && files.length > 0 && files[0] instanceof XmlFile ? (XmlFile)files[0] : null;
  }

  @Override
  public boolean isAvailable(@NotNull XmlFile file) {
    return true;
  }

  
}
