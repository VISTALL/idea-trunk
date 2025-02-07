/*
 * Copyright 2000-2008 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.config.GroovyConfigUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyas
 */
public class LibrariesUtil {
  private LibrariesUtil() {
  }

  public static Library[] getLibrariesByCondition(final Module module, final Condition<Library> condition) {
    if (module == null) return new Library[0];
    final ArrayList<Library> libraries = new ArrayList<Library>();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        for (OrderEntry entry : manager.getOrderEntries()) {
          if (entry instanceof LibraryOrderEntry) {
            LibraryOrderEntry libEntry = (LibraryOrderEntry)entry;
            Library library = libEntry.getLibrary();
            if (condition.value(library)) {
              libraries.add(library);
            }
          }
        }
      }
    });

    return libraries.toArray(new Library[libraries.size()]);
  }

  public static Library[] getGlobalLibraries(Condition<Library> condition) {
    LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable();
    List<Library> libs = ContainerUtil.findAll(table.getLibraries(), condition);
    return libs.toArray(new Library[libs.size()]);
  }

  @NotNull
  public static String getGroovyLibraryHome(Library library) {
    final VirtualFile[] classRoots = library.getFiles(OrderRootType.CLASSES);
    return getGroovyLibraryHome(classRoots);
  }

  public static boolean hasGroovySdk(@Nullable Module module) {
    return module != null && getGroovyHomePath(module) != null;
  }

  @Nullable
  public static String getGroovyHomePath(Module module) {
    final String home = getGroovyLibraryHome(ModuleRootManager.getInstance(module).getFiles(OrderRootType.CLASSES));
    return StringUtil.isEmpty(home) ? null : home;
  }

  public static boolean isEmbeddableDistribution(VirtualFile[] classRoots) {
    if (getGroovySdkHome(classRoots) != null) {
      return false;
    }
    return getEmbeddableGroovyJar(classRoots) != null;
  }

  @Nullable
  private static String getGroovySdkHome(VirtualFile[] classRoots) {
    for (VirtualFile file : classRoots) {
      final String name = file.getName();
      if (name.matches(GroovyConfigUtils.GROOVY_JAR_PATTERN)) {
        String jarPath = file.getPresentableUrl();
        File realFile = new File(jarPath);
        if (realFile.exists()) {
          File parentFile = realFile.getParentFile();
          if (parentFile != null && "lib".equals(parentFile.getName())) {
            return parentFile.getParent();
          }
        }
      }
    }
    return null;
  }

  @Nullable
  private static String getEmbeddableGroovyJar(VirtualFile[] classRoots) {
    for (VirtualFile file : classRoots) {
      final String name = file.getName();
      if (name.matches(GroovyConfigUtils.GROOVY_ALL_JAR_PATTERN)) {
        String jarPath = file.getPresentableUrl();
        File realFile = new File(jarPath);
        if (realFile.exists()) {
          return realFile.getPath();
        }
      }
    }
    return null;
  }

  public static String getGroovyLibraryHome(VirtualFile[] classRoots) {
    final String sdkHome = getGroovySdkHome(classRoots);
    if (sdkHome != null) {
      return sdkHome;
    }

    final String embeddable = getEmbeddableGroovyJar(classRoots);
    if (embeddable != null) {
      final File emb = new File(embeddable);
      if (emb.exists()) {
        final File parent = emb.getParentFile();
        if ("embeddable".equals(parent.getName()) || "lib".equals(parent.getName())) {
          return parent.getParent();
        }
        return parent.getPath();
      }
    }
    return "";
  }

  @NotNull
  public static VirtualFile getLocalFile(@NotNull VirtualFile libFile) {
    final VirtualFileSystem system = libFile.getFileSystem();
    if (system instanceof JarFileSystem) {
      final VirtualFile local = JarFileSystem.getInstance().getVirtualFileForJar(libFile);
      if (local != null) {
        return local;
      }
    }
    return libFile;
  }

  public static String generateNewLibraryName(String version, String prefix, final Project project) {
    List<Object> libNames = ContainerUtil.map(GroovyConfigUtils.getInstance().getAllSDKLibraries(project), new Function<Library, Object>() {
      public Object fun(Library library) {
        return library.getName();
      }
    });
    String originalName = prefix + version;
    String newName = originalName;
    int index = 1;
    while (libNames.contains(newName)) {
      newName = originalName + " (" + index + ")";
      index++;
    }
    return newName;
  }

  public static void placeEntryToCorrectPlace(ModifiableRootModel model, LibraryOrderEntry addedEntry) {
    final OrderEntry[] order = model.getOrderEntries();
    //place library after module sources
    assert order[order.length - 1] == addedEntry;
    int insertionPoint = -1;
    for (int i = 0; i < order.length - 1; i++) {
      if (order[i] instanceof ModuleSourceOrderEntry) {
        insertionPoint = i + 1;
        break;
      }
    }
    if (insertionPoint >= 0) {
      for (int i = order.length - 1; i > insertionPoint; i--) {
        order[i] = order[i - 1];
      }
      order[insertionPoint] = addedEntry;
      model.rearrangeOrderEntries(order);
    }
  }

}
