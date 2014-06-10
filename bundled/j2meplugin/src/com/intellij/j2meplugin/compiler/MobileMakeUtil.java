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
package com.intellij.j2meplugin.compiler;

import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.general.UserDefinedOption;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.util.ActionRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;


/**
 * User: anna
 * Date: Oct 30, 2004
 */
public class MobileMakeUtil {
  private MobileMakeUtil() {
  }

  private static void makeJad(final MobileModuleSettings settings, String jadPath, boolean leaveSettings)
    throws Exception {
    final MobileApplicationType applicationType = settings.getApplicationType();
    String separator = applicationType.getSeparator();
    File jadFile = new File(jadPath);

    if (!leaveSettings) {
      final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(jadFile);
      if (virtualFile != null) {
        if (ReadonlyStatusHandler.getInstance(settings.getModule().getProject()).ensureFilesWritable(virtualFile).hasReadonlyFiles()) {
          return;
        }
      }
    }
    File jarFile = new File(settings.getJarURL());
    if (jarFile.exists()) {
      settings.prepareJarSettings();
    }
    BufferedReader is = null;
    PrintWriter printWriter = null;
    try {
      if (settings.isSynchronized() && leaveSettings){
        //replace Jar-Size setting
        final String jarSizeSettingName = applicationType.getJarSizeSettingName();
        final String size = settings.getSettings().get(jarSizeSettingName);
        final String jarSizeLine = jarSizeSettingName + separator + " " + size;
        boolean sizeWasDeleted = true;
        ArrayList<String> lineStore = new ArrayList<String>();
        is = new BufferedReader(new FileReader(jadFile));
        String line = is.readLine();
        while (line != null){
          final String name = line.substring(0, line.indexOf(separator));
          if (name.trim().compareToIgnoreCase(jarSizeSettingName) != 0){
            lineStore.add(line);
          } else {
            sizeWasDeleted = false;
            lineStore.add(jarSizeLine);
          }
          line = is.readLine();
        }
        printWriter = new PrintWriter(new BufferedWriter(new FileWriter(jadFile)));
        if (sizeWasDeleted) printWriter.println(jarSizeLine);
        for (String byLine : lineStore) {
          printWriter.println(byLine);
        }
      } else {
        printWriter = new PrintWriter(new BufferedWriter(new FileWriter(jadFile)));
        for (String key : settings.getSettings().keySet()) {
          printWriter.println(key + separator + " " + (settings.isMidletKey(key) ? settings.getSettings().get(key).replaceAll(",", ", ") : settings.getSettings().get(key)));
        }

        for (UserDefinedOption key : settings.getUserDefinedOptions()) {
          printWriter.println(key.getKey() + separator + " " + key.getValue());
        }
      }
    }
    finally {
      if (is != null) is.close();
      if (printWriter != null) printWriter.close();
    }
    ActionRunner.runInsideWriteAction(new ActionRunner.InterruptibleRunnable() {
      public void run() {
        LocalFileSystem.getInstance().refresh(false);
      }
    });
  }

  public static void makeJad(MobileModuleSettings settings, boolean leaveSettings) throws Exception {
    final String jadPath = settings.getMobileDescriptionPath().replace(File.separatorChar, '/');
    makeJad(settings, jadPath, leaveSettings);
  }

  public static void getDependencies(Module module, Set<Module> modules) {
    if (modules.contains(module)) return;
    Module[] dependencies = ModuleRootManager.getInstance(module).getDependencies();
    for (Module dependency : dependencies) {
      modules.add(dependency);
      if (!modules.contains(dependency)) {  //circ dependencies
        getDependencies(dependency, modules);
      }
    }
  }

}
