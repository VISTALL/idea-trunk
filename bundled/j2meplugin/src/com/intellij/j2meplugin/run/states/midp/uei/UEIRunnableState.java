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
package com.intellij.j2meplugin.run.states.midp.uei;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.emulator.midp.uei.UnifiedEmulatorType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.midp.MIDPApplicationType;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.j2meplugin.run.J2MERunnableState;
import com.intellij.j2meplugin.run.ui.OTASettingsConfigurable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NonNls;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User: anna
 * Date: Oct 1, 2004
 */
public class UEIRunnableState extends J2MERunnableState {

  public UEIRunnableState(Executor executor,
                          RunnerSettings runnerSettings,
                          ConfigurationPerRunnerSettings configurationSetting,
                          J2MERunConfiguration configuration, Project project, Sdk projectJdk) {
    super(executor, runnerSettings, configurationSetting, configuration, project, projectJdk);
  }

  protected ProcessHandler getExecutionProcess(String availablePort) throws ExecutionException {
    final Emulator emulator = (Emulator)myProjectJdk.getSdkAdditionalData();
    final EmulatorType emulatorType = emulator.getEmulatorType();
    LOG.assertTrue(emulatorType != null);
    final Module module = myConfiguration.getModule();
    GeneralCommandLine generalCommandLine = new GeneralCommandLine();
    generalCommandLine.setExePath(emulatorType.getPathToEmulator(myProjectJdk.getHomePath()));
    if (!myConfiguration.IS_OTA) {
      generalCommandLine.addParameter("-classpath");
      final String[] urls = myProjectJdk.getRootProvider().getUrls(OrderRootType.CLASSES);
      String classpath = "";
      for (int k = 0; urls != null && k < urls.length; k++) {
        classpath += PathUtil.toPresentableUrl(urls[k]) + File.pathSeparator;//(k != urls.length - 1 ? File.pathSeparator : "");
      }
      if (!myConfiguration.IS_CLASSES) {
        Properties properties = new Properties();
        try {
          properties.load(new BufferedInputStream(new FileInputStream(myConfiguration.JAD_NAME)));
        }
        catch (IOException e) {
          throw new ExecutionException(e.getMessage());
        }
        String jar = properties.getProperty(MIDPApplicationType.MIDLET_JAR_URL);
        if (jar == null) { //todo
          throw new ExecutionException(J2MEBundle.message("run.configuration.jar.not.specified.error"));
        }
        if (!new File(jar).exists()) {
          jar = myConfiguration.JAD_NAME.substring(0, myConfiguration.JAD_NAME.lastIndexOf(File.separator) + 1) +
                jar;
        }
        classpath += jar;
        generalCommandLine.addParameter(classpath);
        generalCommandLine.addParameter(emulatorType.getDescriptorOption() + myConfiguration.JAD_NAME);

      }
      else {
        final File tempJad = findFilesToDelete(module);
        classpath += MobileModuleSettings.getInstance(module).getJarURL();
        generalCommandLine.addParameter(classpath);
        generalCommandLine.addParameter(emulatorType.getDescriptorOption() + tempJad.getPath());
      }
    }
    else {
      String[] commands = (getJAMCommand(myConfiguration.SELECTION) + myConfiguration.TO_START).split(" ");
      generalCommandLine.addParameters(commands);
    }
    if (myRunnerSettings.getData() instanceof DebuggingRunnerData) {
      generalCommandLine.addParameter("-Xdebug");
      generalCommandLine.addParameter("-Xrunjdwp:transport=dt_socket,address=" + availablePort + ",server=y");
    }
    if (myConfiguration.TARGET_DEVICE_NAME != null && myConfiguration.TARGET_DEVICE_NAME.length() > 0) {
      generalCommandLine.addParameter(emulatorType.getDeviceOption() + myConfiguration.TARGET_DEVICE_NAME);
    }
    if (myConfiguration.COMMAND_LINE_PARAMETERS != null && myConfiguration.COMMAND_LINE_PARAMETERS.length() > 0) {
      generalCommandLine.addParameter(myConfiguration.COMMAND_LINE_PARAMETERS);
    }
    generalCommandLine.setWorkDirectory(myProjectJdk.getHomePath());
    return new OSProcessHandler(generalCommandLine.createProcess(),
                                generalCommandLine.getCommandLineString());

  }

  public static String getJAMCommand(int command) {
    @NonNls String result = "-Xjam:";
    if (command == OTASettingsConfigurable._INSTALL) {
      result += UnifiedEmulatorType.INSTALL;
    }
    else {
      if (command == OTASettingsConfigurable._RUN) {
        result += UnifiedEmulatorType.RUN;
      }
      else {
        if (command == OTASettingsConfigurable._REMOVE) {
          result += UnifiedEmulatorType.REMOVE;
        }
        else {
          if (command == OTASettingsConfigurable._TRANSIENT) {
            result += UnifiedEmulatorType.TRANSIENT;
          }
        }
      }
    }
    return result + "=";
  }
}
