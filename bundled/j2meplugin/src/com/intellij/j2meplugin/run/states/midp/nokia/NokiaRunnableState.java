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
package com.intellij.j2meplugin.run.states.midp.nokia;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.DefaultJavaProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.midp.nokia.ConfigurationUtil;
import com.intellij.j2meplugin.emulator.midp.nokia.NokiaEmulatorType;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.j2meplugin.run.J2MERunnableState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * User: anna
 * Date: Oct 8, 2004
 */
public class NokiaRunnableState extends J2MERunnableState {
  public NokiaRunnableState(Executor executor,
                            RunnerSettings runnerSettings,
                            ConfigurationPerRunnerSettings configurationSetting,
                            J2MERunConfiguration configuration, Project project, Sdk projectJdk) {
    super(executor, runnerSettings, configurationSetting, configuration, project, projectJdk);

  }

  protected ProcessHandler getExecutionProcess(String availablePort) throws ExecutionException {
    final Emulator emulator = (Emulator)myProjectJdk.getSdkAdditionalData();
    JavaParameters javaParameters = new JavaParameters();
    javaParameters.setJdk(emulator.getJavaSdk());
    @NonNls final String key = "kvem.main";
    javaParameters.setMainClass(ConfigurationUtil.getProperties(myProjectJdk.getHomePath()).getProperty(key));
    javaParameters.getClassPath().add(NokiaEmulatorType.getKvemPath(myProjectJdk.getHomeDirectory().getPath()));
    final String[] urls = myProjectJdk.getRootProvider().getUrls(OrderRootType.CLASSES);
    for (int k = 0; urls != null && k < urls.length; k++) {
      javaParameters.getClassPath().add(PathUtil.toPresentableUrl(urls[k]));
    }
    javaParameters.getVMParametersList().add("-Demulator.home=" + myProjectJdk.getHomeDirectory().getPath());
    ParametersList params = javaParameters.getProgramParametersList();
    if (myRunnerSettings.getData() instanceof DebuggingRunnerData) {
      params.add("-debugger");
      params.add("-dbg_port");
      params.add(availablePort);
    }
    if (!myConfiguration.IS_CLASSES) {
      params.add(myConfiguration.JAD_NAME);
    }
    else {
      final File tempJad = findFilesToDelete(myConfiguration.getModule());
      params.add(tempJad.getPath());
    }
    if (myConfiguration.COMMAND_LINE_PARAMETERS != null && myConfiguration.COMMAND_LINE_PARAMETERS.length() > 0) {
      params.add(myConfiguration.COMMAND_LINE_PARAMETERS);
    }
    return new DefaultJavaProcessHandler(javaParameters);
  }
}
