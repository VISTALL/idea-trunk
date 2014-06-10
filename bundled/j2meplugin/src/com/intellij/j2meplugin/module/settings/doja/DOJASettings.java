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
package com.intellij.j2meplugin.module.settings.doja;

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.J2MEModuleBuilder;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * User: anna
 * Date: Sep 19, 2004
 */
public class DOJASettings extends MobileModuleSettings {
  public DOJASettings() {}

  public DOJASettings(Module module) {
    super(module);
  }

  public void copyTo(MobileModuleSettings mobileModuleSettings) {
    if (!(mobileModuleSettings instanceof DOJASettings)) return;
    super.copyTo(mobileModuleSettings);
  }

  public void initSettings(J2MEModuleBuilder moduleBuilder) {
    super.initSettings(moduleBuilder);
    if (myDefaultModified) {
      putIfNotExists(DOJAApplicationType.APPLICATION_NAME, moduleBuilder.getName());
    }
    else {
      putSetting(DOJAApplicationType.APPLICATION_NAME, moduleBuilder.getName());
    }
  }

  public void prepareJarSettings() {
    super.prepareJarSettings();
    final String lastModified = new SimpleDateFormat(J2MEBundle.message("doja.time.format")).format(new Date(new File(myJarURL).lastModified()));
    LOG.assertTrue(lastModified != null);
    if (myJarURL != null) {
      putSetting(DOJAApplicationType.LAST_MODIFIED,
                 lastModified);
    }
  }

  @Nullable
  public File getManifest() {
    return null;
  }

  public SortedSet<String> getMIDlets() {
    TreeSet<String> treeSet = new TreeSet<String>();
    final String appClass = properties.get(DOJAApplicationType.APPLICATION_CLASS);
    if (appClass != null) {
      treeSet.add(DOJAApplicationType.APPLICATION_CLASS);
    }
    return treeSet;
  }

  public void setMIDletClassName(final String name, final String className) {
    properties.put(name, className);
  }

  public String getMIDletClassName(final String midletKey) {
    return properties.get(midletKey);
  }

  public boolean isMidletKey(final String key) {
    return Comparing.strEqual(key, DOJAApplicationType.APPLICATION_CLASS);
  }

  public MobileApplicationType getApplicationType() {
    return DOJAApplicationType.getInstance();
  }

  public void addMidlet(final String qualifiedName) {
    final String currentApplication = properties.get(DOJAApplicationType.APPLICATION_CLASS);
    if (currentApplication != null) {
      if (Messages.showYesNoDialog(J2MEBundle.message("doja.configuration.contains.executable.class.dialog.message", currentApplication, qualifiedName),
                                   J2MEBundle.message("doja.configuration.contains.executable.class.dialog.title"), Messages.getWarningIcon()) != DialogWrapper.OK_EXIT_CODE) {
        return;
      }
    }
    properties.put(DOJAApplicationType.APPLICATION_CLASS, qualifiedName);
    super.addMidlet(qualifiedName);
  }

  public boolean containsMidlet(final String qualifiedName) {
    return Comparing.strEqual(properties.get(DOJAApplicationType.APPLICATION_CLASS).trim(), qualifiedName);
  }

}
