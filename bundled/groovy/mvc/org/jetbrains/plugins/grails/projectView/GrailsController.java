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

package org.jetbrains.plugins.grails.projectView;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 09.02.2009
 */
public class GrailsController {
  private final Module myModule;
  private final GrTypeDefinition myTypeDefinition;

  public GrailsController(final GrTypeDefinition typeDefinition) {
    myTypeDefinition = typeDefinition;
    myModule = ModuleUtil.findModuleForPsiElement(typeDefinition);
  }

  @Nullable
  public static GrailsController fromClass(@NotNull final GrTypeDefinition grTypeDefinition) {
    final Module module = ModuleUtil.findModuleForPsiElement(grTypeDefinition);
    if (module == null || !GrailsUtils.hasGrailsSupport(module)) {
      return null;
    }
    if (!GrailsUtils.isControllerClass(grTypeDefinition, module)) {
      return null;
    }

    return new GrailsController(grTypeDefinition);
  }

  @Nullable
  public static GrailsController fromFile(@NotNull final PsiFile psiFile) {
    final VirtualFile file = psiFile.getVirtualFile();
    return file == null ? null : fromFile(ModuleUtil.findModuleForPsiElement(psiFile), file);
  }

  @Nullable
  public static GrailsController fromFile(final Module module, @NotNull final VirtualFile file) {
    if (module == null || !GrailsUtils.hasGrailsSupport(module)) {
      return null;
    }
    PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(file);
    if (psiFile instanceof GroovyFile) {
      final GrTypeDefinition[] classes = ((GroovyFile)psiFile).getTypeDefinitions();
      for (GrTypeDefinition grTypeDefinition : classes) {
        if (isControllerClass(grTypeDefinition)) {
          return new GrailsController(grTypeDefinition);
        }
      }
    }
    return null;
  }

  @Nullable
  public VirtualFile getViewsFolder() {
    final VirtualFile viewsFolder = GrailsUtils.findViewsDirectory(myModule);
    if (viewsFolder == null || !viewsFolder.isDirectory()) {
      return null;
    }

    final String name = myTypeDefinition.getName();
    if (name == null) return null;

    if (!name.endsWith("Controller")) return null;

    final String controllerName = name.substring(0, name.length() - "Controller".length());
    final VirtualFile controllerFolder = viewsFolder.findFileByRelativePath(controllerName);

    if (controllerFolder == null || !controllerFolder.isDirectory()) return null;

    return controllerFolder;
  }

  @Nullable
  public List<GrailsLayout> getLayouts() {
    final VirtualFile viewsFolder = getViewsFolder();
    if (viewsFolder == null) return null;

    final VirtualFile layoutDirectory = viewsFolder.findFileByRelativePath("layouts");
    if (layoutDirectory == null || !layoutDirectory.isDirectory()) return null;

    final List<GrailsLayout> layouts = new ArrayList<GrailsLayout>();

    final VirtualFile[] layoutsFiles = layoutDirectory.getChildren();
    for (VirtualFile layout : layoutsFiles) {
      layouts.add(new GrailsLayout(layout));
    }

    return layouts;
  }

  public GrTypeDefinition getTypeDefinition() {
    return myTypeDefinition;
  }

  public Module getModule() {
    return myModule;
  }

  public static boolean isControllerClass(GrTypeDefinition grTypeDefinition) {
    final String name = grTypeDefinition.getName();
    return name != null && name.endsWith("Controller");
  }
}
