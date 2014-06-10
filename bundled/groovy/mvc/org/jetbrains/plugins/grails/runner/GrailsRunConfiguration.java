/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.runner;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.config.GrailsConfigUtils;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.mvc.MvcRunConfiguration;
import org.jetbrains.plugins.groovy.mvc.MvcRunConfigurationEditor;

import javax.swing.*;

public class GrailsRunConfiguration extends MvcRunConfiguration {
  public boolean launchBrowser = true;

  @NonNls private static final String GRAILS_DISABLE_AUTO_RECOMPILE = "-Ddisable.auto.recompile=";
  @NonNls private static final String GRAILS_RECOMPILE_FREQ = "-Drecompile.frequency=";
  @NonNls private static final String GRAILS_SERVER_PORT = "-Dserver.port=";
  @NonNls private static final String GRAILS_SERVER_HOST = "-Dserver.host=";
  @NonNls private static final String GRAILS_ENABLE_JNDI = "-Denable.jndi=";


  private final GrailsConfigurationFactory factory;

  @NonNls private static final String SERVER_RUNNING_BROWSE_TO = "Server running. Browse to ";

  public GrailsRunConfiguration(GrailsConfigurationFactory factory, Project project, String name, String cmdLine) {
    super(name, new RunConfigurationModule(project), factory, GrailsFramework.INSTANCE);
    this.factory = factory;
    this.cmdLine = cmdLine;
  }

  @Override
  public MvcRunConfigurationEditor getConfigurationEditor() {
    final JCheckBox launchBrowser = new JCheckBox("Launch browser");

    final MvcRunConfigurationEditor<GrailsRunConfiguration> editor = new MvcRunConfigurationEditor<GrailsRunConfiguration>() {
      @Override
      protected void commandLineChanged(@NotNull String newText) {
        super.commandLineChanged(newText);
        setCBEnabled(newText.contains("run-app"), launchBrowser);
      }

      @Override
      public void resetEditorFrom(GrailsRunConfiguration configuration) {
        super.resetEditorFrom(configuration);
        launchBrowser.setSelected(configuration.launchBrowser);
      }

      @Override
      public void applyEditorTo(GrailsRunConfiguration configuration) throws ConfigurationException {
        super.applyEditorTo(configuration);
        configuration.launchBrowser = launchBrowser.isSelected();
      }

    };
    editor.addExtension(launchBrowser);
    return editor;
  }

  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);

    launchBrowser = !"false".equals(JDOMExternalizer.readString(element, "launchBrowser"));

    if (StringUtil.isNotEmpty(cmdLine)) {
      return;
    }

    final String testNames = JDOMExternalizer.readString(element, "names");
    if (testNames != null) {
      cmdLine = "test-app";

      final int testType = JDOMExternalizer.readInteger(element, "type", 0);
      if (testType == 1) {
        cmdLine += " -integration";
      } else if (testType == 2) {
        cmdLine += " -unit";
      }

      if (StringUtil.isNotEmpty(testNames)) {
        cmdLine += " " + testNames;
      }

      return;
    }

    cmdLine = "run-app";

    String grailsParams = JDOMExternalizer.readString(element, "grailsparams");
    if (StringUtil.isNotEmpty(grailsParams)) {
      cmdLine += " " + grailsParams.trim();
    }
    final String hostik = JDOMExternalizer.readString(element, "hostik");
    if (StringUtil.isNotEmpty(hostik) && !hostik.equals(GrailsUtils.GRAILS_RUN_DEFAULT_HOST)) {
      cmdLine += " " + GRAILS_SERVER_HOST + hostik;
    }
    final String portik = JDOMExternalizer.readString(element, "port");
    if (StringUtil.isNotEmpty(portik) && !portik.equals(GrailsUtils.GRAILS_RUN_DEFAULT_PORT)) {
      cmdLine += " " + GRAILS_SERVER_PORT + portik;
    }
    boolean disableAutorecomp = JDOMExternalizer.readBoolean(element, "recomp");
    if (disableAutorecomp) {
      cmdLine += " " + GRAILS_DISABLE_AUTO_RECOMPILE + "true";
    }

    boolean jndi = JDOMExternalizer.readBoolean(element, "jndi");
    if (jndi) {
      cmdLine += " " + GRAILS_ENABLE_JNDI + "true";
    }

    final String recFreq = JDOMExternalizer.readString(element, "recompileFreq");
    if (StringUtil.isNotEmpty(recFreq) && !"3".equals(recFreq)) {
      cmdLine += " " + GRAILS_RECOMPILE_FREQ + recFreq;
    }
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    JDOMExternalizer.write(element, "launchBrowser", launchBrowser);
  }

  @Override
  protected String getNoSdkMessage() {
    return "Grails SDK is not configured";
  }

  protected ModuleBasedConfiguration createInstance() {
    return new GrailsRunConfiguration(factory, getConfigurationModule().getProject(), getName(), cmdLine);
  }

  @Override
  protected MvcCommandLineState createCommandLineState(@NotNull ExecutionEnvironment environment, Module module) {
    if (isForTests() && GrailsConfigUtils.isAtLeastGrails1_2(module)) {
      return new MyTestRunCommandLineState(environment);
    }
    return new MyCommandLineState(environment);
  }

  private boolean isForTests() {
    return cmdLine.startsWith("test-app");
  }

  private class MyCommandLineState extends MvcCommandLineState {

    public MyCommandLineState(ExecutionEnvironment environment) {
      super(environment, false);
    }

    @Override
    public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
      ExecutionResult result = super.execute(executor, runner);
      if (result == null) {
        return null;
      }
      final ProcessHandler handler = result.getProcessHandler();
      if (handler != null && launchBrowser) {
        handler.addProcessListener(new ProcessAdapter() {
          @Override
          public void onTextAvailable(ProcessEvent event, Key outputType) {
            String s = event.getText().trim();
            if (s.startsWith(SERVER_RUNNING_BROWSE_TO)) {
              BrowserUtil.launchBrowser(s.substring(SERVER_RUNNING_BROWSE_TO.length()).trim());
              handler.removeProcessListener(this);
            }
          }
        });
      }
      return result;
    }
  }

  private class MyTestRunCommandLineState extends MvcCommandLineState {
    public MyTestRunCommandLineState(ExecutionEnvironment environment) {
      super(environment, true);
    }

    @Override
    public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
      final ProcessHandler processHandler = startProcess();
      final ConsoleView console = SMTestRunnerConnectionUtil.attachRunner(processHandler, this, GrailsRunConfiguration.this, cmdLine);
      return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
    }
  }
}
