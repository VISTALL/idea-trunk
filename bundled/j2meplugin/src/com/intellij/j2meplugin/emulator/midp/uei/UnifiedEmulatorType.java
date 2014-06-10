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
package com.intellij.j2meplugin.emulator.midp.uei;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.j2meplugin.emulator.midp.MIDPEmulatorType;
import com.intellij.j2meplugin.emulator.ui.MobileApiSettingsEditor;
import com.intellij.j2meplugin.emulator.ui.MobileDefaultApiEditor;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.j2meplugin.run.J2MERunnableState;
import com.intellij.j2meplugin.run.states.midp.uei.UEIRunnableState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.util.Key;
import org.apache.tools.ant.filters.StringInputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * User: anna
 * Date: Nov 14, 2004
 */
public class UnifiedEmulatorType extends MIDPEmulatorType {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");
  private String myProfile;
  private String myConfiguration;
  private String mySuggestedName;
  @NonNls
  public static final String INSTALL = "install";
  @NonNls
  public static final String FORCE = "force";
  @NonNls
  public static final String RUN = "run";
  @NonNls
  public static final String REMOVE = "remove";
  @NonNls
  public static final String TRANSIENT = "transient";
  @NonNls
  public static final String STORAGE_NAMES = "storageNames";

  @NonNls
  public String getName() {
    return "Unified Emulator Type";
  }

  public boolean isValidHomeDirectory(String homePath) {
    return fillEmulatorConfigurations(homePath);
  }

  public J2MERunnableState getJ2MERunnableState(Executor executor,
                                                RunnerSettings runnerSettings,
                                                ConfigurationPerRunnerSettings configurationSetting,
                                                J2MERunConfiguration configuration,
                                                Project project,
                                                Sdk projectJdk) {
    return new UEIRunnableState(executor,
                                runnerSettings,
                                configurationSetting,
                                configuration,
                                project,
                                projectJdk);
  }

  public String getDefaultProfile(@NotNull String home) {
    if (myProfile == null || myProfile.length() == 0) {
      fillEmulatorConfigurations(home);
    }
    return myProfile;
  }

  public String getDefaultConfiguration(@NotNull String home) {
    if (myConfiguration == null || myConfiguration.length() == 0) {
      fillEmulatorConfigurations(home);
    }
    return myConfiguration;
  }

  public MobileApiSettingsEditor getApiEditor(final String homePath, Sdk sdk, SdkModificator sdkModificator) {
    return new MobileDefaultApiEditor();
  }

  public String[] getAvailableSkins(final String homePath) {
    String exe = getPathToEmulator(homePath);
    return exe != null ? fillEmulatorDevices(exe) : null;
  }

  private boolean fillEmulatorConfigurations(String home) {
    Properties versionProps = getVersionProperties(home);
    if (versionProps == null) {
      return false;
    }
    myProfile = versionProps.getProperty("Profile");
    myConfiguration = versionProps.getProperty("Configuration");
    mySuggestedName = versionProps.getProperty(SDK_NAME);
    return (myProfile != null && myConfiguration != null && mySuggestedName != null);
  }

  @Nullable
  public static String[] fillEmulatorDevices(String exe) {
    final StringBuffer help = new StringBuffer();
    if (exe != null && exe.length() > 0) {
      GeneralCommandLine generalCommandLine = new GeneralCommandLine();
      generalCommandLine.setWorkDirectory(null);
      generalCommandLine.setExePath(exe);
      @NonNls final String query = "-Xquery";
      generalCommandLine.addParameter(query);
      try {
        OSProcessHandler osProcessHandler = new OSProcessHandler(generalCommandLine.createProcess(),
                                                                 generalCommandLine.getCommandLineString());
        osProcessHandler.addProcessListener(new ProcessAdapter() {
          public void onTextAvailable(ProcessEvent event, Key outputType) {
            help.append(event.getText());
          }
        });
        osProcessHandler.startNotify();
        osProcessHandler.waitFor();
        osProcessHandler.destroyProcess();
      }
      catch (ExecutionException e) {
        LOG.error(e);
      }
      StringInputStream in = null;
      Properties properties = new Properties();
      try {
        try {
          in = new StringInputStream(help.toString());
          properties.load(in);
          @NonNls final String key = "device.list";
          final String devices = properties.getProperty(key);
          if (devices != null) {
            return devices.split(", ");
          }
          else {
            return null;
          }
        }
        finally {
          if (in != null) {
            in.close();
          }
        }
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
    return null;
  }

  @Nullable
  public String[] getOTACommands(String homeDir) {
    String exe = getPathToEmulator(homeDir);
    if (exe != null && exe.length() != 0) {
      GeneralCommandLine generalCommandLine = new GeneralCommandLine();
      generalCommandLine.setWorkDirectory(null);
      generalCommandLine.setExePath(exe);
      generalCommandLine.addParameter("-help");
      try {
        OSProcessHandler osProcessHandler = new OSProcessHandler(generalCommandLine.createProcess(),
                                                                 generalCommandLine.getCommandLineString());
        @NonNls final StringBuffer buffer = new StringBuffer();
        osProcessHandler.addProcessListener(new ProcessAdapter() {
          public void onTextAvailable(ProcessEvent event, Key outputType) {
            buffer.append(event.getText());
          }
        });
        osProcessHandler.startNotify();
        osProcessHandler.waitFor();
        osProcessHandler.destroyProcess();
        if (buffer.length() != 0 && buffer.indexOf("-Xjam") != -1) {
          @NonNls String otaCommands = buffer.substring(buffer.indexOf("-Xjam") + "-Xjam".length());
          final int endIndex = otaCommands.indexOf("-X");
          if (endIndex > -1) {
            otaCommands = otaCommands.substring(0, endIndex);
          }
          ArrayList<String> result = new ArrayList<String>();
          if (otaCommands.indexOf(INSTALL) > -1) result.add(INSTALL);
          if (otaCommands.indexOf(FORCE) > -1) result.add(FORCE);
          if (otaCommands.indexOf(RUN) > -1) result.add(RUN);
          if (otaCommands.indexOf(REMOVE) > -1) result.add(REMOVE);
          if (otaCommands.indexOf(TRANSIENT) > -1) result.add(TRANSIENT);
          if (otaCommands.indexOf(STORAGE_NAMES) > -1) result.add(STORAGE_NAMES);
          return result.toArray(new String[result.size()]);
        }
      }
      catch (ExecutionException e) {
        return null;
      }
    }
    return null;
  }
}
