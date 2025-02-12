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
package com.intellij.j2meplugin.run;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.PatchedRunnableState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.module.J2MEModuleProperties;
import com.intellij.j2meplugin.module.MobileModuleUtil;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.general.UserDefinedOption;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;

/**
 * User: anna
 * Date: Sep 30, 2004
 */
public class J2MERunnableState implements PatchedRunnableState {
  protected static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");
  protected RunnerSettings myRunnerSettings;
  protected ConfigurationPerRunnerSettings myConfigurationSetting;
  protected J2MERunConfiguration myConfiguration;
  protected Sdk myProjectJdk;
  protected Project myProject;
  protected ArrayList<File> myFilesToDelete = new ArrayList<File>();

  private String myDebugPort = "";
  private final Executor myExecutor;

  public J2MERunnableState(Executor executor,
                           RunnerSettings runnerSettings,
                           ConfigurationPerRunnerSettings configurationSetting,
                           J2MERunConfiguration configuration,
                           Project project,
                           Sdk projectJdk) {
    myExecutor = executor;
    myRunnerSettings = runnerSettings;
    myConfigurationSetting = configurationSetting;
    myConfiguration = configuration;
    myProject = project;
    myProjectJdk = projectJdk;
    if (myRunnerSettings.getData() instanceof DebuggingRunnerData) {
      myDebugPort = ((DebuggingRunnerData)myRunnerSettings.getData()).getDebugPort();
      if (myDebugPort.length() == 0){
        try {
          myDebugPort = DebuggerUtils. getInstance().findAvailableDebugAddress(true);
        }
        catch (ExecutionException e) {
          LOG.error(e);
        }
        ((DebuggingRunnerData)myRunnerSettings.getData()).setDebugPort(myDebugPort);
      }
      ((DebuggingRunnerData)myRunnerSettings.getData()).setLocal(false);
    }
  }

  public RunnerSettings getPatcher() {
    return myRunnerSettings;
  }

  public RunnerSettings getRunnerSettings() {
    return myRunnerSettings;
  }

  public ConfigurationPerRunnerSettings getConfigurationSettings() {
    return myConfigurationSetting;
  }

  public ExecutionResult execute(final Executor executor, @NotNull final ProgramRunner runner) throws ExecutionException {
    if (myProjectJdk == null) throw new ExecutionException(J2MEBundle.message("run.configuration.invalid.jdk"));
    final EmulatorType emulatorType = ((Emulator)myProjectJdk.getSdkAdditionalData()).getEmulatorType();
    LOG.assertTrue(emulatorType != null);
    final MobileApplicationType mobileApplicationType = MobileModuleUtil.getMobileApplicationTypeByName(
      emulatorType.getApplicationType());
    if (!myConfiguration.IS_OTA) {
      if (myConfiguration.IS_CLASSES) {
        if ((myConfiguration.MAIN_CLASS_NAME == null || myConfiguration.MAIN_CLASS_NAME.length() == 0)) {
          throw new ExecutionException(J2MEBundle.message("run.configuration.no.class.specified", mobileApplicationType.getPresentableClassName()) );
        }
      }
      else {
        if (myConfiguration.JAD_NAME == null || myConfiguration.JAD_NAME.length() == 0) {
          throw new ExecutionException(J2MEBundle.message("run.configuration.no.file.specified", mobileApplicationType.getExtension()));
        }
        if (!myConfiguration.JAD_NAME.endsWith(mobileApplicationType.getExtension())) {
          throw new ExecutionException(J2MEBundle.message("run.configuration.mistyped.descriptor", myConfiguration.JAD_NAME, mobileApplicationType.getName()));
        }
      }
    }
    else {
      if (myConfiguration.TO_START == null || myConfiguration.TO_START.length() == 0) {
        throw new ExecutionException(J2MEBundle.message("run.configuration.no.file.specified", mobileApplicationType.getExtension()));
      }
    }
    final ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(myProject).getConsole();
    final ProcessHandler processHandler = getExecutionProcess(myDebugPort);
    processHandler.addProcessListener(new ProcessAdapter() {

      public void processTerminated(final ProcessEvent event) {
        for (final File file : myFilesToDelete) {
          if (file != null && file.exists()) {
            FileUtil.delete(file);
          }
        }
      }
    });
    console.attachToProcess(processHandler);
    ProcessTerminatedListener.attach(processHandler);
    return new DefaultExecutionResult(console,
                                      processHandler);
  }

  protected ProcessHandler getExecutionProcess(String availablePort) throws ExecutionException {
    return null;
  }

  @Nullable
  private File createTempJad(final Module module) {
    PrintWriter printWriter = null;
    try {
      final MobileApplicationType mobileApplicationType = J2MEModuleProperties.getInstance(module).getMobileApplicationType();
      MobileModuleSettings settings = MobileModuleSettings.getInstance(module);
      String separator = mobileApplicationType.getSeparator();

      @NonNls final String prefix = "temp";
      @NonNls final String childName = "caches";
      File jadFile = File.createTempFile(prefix, "." + mobileApplicationType.getExtension(),
                                         new File(PathManager.getSystemPath(), childName));
      jadFile.deleteOnExit();

      File jarFile = new File(settings.getJarURL());
      if (!jarFile.exists()) return null; //request rebuild

      settings.prepareJarSettings();

      printWriter = new PrintWriter(new BufferedWriter(new FileWriter(jadFile)));

      printWriter.println(mobileApplicationType.createConfigurationByClass(myConfiguration.MAIN_CLASS_NAME));
      @NonNls final String jarExtension = ".jar";
      printWriter.println(
        mobileApplicationType.getJarUrlSettingName() + separator + " " +
        jadFile.getName().replaceAll("." + mobileApplicationType.getExtension(), jarExtension));
      for (String key : settings.getSettings().keySet()) {
        if (!settings.isMidletKey(key) && !key.equals(mobileApplicationType.getJarUrlSettingName())) {
          printWriter.println(key + separator + " " + settings.getSettings().get(key));
        }
      }


      for (UserDefinedOption key : myConfiguration.userParameters) {
        printWriter.println(key.getKey() + separator + " " + key.getValue());
      }

      printWriter.close();

      return jadFile;
    }
    catch (IOException e) {
      LOG.error(e);
    } finally {
      if (printWriter != null){
        printWriter.close();
      }
    }
    return null;
  }

  protected File findFilesToDelete(final Module module) throws ExecutionException {
    final File tempJad = createTempJad(module);
    if (tempJad == null) {
      throw new ExecutionException(J2MEBundle.message("run.configuration.rebuild.needed"));
    }
    final MobileApplicationType mobileApplicationType = J2MEModuleProperties.getInstance(module).getMobileApplicationType();
    MobileModuleSettings settings = MobileModuleSettings.getInstance(module);
    myFilesToDelete.add(tempJad);
    @NonNls final String jarExtension = ".jar";
    final File toJarFile = new File(tempJad.getPath().replaceAll("." + mobileApplicationType.getExtension(), jarExtension));
    myFilesToDelete.add(toJarFile);
    try {
      FileUtil.copy(new File(FileUtil.toSystemDependentName(settings.getJarURL())),
                    toJarFile);
    }
    catch (IOException e) {
      throw new ExecutionException(e.getMessage());
    }
    return tempJad;
  }
}
