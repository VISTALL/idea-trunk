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
package com.intellij.j2meplugin.emulator.midp.wtk;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.midp.MIDPEmulatorType;
import com.intellij.j2meplugin.emulator.midp.uei.UnifiedEmulatorType;
import com.intellij.j2meplugin.emulator.ui.MobileApiSettingsEditor;
import com.intellij.j2meplugin.emulator.ui.MobileDefaultApiEditor;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.j2meplugin.run.J2MERunnableState;
import com.intellij.j2meplugin.run.states.midp.uei.UEIRunnableState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;
import java.util.ArrayList;


/**
 * User: anna
 * Date: Sep 6, 2004
 */
public class WTKEmulatorType extends MIDPEmulatorType {
  @NonNls private static final String PROFILES = "profiles";
  @NonNls private static final String CONFIGURATIONS = "configurations";
  public static final String EMULATOR_TYPE_NAME = J2MEBundle.message("emulator.wtk.fullname");
  //@NonNls public static final String KVEM_PATH = "wtklib/kenv.zip;/wtklib/ktools.zip";

  @NonNls
  public String getName() {
    return EMULATOR_TYPE_NAME;
  }

  @Nullable
  @NonNls
  public String getUtilPath(final String home) {
    return toSystemDependentPath(home, "bin/utils");
  }

  @Nullable
  @NonNls
  public String getPrefPath(final String home) {
    return toSystemDependentPath(home, "bin/prefs");
  }

  public boolean isValidHomeDirectory(String homePath) {
    return ConfigurationUtil.isValidWTKHome(homePath);
  }

  public String[] getAvailableSkins(final String homePath) {
    final String exePath = getPathToEmulator(homePath);
    if (exePath != null) {
      final String[] devices = UnifiedEmulatorType.fillEmulatorDevices(exePath);
      if (devices != null) return devices;
    }
    return ConfigurationUtil.getWTKDevices(homePath);
  }

  public String[] getOTACommands() {
    return new String[]{UnifiedEmulatorType.INSTALL, UnifiedEmulatorType.FORCE, UnifiedEmulatorType.RUN, UnifiedEmulatorType.REMOVE, UnifiedEmulatorType.TRANSIENT, UnifiedEmulatorType.STORAGE_NAMES};
  }

  public String[] getApi(String homePath) {
    return ConfigurationUtil.getDefaultApiPath(homePath);
  }

  public MobileApiSettingsEditor getApiEditor(String homePath, Sdk sdk, SdkModificator sdkModificator) {
    final Properties apiSettings = ConfigurationUtil.getApiSettings(homePath);
    if (apiSettings == null || apiSettings.isEmpty()) {
      return new MobileDefaultApiEditor();
    }
    return new WTKApiEditor(this, sdk, sdkModificator);
  }

  public J2MERunnableState getJ2MERunnableState(Executor executor,
                                                RunnerSettings runnerSettings,
                                                ConfigurationPerRunnerSettings configurationSetting,
                                                J2MERunConfiguration configuration,
                                                Project project,
                                                Sdk projectJdk) {
    return new UEIRunnableState(executor, runnerSettings, configurationSetting, configuration, project, projectJdk);
  }

  public String[] getAvailableProfiles(@NotNull String homePath) {
    return getExistSettings(homePath, PROFILES);
  }

  public String[] getAvailableConfigurations(@NotNull String homePath) {
    return getExistSettings(homePath, CONFIGURATIONS);
  }

  //profiles, configurations
  @SuppressWarnings({"HardCodedStringLiteral"})
  static String[] getExistSettings(String homePath, String settingName) {
    Properties properties = ConfigurationUtil.getApiSettings(homePath);
    if (properties == null || properties.isEmpty()) {
      return null;
    }
    String profiles = properties.getProperty(settingName);
    if (profiles == null) {
      return null;
    }
    ArrayList<String> result = new ArrayList<String>();
    final String[] values = profiles.split("[, \n]");
    for (int i = 0; values != null && i < values.length; i++) {
      String value = values[i];
      final String stringInJad = properties.getProperty(value + ".jadValue");
      if (stringInJad != null && stringInJad.length() != 0){
        result.add(stringInJad.trim());
      }
    }
    return result.toArray(new String[result.size()]);
  }
  

  public String getDefaultProfile(@NotNull String homePath) {
    return ConfigurationUtil.getProfileVersion(homePath);
  }

  public String getDefaultConfiguration(@NotNull String homePath) {
    return ConfigurationUtil.getConfigurationVersion(homePath);
  }

  public static WTKEmulatorType getInstance() {
    return ApplicationManager.getApplication().getComponent(WTKEmulatorType.class);
  }
}
