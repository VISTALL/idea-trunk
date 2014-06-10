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
package com.intellij.j2meplugin.module.settings;

import com.intellij.j2meplugin.compiler.MobileMakeUtil;
import com.intellij.j2meplugin.module.settings.general.UserDefinedOption;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ActionRunner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: anna
 * Date: Sep 23, 2004
 */
public abstract class MobileSettingsConfigurable implements UnnamedConfigurable {
  protected MobileModuleSettings mySettings;
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");
  protected Module myModule;
  protected Project myProject;

  protected MobileSettingsConfigurable(Module module, final MobileModuleSettings settings, final Project project) {
    mySettings = settings;
    myModule = module;
    myProject = project;
  }


  public abstract void disableMidletProperties();

  public void reset() {
    if (mySettings.isSynchronized()) {
      try {
        ActionRunner.runInsideWriteAction(new ActionRunner.InterruptibleRunnable() {
          public void run() throws Exception {
            final VirtualFile descriptor = LocalFileSystem.getInstance().refreshAndFindFileByPath(
              mySettings.getMobileDescriptionPath().replace(File.separatorChar, '/'));
            final FileDocumentManager documentManager = FileDocumentManager.getInstance();
            if (descriptor != null) {
              final Document document = documentManager.getDocument(descriptor);
              if (document != null) {
                documentManager.saveDocument(document);
              }
            }
          }
        });
        Properties properties = new Properties();
        final File descriptor = new File(mySettings.getMobileDescriptionPath());
        if (descriptor.exists()) {
          final InputStream is = new BufferedInputStream(new FileInputStream(descriptor));
          try {
            properties.load(is);
            mySettings.getSettings().clear();
            mySettings.getUserDefinedOptions().clear();
            for (final Object o : properties.keySet()) {
              String key = (String)o;
              if (mySettings.getApplicationType().isUserField(key)) {
                mySettings.getUserDefinedOptions().add(new UserDefinedOption(key, properties.getProperty(key)));
              }
              else {
                mySettings.getSettings().put(key, properties.getProperty(key));
              }
            }
          }
          finally {
            is.close();
          }
        }
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  public void apply() throws ConfigurationException {
    if (mySettings.isSynchronized()) {
      try {
        MobileMakeUtil.makeJad(mySettings, false);
      }
      catch (Exception e) {
        throw new ConfigurationException(e.getMessage());
      }
    }
  }


  public MobileModuleSettings getSettings() {
    return mySettings;
  }
}
