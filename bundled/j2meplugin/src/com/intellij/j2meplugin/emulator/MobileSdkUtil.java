/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
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
package com.intellij.j2meplugin.emulator;

import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import org.jetbrains.annotations.NonNls;

/**
 * User: anna
 * Date: Sep 21, 2004
 */
public class MobileSdkUtil {
  @NonNls private static final String JAR_FILE_TYPE = ".jar";
  @NonNls private static final String ZIP_FILE_TYPE = ".zip";
  @NonNls private static final String EXT_DIR = "ext";

  public static VirtualFile[] findApiClasses(String[] api) {
    JarFileSystem jarFileSystem = JarFileSystem.getInstance();

    ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
    for (int i = 0; api != null && i < api.length; i++) {
      File child = new File(api[i]);
      String path = child.getAbsolutePath().replace(File.separatorChar, '/') + JarFileSystem.JAR_SEPARATOR;
      jarFileSystem.setNoCopyJarForPath(path);
      VirtualFile vFile = jarFileSystem.findFileByPath(path);
      if (vFile != null) {
        result.add(vFile);
      }
    }
    return result.toArray(new VirtualFile[result.size()]);
  }

  public static VirtualFile[] findApiClasses(File file) {
    FileFilter jarFileFilter = new FileFilter() {
      public boolean accept(File f) {
        if (f.isDirectory()) return false;
        if (f.getName().endsWith(JAR_FILE_TYPE) ||
            f.getName().endsWith(ZIP_FILE_TYPE)) {
          return true;
        }
        return false;
      }
    };

    @NonNls final String libDirName = "lib";
    final File lib = new File(file, libDirName);
    File[] jarDirs = new File[]{lib, new File(lib, EXT_DIR)};

    ArrayList<File> childrenList = new ArrayList<File>();
    for (int i = 0; i < jarDirs.length; i++) {
      File jarDir = jarDirs[i];
      if ((jarDir != null) && jarDir.isDirectory()) {
        File[] files = jarDir.listFiles(jarFileFilter);
        for (int j = 0; j < files.length; j++) {
          childrenList.add(files[j]);
        }
      }
    }

    JarFileSystem jarFileSystem = JarFileSystem.getInstance();

    ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
    for (int i = 0; i < childrenList.size(); i++) {
      File child = childrenList.get(i);
      String path = child.getAbsolutePath().replace(File.separatorChar, '/') + JarFileSystem.JAR_SEPARATOR;
      jarFileSystem.setNoCopyJarForPath(path);
      VirtualFile vFile = jarFileSystem.findFileByPath(path);
      if (vFile != null) {
        result.add(vFile);
      }
    }
    return result.toArray(new VirtualFile[result.size()]);
  }

  public static void findDocs(File root, ArrayList<VirtualFile> docs) {
    if (!root.exists() || !root.isDirectory()) return;
    File[] docFiles = root.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return isDocRoot(pathname);
      }
    });
    if (docFiles != null && docFiles.length > 0) {
      String path = root.getAbsolutePath().replace(File.separatorChar, '/');
      docs.add(LocalFileSystem.getInstance().findFileByPath(path));
    }

    File[] children = root.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return !isDocRoot(pathname);
      }
    });
    for (int i = 0; children != null && i < children.length; i++) {
      findDocs(children[i], docs);
    }
  }

  private static boolean isDocRoot(File pathname) {
    @NonNls final String path = pathname.getPath();
    if (path.endsWith("index.htm") || path.endsWith("index.html")) {
      return true;
    }
    return false;
  }

}
