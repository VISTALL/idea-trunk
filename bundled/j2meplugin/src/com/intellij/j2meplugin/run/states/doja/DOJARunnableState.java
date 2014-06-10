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
package com.intellij.j2meplugin.run.states.doja;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.j2meplugin.run.J2MERunnableState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;

import java.io.File;

/**
 * User: anna
 * Date: Oct 1, 2004
 */
public class DOJARunnableState extends J2MERunnableState {
  public DOJARunnableState(Executor executor,
                           RunnerSettings runnerSettings,
                           ConfigurationPerRunnerSettings configurationSetting,
                           J2MERunConfiguration configuration, Project project, Sdk projectJdk) {
    super(executor, runnerSettings, configurationSetting, configuration, project, projectJdk);
  }


  protected ProcessHandler getExecutionProcess(String availablePort) throws ExecutionException {
    final Emulator emulator = (Emulator)myProjectJdk.getSdkAdditionalData();
    final EmulatorType emulatorType = emulator.getEmulatorType();
    LOG.assertTrue(emulatorType != null);
    GeneralCommandLine generalCommandLine = new GeneralCommandLine();
    generalCommandLine.setExePath(emulatorType.getPathToEmulator(myProjectJdk.getHomePath()));
    //final DeviceSpecificOption descriptor = emulator.getEmulatorType().getDeviceSpecificOptions().get(EmulatorType.DESCRIPTOR);



    if (myRunnerSettings.getData() instanceof DebuggingRunnerData) {
      generalCommandLine.addParameter("-debugger");
      generalCommandLine.addParameter("-suspend"); //todo -nosuspend
      generalCommandLine.addParameter("-port");
      generalCommandLine.addParameter(findFreePort());
      generalCommandLine.addParameter("-debugport");
      generalCommandLine.addParameter(availablePort);
      generalCommandLine.addParameter("-jdkpath");
      generalCommandLine.addParameter(emulator.getJavaSdk().getHomePath());
    }


    generalCommandLine.addParameter(emulatorType.getDescriptorOption());
    if (!myConfiguration.IS_CLASSES) {
      generalCommandLine.addParameter(myConfiguration.JAD_NAME);
    }
    else {
      final Module module = myConfiguration.getModule();
      final File tempJam = findFilesToDelete(module);
      generalCommandLine.addParameter(tempJam.getPath());
    }
    if (myConfiguration.TARGET_DEVICE_NAME != null && myConfiguration.TARGET_DEVICE_NAME.length() != 0) {
      generalCommandLine.addParameter(emulatorType.getDeviceOption());
      generalCommandLine.addParameter(myConfiguration.TARGET_DEVICE_NAME);
    }

    if (myConfiguration.COMMAND_LINE_PARAMETERS != null) {
      String[] params = myConfiguration.COMMAND_LINE_PARAMETERS.split(" ");
      for (int i = 0; params != null && i < params.length; i++) {
        generalCommandLine.addParameter(params[i]);
      }
    }
    generalCommandLine.setWorkDirectory(null);
    return new OSProcessHandler(generalCommandLine.createProcess(),
                                generalCommandLine.getCommandLineString());

  }

  private String findFreePort(){
    try {
      return DebuggerUtils.getInstance().findAvailableDebugAddress(true);
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
    return null;
  }

}
