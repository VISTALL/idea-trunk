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

package com.intellij.uml.java;

import com.intellij.uml.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.intellij.lang.annotations.Pattern;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlProvider extends UmlProvider<PsiElement> {
  private static final String ID = "JAVA";
  private final JavaUmlVisibilityManager myVisibilityManager;
  private final JavaUmlCategoryManager myCategoryManager;
  private final JavaUmlElementManager myElementManager;
  private final JavaUmlVfsResolver myVfsResolver;
  private final JavaUmlColorManager myColorManager = new JavaUmlColorManager();
  private final JavaUmlRelationshipManager myRelationshipManager = new JavaUmlRelationshipManager();
  private final JavaUmlExtras myExtras = new JavaUmlExtras();


  public JavaUmlProvider() {
    myVisibilityManager = new JavaUmlVisibilityManager();
    myCategoryManager = new JavaUmlCategoryManager();
    myElementManager = new JavaUmlElementManager();
    myVfsResolver = new JavaUmlVfsResolver();
    myElementManager.setUmlProvider(this);
  }

  @Pattern("[a-zA-Z0-9_-]*")
  public String getID() {
    return ID;
  }

  @Override
  public JavaUmlVisibilityManager getVisibilityManager() {
    return myVisibilityManager;
  }

  @Override
  public JavaUmlCategoryManager getNodeContentManager() {
    return myCategoryManager;
  }

  @Override
  public JavaUmlElementManager getElementManager() {
    return myElementManager;
  }

  @Override
  public JavaUmlVfsResolver getVfsResolver() {
    return myVfsResolver;
  }

  @Override
  public JavaUmlRelationshipManager getRelationshipManager() {
    return myRelationshipManager;
  }

  @Override
  public UmlDataModel<PsiElement> createDataModel(@NotNull Project project, @Nullable PsiElement element, @Nullable VirtualFile file) {
    return new JavaUmlDataModel(element, file);
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
  public JavaUmlExtras getExtras() {
    return myExtras;
  }
}
