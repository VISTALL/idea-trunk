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

package org.jetbrains.android.sdk;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.android.util.OrderRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 26, 2009
 * Time: 8:01:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidSdkUtils {
  public static final String DEFAULT_PLATFORM_NAME_PROPERTY = "AndroidPlatformName";

  private AndroidSdkUtils() {
  }

  private static boolean isAndroidSdk(@NotNull String path) {
    if (AndroidSdk.isAndroid15Sdk(path)) {
      path = FileUtil.toSystemDependentName(path);
      SdkManager manager = SdkManager.createManager(path, new EmptySdkLog());
      return manager != null;
    }
    else {
      AndroidSdk11 sdk11 = new AndroidSdk11(path);
      return sdk11.isValid();
    }
  }

  @Nullable
  private static VirtualFile getPlatformDir(IAndroidTarget target) {
    String platformPath = target.isPlatform() ? target.getLocation() : target.getParent().getLocation();
    VirtualFile platformDir = LocalFileSystem.getInstance().findFileByPath(platformPath);
    if (platformDir == null) return null;
    return platformDir;
  }

  public static VirtualFile[] chooseAndroidSdkPath(Component parent) {
    return FileChooser.chooseFiles(parent, new FileChooserDescriptor(false, true, false, false, false, false) {
      @Override
      public boolean isFileSelectable(VirtualFile file) {
        return super.isFileSelectable(file) && isAndroidSdk(file.getPath());
      }
    });
  }

  public static List<OrderRoot> getLibraryRootsForTarget(@NotNull IAndroidTarget target, @NotNull String sdkPath) {
    List<OrderRoot> result = new ArrayList<OrderRoot>();
    VirtualFile platformDir = AndroidSdkUtils.getPlatformDir(target);
    if (platformDir == null) return result;

    VirtualFile androidJar = platformDir.findChild(SdkConstants.FN_FRAMEWORK_LIBRARY);
    if (androidJar == null) return result;
    VirtualFile androidJarRoot = JarFileSystem.getInstance().findFileByPath(androidJar.getPath() + JarFileSystem.JAR_SEPARATOR);
    result.add(new OrderRoot(OrderRootType.CLASSES, androidJarRoot));

    IAndroidTarget.IOptionalLibrary[] libs = target.getOptionalLibraries();
    if (libs != null) {
      for (IAndroidTarget.IOptionalLibrary lib : libs) {
        VirtualFile libRoot = JarFileSystem.getInstance().findFileByPath(lib.getJarPath() + JarFileSystem.JAR_SEPARATOR);
        result.add(new OrderRoot(OrderRootType.CLASSES, libRoot));
      }
    }
    VirtualFile targetDir = platformDir;
    if (!target.isPlatform()) {
      targetDir = LocalFileSystem.getInstance().findFileByPath(target.getLocation());
    }
    AndroidSdkUtils.addJavaDocAndSources(result, targetDir);
    VirtualFile sdkDir = LocalFileSystem.getInstance().findFileByPath(sdkPath);
    if (sdkDir != null) {
      AndroidSdkUtils.addJavaDocAndSources(result, sdkDir);
    }
    return result;
  }

  @Nullable
  private static VirtualFile findJavadocDir(@NotNull VirtualFile dir) {
    VirtualFile docsDir = dir.findChild(SdkConstants.FD_DOCS);

    if (docsDir != null) {
      VirtualFile referenceDir = docsDir.findChild(SdkConstants.FD_DOCS_REFERENCE);
      if (referenceDir != null) {
        return referenceDir;
      }
    }
    return null;
  }

  private static void addJavaDocAndSources(List<OrderRoot> list, VirtualFile dir) {
    VirtualFile javadocDir = findJavadocDir(dir);
    if (javadocDir != null) {
      list.add(new OrderRoot(JavadocOrderRootType.getInstance(), javadocDir));
    }

    VirtualFile sourcesDir = dir.findChild(SdkConstants.FD_SOURCES);
    if (sourcesDir != null) {
      list.add(new OrderRoot(OrderRootType.SOURCES, sourcesDir));
    }
  }

  public static String getPresentableTargetName(IAndroidTarget target) {
    IAndroidTarget parentTarget = target.getParent();
    if (parentTarget != null) {
      return target.getName() + " (" + parentTarget.getVersionName() + ')';
    }
    return target.getName();
  }
}
