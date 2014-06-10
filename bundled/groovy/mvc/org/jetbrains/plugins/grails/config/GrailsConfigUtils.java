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

package org.jetbrains.plugins.grails.config;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.config.AbstractConfigUtils;
import org.jetbrains.plugins.groovy.config.GroovyConfigUtils;
import org.jetbrains.plugins.groovy.util.GroovyUtils;
import org.jetbrains.plugins.groovy.util.LibrariesUtil;

/**
 * @author ilyas
 */
public abstract class GrailsConfigUtils extends AbstractConfigUtils {

  @NonNls public static final String GRAILS_CORE_JAR_PATTERN = "grails-core-\\d.*jar";

  private static GrailsConfigUtils myGrailsConfigUtils;
  @NonNls private static final String GRAILS_MANIFEST_MF = "META-INF/GRAILS-MANIFEST.MF";
  @NonNls protected static final String DIST = "/dist";
  @NonNls static final String LIB_DIR = "/lib";

  private GrailsConfigUtils() {
  }

  public static GrailsConfigUtils getInstance() {
    if (myGrailsConfigUtils == null) {
      myGrailsConfigUtils = new GrailsConfigUtils() {
        {
          STARTER_SCRIPT_FILE_NAME = "grails";
        }};
    }
    return myGrailsConfigUtils;
  }


  public boolean isSDKLibrary(Library library) {
    if (library == null) return false;

    return containsGrailsJar(library.getFiles(OrderRootType.CLASSES));

  }

  public static boolean containsGrailsJar(VirtualFile[] files) {
    for (VirtualFile file : files) {
      if (isGrailsCoreJar(file.getName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isSDKHome(VirtualFile file) {
    if (file != null && file.isDirectory()) {
      final String path = file.getPath();
      if (GroovyUtils.getFilesInDirectoryByPattern(path + "/lib", GRAILS_CORE_JAR_PATTERN).length > 0) {
        return true;
      }
      if (GroovyUtils.getFilesInDirectoryByPattern(path + "/embeddable", GRAILS_CORE_JAR_PATTERN).length > 0) {
        return true;
      }
      if (file.findFileByRelativePath("bin/" + STARTER_SCRIPT_FILE_NAME) != null) {
        return true;
      }
    }
    return false;
  }


  @NotNull
  public String getSDKVersion(@NotNull String path) {
    String grailsJarVersion = getSDKJarVersion(path + DIST, GRAILS_CORE_JAR_PATTERN, GroovyConfigUtils.MANIFEST_PATH);
    if (grailsJarVersion == null) {
      // check for versions <= 0.6
      grailsJarVersion = getSDKJarVersion(path + DIST, GRAILS_CORE_JAR_PATTERN, GRAILS_MANIFEST_MF);
    }
    return grailsJarVersion == null ? UNDEFINED_VERSION : grailsJarVersion;
  }

  @Nullable
  public String getGrailsVersion(Module module) {
    Library[] libraries = getSDKLibrariesByModule(module);
    if (libraries.length == 0) return null;
    return getSDKLibVersion(libraries[0]);
  }

  @NotNull
  public String getSDKInstallPath(Module module) {
    if (module == null) return "";
    Library[] libraries = getSDKLibrariesByModule(module);
    if (libraries.length == 0) return "";
    Library library = libraries[0];
    return LibrariesUtil.getGroovyLibraryHome(library);
  }

  public static boolean isAtLeastGrails1_1(Module module) {
    final String version = getInstance().getGrailsVersion(module);
    return version != null && version.compareTo("1.1") >= 0;
  }

  public static boolean isAtLeastGrails1_2(Module module) {
    final String version = getInstance().getGrailsVersion(module);
    return version != null && version.compareTo("1.2") >= 0;
  }

  public static boolean isGrailsCoreJar(String fileName) {
    return fileName.matches(GRAILS_CORE_JAR_PATTERN);
  }
}
