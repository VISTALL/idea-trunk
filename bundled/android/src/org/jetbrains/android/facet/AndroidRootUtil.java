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

package org.jetbrains.android.facet;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.android.sdklib.SdkConstants;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 16, 2009
 * Time: 3:28:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidRootUtil {
  private AndroidRootUtil() {
  }

  @Nullable
  public static VirtualFile getManifestFile(@NotNull Module module) {
    VirtualFile[] files = ModuleRootManager.getInstance(module).getContentRoots();
    for (VirtualFile contentRoot : files) {
      VirtualFile manifest = contentRoot.findChild(SdkConstants.FN_ANDROID_MANIFEST_XML);
      if (manifest != null) return manifest;
    }
    return null;
  }

  @Nullable
  public static VirtualFile getResourceDir(@NotNull Module module) {
    return getManifestBrotherDir(module, SdkConstants.FD_RES);
  }

  @Nullable
  private static VirtualFile getManifestBrotherDir(@NotNull Module module, @NotNull String path) {
    VirtualFile manifestFile = getManifestFile(module);
    if (manifestFile == null) return null;
    VirtualFile parent = manifestFile.getParent();
    if (parent != null) {
      return parent.findChild(path);
    }
    return parent;
  }

  @Nullable
  public static VirtualFile getAssetsDir(@NotNull Module module) {
    return getManifestBrotherDir(module, SdkConstants.FD_ASSETS);
  }

  @Nullable
  public static VirtualFile getGenDir(@NotNull Module module) {
    return getManifestBrotherDir(module, SdkConstants.FD_GEN_SOURCES);
  }
}
