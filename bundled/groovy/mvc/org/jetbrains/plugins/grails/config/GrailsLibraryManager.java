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

package org.jetbrains.plugins.grails.config;

import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.groovy.config.AbstractGroovyLibraryManager;
import org.jetbrains.plugins.groovy.util.LibrariesUtil;

import javax.swing.*;
import java.io.File;

/**
 * @author peter
 */
public class GrailsLibraryManager extends AbstractGroovyLibraryManager {
  public boolean managesLibrary(@NotNull Library library, LibrariesContainer container) {
    return GrailsConfigUtils.containsGrailsJar(container.getLibraryFiles(library, OrderRootType.CLASSES));
  }

  @Nls
  public String getLibraryVersion(@NotNull Library library, LibrariesContainer librariesContainer) {
    return GrailsConfigUtils.getInstance().getSDKVersion(LibrariesUtil.getGroovyLibraryHome(librariesContainer.getLibraryFiles(library, OrderRootType.CLASSES)));
  }

  @NotNull
  public Icon getIcon() {
    return GrailsIcons.GRAILS_SDK;
  }

  @Override
  public Icon getDialogIcon() {
    return GrailsIcons.GRAILS_ICON;
  }

  @NotNull
  @Override
  public String getAddActionText() {
    return "Create new Grails SDK...";
  }

  @Override
  public boolean isSDKHome(@NotNull VirtualFile file) {
    return GrailsConfigUtils.getInstance().isSDKHome(file);
  }

  @NotNull
  @Override
  public String getSDKVersion(String path) {
    return GrailsConfigUtils.getInstance().getSDKVersion(path);
  }

  @Override
  protected void fillLibrary(String path, Library.ModifiableModel model) {
    String[] jars = new File(path + GrailsConfigUtils.DIST).list();
      if (jars != null) {
        for (String fileName : jars) {
          if (fileName.endsWith(".jar")) {
            model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + (GrailsConfigUtils.DIST + "/") + fileName)), OrderRootType.CLASSES);
          }
        }
      }

      jars = new File(path + GrailsConfigUtils.LIB_DIR).list();
      if (jars != null) {
        for (String fileName : jars) {
          if (fileName.endsWith(".jar")) {
            model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/lib/" + fileName)), OrderRootType.CLASSES);
          }
        }
      }

      model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/src/commons")), OrderRootType.SOURCES);
      model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/src/groovy")), OrderRootType.SOURCES);
      model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/src/persistence")), OrderRootType.SOURCES);
      model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/src/scaffolding")), OrderRootType.SOURCES);
      model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/src/tiger")), OrderRootType.SOURCES);
      model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/src/web")), OrderRootType.SOURCES);

      model.addRoot(VfsUtil.getUrlForLibraryRoot(new File(path + "/doc/api")), JavadocOrderRootType.getInstance());
  }

  @Nls
  @NotNull
  @Override
  public String getLibraryCategoryName() {
    return "Grails";
  }

}