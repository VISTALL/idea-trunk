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
package com.intellij.j2meplugin.emulator.midp;

import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.emulator.ui.MobileApiSettingsEditor;
import com.intellij.j2meplugin.module.settings.midp.MIDPApplicationType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.ExecutionException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

/**
 * User: anna
 * Date: Nov 17, 2004
 */
public abstract class MIDPEmulatorType extends EmulatorType {
  protected static final String SDK_NAME = "_SDK_NAME";
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");
  
  @NonNls
  public String getApplicationType() {
    return MIDPApplicationType.NAME;
  }

  @NonNls
  @Nullable
  public String getDescriptorOption() {
    return "-Xdescriptor:";
  }

  @Nullable
  @NonNls
  public String getDeviceOption() {
    return "-Xdevice:";
  }

  @Nullable
  @NonNls
  public String getRelativePathToEmulator() {
    return "bin/emulator";
  }

  protected Properties getVersionProperties(String home) {
    String emulatorExe = getPathToEmulator(home);

    if (emulatorExe == null || emulatorExe.length() < 1) {
      return null;
    }
    String versionOutput = getExeOutput(emulatorExe, "-version");
    if (versionOutput == null || versionOutput.length() < 4) {
      return null;
    }
    return convertVersionOutputToProperties(versionOutput);
  }

  static Properties convertVersionOutputToProperties(String versionOutput) {
    BufferedReader versionOutputLines = new BufferedReader(new StringReader(versionOutput), 512);
    String line;
    int lineNumber = 0;
    Properties props = new Properties();
    while ( (line = readLine(versionOutputLines)) != null) {
      if (lineNumber == 1) {
        props.setProperty(SDK_NAME, line);
      } else if (lineNumber > 1) {
        int colon = line.indexOf(':');
        if (colon > 0 && (colon+1) < line.length()) {
          String key = line.substring(0, colon);
          String value = line.substring(colon + 1).trim();
          props.setProperty(key, value);
        }
      }
      ++lineNumber;
    }
    return props;
  }

  @Nullable
  private static String readLine(BufferedReader br) {
    try {
      return br.readLine();
    } catch (IOException ignored) {
      return null;
    }
  }

  @Nullable
  protected static String getExeOutput(String exe, String param) {
     final StringBuffer output = new StringBuffer();
     if (exe == null || exe.length() < 1) {
         return null;
     }
     GeneralCommandLine generalCommandLine = new GeneralCommandLine();
     generalCommandLine.setWorkDirectory(null);
     generalCommandLine.setExePath(exe);
     generalCommandLine.addParameter(param);
     try {
        OSProcessHandler osProcessHandler = new OSProcessHandler(generalCommandLine.createProcess(),
                                                                 generalCommandLine.getCommandLineString());
        osProcessHandler.addProcessListener(new ProcessAdapter() {
          public void onTextAvailable(ProcessEvent event, Key outputType) {
            output.append(event.getText());
          }
        });
        osProcessHandler.startNotify();
        osProcessHandler.waitFor();
        osProcessHandler.destroyProcess();
      }
      catch (ExecutionException e) {
        LOG.error(e);
      }
      return output.toString();
  }  

  @Nullable
  public String suggestName(String homePath) {
    Properties versionProps = getVersionProperties(homePath);
    if (versionProps == null) {
      return null;
    }
    return versionProps.getProperty(SDK_NAME);
  }
  
  @NonNls
  public abstract String getDefaultProfile(@NotNull String home);

  public String[] getAvailableProfiles(@NotNull String homePath) {
    return new String[] {getDefaultProfile(homePath)};
  }

  @NonNls
  public abstract String getDefaultConfiguration(@NotNull String home);

  public String[] getAvailableConfigurations(@NotNull String homePath) {
    return new String[] {getDefaultConfiguration(homePath)};
  }

  public abstract MobileApiSettingsEditor getApiEditor(final String homePath, Sdk sdk, SdkModificator sdkModificator);
}
