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

import com.android.ddmlib.AndroidDebugBridge;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.intellij.CommonBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.util.AndroidUtils;
import static org.jetbrains.android.util.AndroidUtils.ADB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 2, 2009
 * Time: 2:35:49 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AndroidSdk {
  private static boolean myDdmLibInitialized = false;

  @NotNull
  public abstract String getLocation();

  @NotNull
  public abstract String getName();

  @NotNull
  public abstract IAndroidTarget[] getTargets();

  @NotNull
  public abstract String getDefaultTargetName();

  public abstract int getExpectedTargetCount();

  // be careful! target name is NOT unique
  @Nullable
  public IAndroidTarget findTargetByName(@NotNull String name) {
    for (IAndroidTarget target : getTargets()) {
      if (target.getName().equals(name)) {
        return target;
      }
    }
    return null;
  }

  @Nullable
  public static AndroidSdk parse(@NotNull String path, @NotNull ISdkLog log) {
    path = FileUtil.toSystemDependentName(path);
    if (isAndroid15Sdk(path)) {
      SdkManager manager = SdkManager.createManager(path, log);
      if (manager != null) {
        return new AndroidSdk15(manager);
      }
    }
    else {
      return new AndroidSdk11(path);
    }
    return null;
  }

  @Nullable
  public static AndroidSdk parse(@NotNull String path, @NotNull final Component component) {
    MessageBuildingSdkLog log = new MessageBuildingSdkLog();
    AndroidSdk sdk = parse(path, log);
    if (sdk == null || sdk.getExpectedTargetCount() != sdk.getTargets().length) {
      String message = log.getErrorMessage();
      if (message.length() > 0) {
        message = "Android SDK is parsed incorrectly. Parsing log:\n" + message;
        Messages.showInfoMessage(component, message, CommonBundle.getErrorTitle());
      }
    }
    return sdk;
  }

  static boolean isAndroid15Sdk(@NotNull String location) {
    VirtualFile sdkDir = LocalFileSystem.getInstance().findFileByPath(location);
    return sdkDir != null && sdkDir.findChild(SdkConstants.FD_PLATFORMS) != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj.getClass() != getClass()) return false;
    AndroidSdk sdk = (AndroidSdk)obj;
    return getLocation().equals(sdk.getLocation());
  }

  @Override
  public int hashCode() {
    return getLocation().hashCode();
  }

  public void initializeDdmlib() {
    if (!myDdmLibInitialized) {
      myDdmLibInitialized = true;
      AndroidDebugBridge.init(true);
    }
    String adbPath = getLocation() + File.separator + AndroidUtils.toolPath(ADB);
    AndroidDebugBridge.createBridge(adbPath, false);
  }

  @Nullable
  public AndroidDebugBridge getDebugBridge(Project project) {
    initializeDdmlib();
    AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
    if (bridge == null) {
      Messages.showErrorDialog(project, "Cannot connect to emulator", CommonBundle.getErrorTitle());
    }
    return bridge;
  }
}
