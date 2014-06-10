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
package com.intellij.j2meplugin.module.settings.ui;

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.J2MEModuleProperties;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.util.MobileIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * User: anna
 * Date: Oct 6, 2004
 */
public class MobileBuildSettings implements ModuleConfigurationEditor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");
  private final Module myModule;
  private final ModifiableRootModel myRootModel;
  private final MobileExplodedPanel myExplodedPanel;
  private final MobileBuildPanel myBuildPanel;
  private final MobileModuleResourcesSettings myResourcesSettings;

  public MobileBuildSettings(Module module, ModifiableRootModel rootModel) {
    myModule = module;
    myRootModel = rootModel;
    final VirtualFile explodedDirectory = rootModel.getExplodedDirectory();
    final String defaultExplodedPath =
      explodedDirectory != null ? explodedDirectory.getPath() : ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        public String compute() {
          @NonNls final String output = "output";
          return new File(myModule.getModuleFilePath()).getParent().replace('/', File.separatorChar) + File.separatorChar + output;
        }
      });
    myExplodedPanel =
    new MobileExplodedPanel(explodedDirectory != null,
                            rootModel.isExcludeExplodedDirectory(), defaultExplodedPath);
    final MobileModuleSettings mobileModuleSettings = MobileModuleSettings.getInstance(myModule);
    myBuildPanel = new MobileBuildPanel(J2MEModuleProperties.getInstance(myModule).getMobileApplicationType(),
                                        myModule.getProject(),
                                        mobileModuleSettings);
    myResourcesSettings = new MobileModuleResourcesSettings(module, rootModel);
  }

  public void saveData() {}

  public void moduleStateChanged() {}

  public String getDisplayName() {
    return J2MEBundle.message("mobile.build.settings.title");
  }

  public Icon getIcon() {
    return MobileIcons.MOBILE_COMPILE;
  }

  public String getHelpTopic() {
    return "j2me.moduleJ2ME";
  }

  public JComponent createComponent() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(myBuildPanel.createComponent(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                                                            GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    panel.add(myExplodedPanel.getComponent(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                                                            GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    final JPanel resourcesPanel = (JPanel)myResourcesSettings.createComponent();
    resourcesPanel.setBorder(BorderFactory.createTitledBorder(myResourcesSettings.getDisplayName()));
    panel.add(resourcesPanel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                                                                            GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    return panel;
  }

  public boolean isModified() {
    return myBuildPanel.isModified() || myExplodedPanel.isModified() || myResourcesSettings.isModified();
  }

  public void apply() throws ConfigurationException {
    myBuildPanel.apply();
    myExplodedPanel.apply();
    if (myExplodedPanel.isPathEnabled()) {
      final String path = myExplodedPanel.getExplodedDir();
      if (!new File(path).exists()) {
        /* if (Messages.showYesNoDialog(myModule.getProject(),
                                         "Exploded directory " + path + " doesn't exist. \n " +
                                         "Would you like to create it and continue?",
                                         "Exploded directory doesn't exist.",
                                         null) != DialogWrapper.OK_EXIT_CODE) {
           return;
         }
   */
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            String pathDir = FileUtil.toSystemIndependentName(path);
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(pathDir);
            if (file == null) {
              final File ioFile = new File(path);
              CommandProcessor.getInstance().executeCommand(myModule.getProject(), new Runnable() {
                public void run() {
                  FileUtil.createParentDirs(ioFile);
                  final VirtualFile dir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(ioFile.getParentFile());
                  if (dir != null) {
                    try {
                      dir.createChildDirectory(this, ioFile.getName());
                    }
                    catch (IOException e) {
                      LOG.error(e);
                    }
                  }
                }
              }, J2MEBundle.message("exploded.directory.create.command"), null);
            }
          }
        });
      }

      String canonicalPath;
      try {
        canonicalPath = "".equals(path) ? null : new File(path).getCanonicalPath();
      }
      catch (IOException e) {
        canonicalPath = path;
      }

      final String url = (canonicalPath == null)
                         ? null
                         : VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, canonicalPath.replace(File.separatorChar, '/'));
      myRootModel.setExplodedDirectory(url);
      myRootModel.setExcludeExplodedDirectory(myExplodedPanel.isExcludeFromContent());
    }
    else {
      VirtualFile exploded = null;
      myRootModel.setExplodedDirectory(exploded);
    }
    myResourcesSettings.apply();
  }

  public void reset() {
    myBuildPanel.reset();
    myExplodedPanel.reset();
    myResourcesSettings.reset();
  }

  public void disposeUIResources
    () {
  }
}
