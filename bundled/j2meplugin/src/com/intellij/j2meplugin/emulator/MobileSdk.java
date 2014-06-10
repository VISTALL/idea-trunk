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

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.ui.MobileSdkConfigurable;
import com.intellij.j2meplugin.module.J2MEModuleProperties;
import com.intellij.j2meplugin.module.J2MEModuleType;
import com.intellij.j2meplugin.util.MobileIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

/**
 * User: anna
 * Date: Sep 21, 2004
 */
public class MobileSdk extends SdkType implements JavaSdkType {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");

  public MobileSdk() {
    super("MobileSDK");
  }

  public String getPresentableName() {
    return J2MEBundle.message("jdk.type.name");
  }

  public Icon getIcon() {
    return MobileIcons.SDK_CLOSED;
  }

  public Icon getIconForExpandedTreeNode() {
    return MobileIcons.SDK_OPEN;
  }

  public Icon getIconForAddAction() {
    return MobileIcons.SDK_ADD;
  }

  public static MobileSdk getInstance() {
    return SdkType.findInstance(MobileSdk.class);
  }

  public static boolean checkCorrectness(Sdk projectJdk, Module module) {
    if (projectJdk == null) return false;
    if (!projectJdk.getSdkType().equals(MobileSdk.getInstance())) return false;
    if (!(projectJdk.getSdkAdditionalData() instanceof Emulator)) return false;
    final Emulator emulator = ((Emulator)projectJdk.getSdkAdditionalData());
    if (emulator.getHome() == null) {
      emulator.setHome(projectJdk.getHomePath());
    }
    final EmulatorType emulatorType = emulator.getEmulatorType();
    if (emulatorType == null) return false;
    final Sdk javaSdk = emulator.getJavaSdk();
    if (javaSdk == null || javaSdk.getSdkType() == null || !javaSdk.getSdkType().equals(JavaSdk.getInstance())) {
      return false;
    }
    if (module != null) {
      if (!module.getModuleType().equals(J2MEModuleType.getInstance()) ||
           J2MEModuleProperties.getInstance(module).getMobileApplicationType() == null) {
        return false;
      }
    }
    return true;
  }

  @Nullable
  public static EmulatorType getEmulatorType(Sdk jdk, Module module) {
    if (checkCorrectness(jdk, module)) {
      return ((Emulator)jdk.getSdkAdditionalData()).getEmulatorType();
    }
    return null;
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
    final MobileSdkConfigurable mobileSdkConfigurable = new MobileSdkConfigurable(sdkModel, sdkModificator);
    sdkModel.addListener(new SdkModel.Listener() {
      public void sdkAdded(Sdk sdk) {
        if (sdk.getSdkType().equals(JavaSdk.getInstance())) {
          mobileSdkConfigurable.addJavaSdk(sdk);
        }
      }

      public void beforeSdkRemove(Sdk sdk) {
        if (sdk.getSdkType().equals(JavaSdk.getInstance())) {
          mobileSdkConfigurable.removeJavaSdk(sdk);
        }
      }

      public void sdkChanged(Sdk sdk, String previousName) {
        if (sdk.getSdkType().equals(JavaSdk.getInstance())) {
          mobileSdkConfigurable.updateJavaSdkList(sdk, previousName);
        }
      }

      public void sdkHomeSelected(final Sdk sdk, final String newSdkHome) {
        if (sdk.getSdkType().equals(MobileSdk.getInstance())) {
          mobileSdkConfigurable.homePathChanged(newSdkHome);
        }
      }
    });
    return mobileSdkConfigurable;
  }

  public void saveAdditionalData(SdkAdditionalData additionalData, Element additional) {
    if (!(additionalData instanceof Emulator)) return;
    try {
      ((Emulator)additionalData).writeExternal(additional);
    }
    catch (WriteExternalException e) {
      LOG.error(e);
    }
  }

  public SdkAdditionalData loadAdditionalData(Element additional) {
    Emulator emulator = new Emulator();
    try {
      emulator.readExternal(additional);
    }
    catch (InvalidDataException e) {
      LOG.error(e);
    }
    return emulator;
  }

  public String getBinPath(Sdk sdk) {
    if (!checkCorrectness(sdk, null)) return null;
    Sdk mySdk = ((Emulator)sdk.getSdkAdditionalData()).getJavaSdk();
    LOG.assertTrue(mySdk != null);
    return ((JavaSdk)mySdk.getSdkType()).getBinPath(mySdk);
  }

  public String getToolsPath(Sdk sdk) {
    if (!checkCorrectness(sdk, null)) return null;
    Sdk mySdk = ((Emulator)sdk.getSdkAdditionalData()).getJavaSdk();
    LOG.assertTrue(mySdk != null);
    return ((JavaSdk)mySdk.getSdkType()).getToolsPath(mySdk);
  }

  public String getVMExecutablePath(Sdk sdk) {
    if (!checkCorrectness(sdk, null)) return null;
    Sdk mySdk = ((Emulator)sdk.getSdkAdditionalData()).getJavaSdk();
    LOG.assertTrue(mySdk != null);
    return ((JavaSdk)mySdk.getSdkType()).getVMExecutablePath(mySdk);
  }

  public String suggestHomePath() {
    return null;
  }

  public boolean isValidSdkHome(String path) {
    return EmulatorUtil.getValidEmulatorType(path) != null;
  }

  public String getVersionString(String sdkHome) {
    return "1.3";
  }

  @Nullable
  public String suggestSdkName(String currentSdkName, String sdkHome) {
    final EmulatorType emulatorType = EmulatorUtil.getValidEmulatorType(sdkHome);
    if (emulatorType != null) {
      return emulatorType.suggestName(sdkHome);
    }
    return null;
  }


  public void setupSdkPaths(Sdk sdk) {
    VirtualFile[] classes = null;
    final File mobileJdkHome = new File(sdk.getHomePath());
    final EmulatorType emulatorType = EmulatorUtil.getValidEmulatorType(sdk.getHomePath());
    LOG.assertTrue(emulatorType != null);
    final String[] apiClasses = emulatorType.getApi(sdk.getHomePath());
    if (apiClasses != null) {
      classes = MobileSdkUtil.findApiClasses(apiClasses);
    }
    if (classes == null || classes.length == 0) {
      classes = MobileSdkUtil.findApiClasses(mobileJdkHome);
    }
    ArrayList<VirtualFile> docs = new ArrayList<VirtualFile>();
    @NonNls final String docsString = "docs";
    @NonNls final String apiString = "api";
    @NonNls final String docString = "doc";
    final File api = new File(new File(mobileJdkHome, docsString), apiString).exists() ? new File(new File(mobileJdkHome, docsString), apiString) :
                     new File(new File(mobileJdkHome, docString), apiString).exists() ? new File(new File(mobileJdkHome, docString), apiString) : null;
    if (api != null) {
      MobileSdkUtil.findDocs(api, docs);
    }
    else {
      MobileSdkUtil.findDocs(mobileJdkHome, docs);
    }

    final SdkModificator sdkModificator = sdk.getSdkModificator();
    for (int i = 0; classes != null && i < classes.length; i++) {
      sdkModificator.addRoot(classes[i], OrderRootType.CLASSES);
    }

    for (final VirtualFile doc : docs) {
      sdkModificator.addRoot(doc, JavadocOrderRootType.getInstance());
    }

    sdkModificator.setSdkAdditionalData(new Emulator(emulatorType, null, EmulatorUtil.findFirstJavaSdk(), sdk.getHomePath()));
    
    sdkModificator.commitChanges();

  }
}
