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

package com.intellij.uml.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.uml.UmlColorManager;
import com.intellij.uml.UmlExtras;
import com.intellij.uml.UmlProvider;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin Bulenkov
 */
public class ModulesUmlProvider extends UmlProvider<ModuleItem> {
  private static final String ID = "MODULES";
  private final ModulesUmlVisibilityManager myVisibilityManager;
  private final ModulesUmlCategoryManager myCategoryManager;
  private final ModulesUmlElementManager myElementManager;
  private final ModulesUmlVfsResolver myVfsResolver;
  private final ModulesUmlColorManager myColorManager = new ModulesUmlColorManager();
  private final ModulesUmlRelationshipManager myRelationshipManager = new ModulesUmlRelationshipManager();
  private final ModulesUmlExtras myExtras = new ModulesUmlExtras();

  public ModulesUmlProvider() {
    myVisibilityManager = new ModulesUmlVisibilityManager();
    myCategoryManager = new ModulesUmlCategoryManager();
    myElementManager = new ModulesUmlElementManager();
    myVfsResolver = new ModulesUmlVfsResolver();
    myElementManager.setUmlProvider(this);
  }

  @Pattern("[a-zA-Z0-9_-]*")
  public String getID() {
    return ID;
  }

  @Override
  public ModulesUmlVisibilityManager getVisibilityManager() {
    return myVisibilityManager;
  }

  @Override
  public ModulesUmlCategoryManager getNodeContentManager() {
    return myCategoryManager;
  }

  @Override
  public ModulesUmlElementManager getElementManager() {
    return myElementManager;
  }

  @Override
  public ModulesUmlVfsResolver getVfsResolver() {
    return myVfsResolver;
  }

  @Override
  public ModulesUmlRelationshipManager getRelationshipManager() {
    return myRelationshipManager;
  }

  @Override
  public ModulesUmlDataModel createDataModel(@NotNull Project project, @Nullable ModuleItem element, @Nullable VirtualFile file) {
    return new ModulesUmlDataModel(element, file);
  }

  @Override
  public ModificationTracker getModificationTracker(@NotNull Project project) {
    return PsiManager.getInstance(project).getModificationTracker();
  }

  @Override
  public UmlColorManager getColorManager() {
    return myColorManager;
  }

  @Override
  public UmlExtras getExtras() {
    return myExtras;
  }
}
