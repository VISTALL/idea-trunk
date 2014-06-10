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
package com.intellij.j2meplugin.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.midp.MIDPApplicationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * User: anna
 * Date: Aug 16, 2004
 */
public class J2MEModuleBuilder extends JavaModuleBuilder {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");
  private String explodedDirPath;
  private boolean excludeFromContent = false;
  private String resourcesDirPath;
  private MobileApplicationType myMobileApplicationType;
  private MobileModuleSettings myMobileModuleSettings;
  private boolean myDefaultRDirectoryModified = false;
  private boolean myDefaultEDirectoryModified = false;

  public void setupRootModel(final ModifiableRootModel rootModel) throws ConfigurationException {
    super.setupRootModel(rootModel);
    if (explodedDirPath != null) {
      createRoot(rootModel, explodedDirPath, J2MEBundle.message("exploded.directory.create.command"));
      String canonicalPath;
      try {
        canonicalPath = "".equals(explodedDirPath) ? null : new File(explodedDirPath).getCanonicalPath();
      }
      catch (IOException e) {
        canonicalPath = explodedDirPath;
      }
      final String url = (canonicalPath == null)
                         ? null
                         : VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, canonicalPath.replace(File.separatorChar, '/'));
      rootModel.setExplodedDirectory(url);
      rootModel.setExcludeExplodedDirectory(excludeFromContent);
    }

    if (resourcesDirPath != null) {
      final String path = FileUtil.toSystemIndependentName(resourcesDirPath);
      createRoot(rootModel, path, J2MEBundle.message("resource.directory.create.command"));
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          final File parentDir = new File(path).getParentFile();
          if (parentDir != null) {
            final VirtualFile contentRootFile = LocalFileSystem.getInstance().findFileByIoFile(parentDir);
            if (contentRootFile != null) {
              ContentEntry contentEntry = rootModel.addContentEntry(contentRootFile);
              for (int i = 0; rootModel.getContentEntries() != null && i < rootModel.getContentEntries().length; i++) {
                final VirtualFile virtualFile = rootModel.getContentEntries()[i].getFile();
                if (virtualFile != null) {
                  if (path.startsWith(virtualFile.getPath())) {
                    contentEntry = rootModel.getContentEntries()[i];
                  }
                }
              }
              final VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(path);
              LOG.assertTrue(fileByPath != null);
              contentEntry.addSourceFolder(fileByPath, false);
              J2MEModuleProperties.getInstance(rootModel.getModule()).setResourcePath(resourcesDirPath);
            }
          }
        }
      });
    }

    final Module module = rootModel.getModule();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        J2MEModuleProperties.getInstance(module).setMobileApplicationType(getMobileApplicationType());
        getMobileModuleSettings().copyTo(MobileModuleSettings.getInstance(module));
      }
    });

    rootModel.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(LanguageLevel.JDK_1_4);
  }

  private void createRoot(final ModifiableRootModel rootModel, String path, final String commandName) {
    path = FileUtil.toSystemIndependentName(path);
    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
    if (file == null) {
      final File ioFile = new File(path);
      CommandProcessor.getInstance().executeCommand(rootModel.getModule().getProject(), new Runnable() {
        public void run() {
          FileUtil.createParentDirs(ioFile);
          final File parentDir = ioFile.getParentFile();
          if (parentDir != null) {
            final LocalFileSystem lfs = LocalFileSystem.getInstance();
            final VirtualFile dir = lfs.refreshAndFindFileByIoFile(parentDir);
            if (dir != null) {
              try {
                dir.createChildDirectory(this, ioFile.getName());
              }
              catch (IOException e) {
                LOG.error(e);
              }
            }
          }
        }
      }, commandName, null);
    }
  }

  public String getExplodedDirPath() {
    return explodedDirPath;
  }

  public void setExplodedDirPath(String explodedDirPath) {
    this.explodedDirPath = explodedDirPath;
  }

  public String getResourcesDirPath() {
    return resourcesDirPath;
  }

  public void setResourcesDirPath(String resourcesDirPath) {
    this.resourcesDirPath = resourcesDirPath;
  }

  public MobileApplicationType getMobileApplicationType() {
    if (myMobileApplicationType == null) {
      final MobileApplicationType applicationType = getCurrentApplicationType();
      if (applicationType == null) {
        return MIDPApplicationType.getInstance();
      }
      myMobileApplicationType = applicationType;
    }
    return myMobileApplicationType;
  }

  @Nullable
  private MobileApplicationType getCurrentApplicationType() {
    final Sdk projectJdk = getModuleJdk();
    if (projectJdk != null) {
      final SdkAdditionalData data = projectJdk.getSdkAdditionalData();
      if (data instanceof Emulator) {
        final EmulatorType emulatorType = ((Emulator)data).getEmulatorType();
        LOG.assertTrue(emulatorType != null);
        return MobileModuleUtil.getMobileApplicationTypeByName(emulatorType.getApplicationType());
      }
    }
    return null;
  }

  public void setMobileApplicationType(MobileApplicationType mobileApplicationType) {
    myMobileApplicationType = mobileApplicationType;
  }

  public MobileModuleSettings getMobileModuleSettings() {
    if (myMobileModuleSettings == null) {
      if (myMobileApplicationType != null) {
        myMobileModuleSettings = myMobileApplicationType.createTempSettings(this);
      } else { //temp
        return getMobileApplicationType().createTempSettings(this);
      }
    }
    return myMobileModuleSettings;
  }

  public void setMobileModuleSettings(MobileModuleSettings mobileModuleSettings) {
    myMobileModuleSettings = mobileModuleSettings;
  }

  public boolean isExcludeFromContent() {
    return excludeFromContent;
  }

  public void setExcludeFromContent(boolean excludeFromContent) {
    this.excludeFromContent = excludeFromContent;
  }

  public ModuleType getModuleType() {
    return J2MEModuleType.getInstance();
  }

  public boolean isDefaultRDirectoryModified() {
    return myDefaultRDirectoryModified;
  }

  public void setDefaultRDirectoryModified(boolean defaultRDirectoryModified) {
    myDefaultRDirectoryModified = defaultRDirectoryModified;
  }

  public boolean isDefaultEDirectoryModified() {
    return myDefaultEDirectoryModified;
  }

  public void setDefaultEDirectoryModified(boolean defaultEDirectoryModified) {
    myDefaultEDirectoryModified = defaultEDirectoryModified;
  }

  public void setModuleJdk(final Sdk jdk) {
    super.setModuleJdk(jdk);
    final MobileApplicationType currentApplicationType = getCurrentApplicationType();
    if (currentApplicationType != myMobileApplicationType) {
      myMobileApplicationType = currentApplicationType;
      myMobileModuleSettings = null;
    }
  }
}
