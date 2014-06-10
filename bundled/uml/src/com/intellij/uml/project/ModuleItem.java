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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class ModuleItem {
  private final Module myModule;
  private final Library myLibrary;
  private final Project myProject;
  private String name;

  @NonNls private static final String SEPARATOR = "-";
  @NonNls private static final String MODULE_PREFIX = "mdl" + SEPARATOR;
  @NonNls private static final String LIB_PREFIX = "lib" + SEPARATOR;
  @NonNls private static final String JAR_PREFIX = "jar" + SEPARATOR;
  public static final ModuleItem[] EMPTY_ARRAY = {};

  public ModuleItem(@NotNull Module module) {
    this(module, null, module.getProject());
  }

  public ModuleItem(@NotNull Library library, @NotNull Project project) {
    this(null, library, project);
  }

  public Module getModule() {
    return myModule;
  }

  public Library getLibrary() {
    return myLibrary;
  }  

  public static ModuleItem fromFQN(@NotNull String fqn, Project project) throws IllegalArgumentException {
    if (fqn.length() < 5 || fqn.charAt(3) != '-' ) throw new IllegalArgumentException("Wrong FQN");
    final String prefix = fqn.substring(0, 4);
    final String name = fqn.substring(4);
    if (MODULE_PREFIX.equals(prefix)) {
      final Module module = ModuleManager.getInstance(project).findModuleByName(name);
      if (module == null) {
        throw new IllegalArgumentException("Wrong FQN");
      }
      return new ModuleItem(module);
    } else if (LIB_PREFIX.equals(prefix)) {
      for (Library library : ProjectLibraryTable.getInstance(project).getLibraries()) {
        if (library.getName().equals(name)) {
          return new ModuleItem(library, project);
        }
      }
    }
    //TODO: Module libraries
    throw new IllegalArgumentException("Wrong FQN");
  }

  private ModuleItem(Module module, Library library, Project project) {
    myModule = module;
    myLibrary = library;
    myProject = project;
  }

  public boolean isModule() {
    return myModule != null;
  }

  public boolean isLibrary() {
    return myLibrary != null;
  }

  public Project getProject() {
    return myProject; 
  }

  private String getPrefix() {
    if (isModule()) {
      return MODULE_PREFIX;
    } else if (isLibrary()) {
      return LIB_PREFIX;
    } else {
      return JAR_PREFIX;
    }
  }

  public String getName() {
    if (name == null) {
      if (isModule()) {
        name = myModule.getName();
      } else if (isLibrary()) {
        name = myLibrary.getName();
        if (name == null) {
          name = myLibrary.getModifiableModel().getName();
          if (name == null) {
            final VirtualFile[] files = myLibrary.getModifiableModel().getFiles(OrderRootType.COMPILATION_CLASSES);
            if (files != null && files.length > 0) {
              name = files[0].getName();
            } else {
              name = "???";
            }
          }
        }
      } else {
        name = "-unknown-"; //TODO
      }
    }
    return name;
  }

  public String getQualifiedName() {
    return getPrefix() + getName();
  }

  public Icon getIcon() {
    if (isModule()) {
      return myModule.getModuleType().getNodeIcon(false);
    } else {
      return Icons.LIBRARY_ICON;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModuleItem that = (ModuleItem)o;
    return getQualifiedName().equals(that.getQualifiedName());
  }

  @Override
  public int hashCode() {
    return getQualifiedName().hashCode();
  }

  @Override
  public String toString() {
    return (isLibrary() ? "Library " : "Module ") + getName();
  }
}
