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
package com.intellij.j2meplugin.emulator.doja;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.module.settings.doja.DOJAApplicationType;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.j2meplugin.run.J2MERunnableState;
import com.intellij.j2meplugin.run.states.doja.DOJARunnableState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NonNls;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * User: anna
 * Date: Oct 1, 2004
 */
public class DOJAEmulatorType extends EmulatorType {
  @NonNls private static final String ABOUT_1_PROPERTY = "ABOUT_1";
  @NonNls private static final String MAIN_WINDOW_TITLE = "MAIN_WINDOW_TITLE";
  @NonNls private static final String LIB = "lib";

  public String getName() {
    return "DoJa";
  }

  public String getApplicationType() {
    return DOJAApplicationType.NAME;
  }

  public String getDescriptorOption() {
    return "-i";
  }

  public String getDeviceOption() {
    return "-s";
  }

  public String getRelativePathToEmulator() {
    return "bin/doja_g";
  }

  public String suggestName(String homePath) {
    return getProperties(homePath).getProperty(ABOUT_1_PROPERTY);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  private static Properties getProperties(String homePath) {
    File i18properties = new File(homePath + File.separator + LIB + File.separator + "i18n" + File.separator + "I18N.properties");
    Properties prop = new Properties();
    try {
      prop.load(new BufferedInputStream(new FileInputStream(i18properties)));
    }
    catch (IOException e) {
    }
    return prop;
  }

  public boolean isValidHomeDirectory(String homePath) {
    @NonNls String property = getProperties(homePath).getProperty(MAIN_WINDOW_TITLE);
    if (property == null) return false;
    return property.equals("iappliTool");
  }

  public String[] getAvailableSkins(final String homePath) {
    @NonNls final String skin = "skin";
    File skins = new File(new File(homePath, LIB), skin);
    if (!skins.exists() || !skins.isDirectory()){
      return new String[]{"device1", "device2", "device3"};
    }
    final String[] strings = skins.list();
    ArrayList<String> devices = new ArrayList<String>();
    for (String device : strings) {
      if (new File(skins, device).isDirectory()) {
        devices.add(device);
      }
    }
    return devices.toArray(new String[devices.size()]);
  }

  public J2MERunnableState getJ2MERunnableState(Executor executor,
                                                RunnerSettings runnerSettings,
                                                ConfigurationPerRunnerSettings configurationSetting,
                                                J2MERunConfiguration configuration,
                                                Project project,
                                                Sdk projectJdk) {
    return new DOJARunnableState(executor,
                                 runnerSettings,
                                 configurationSetting,
                                 configuration,
                                 project,
                                 projectJdk);
  }
}
