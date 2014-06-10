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
package org.jetbrains.idea.tomcat;

import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.util.EnvironmentVariable;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.localRun.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TomcatStartupPolicy implements ExecutableObjectStartupPolicy {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.tomcat.TomcatStartupPolicy");
  @NonNls protected static final String TEMP_FILE_NAME = "temp";
  @NonNls protected static final String BIN_DIR = "bin";
  @NonNls private static final String CATALINA_TMPDIR_ENV_PROPERTY = "CATALINA_TMPDIR";
  @NonNls private static final String JAVA_HOME_ENV_PROPERTY = "JAVA_HOME";
  @NonNls private static final String BOOTSTRAP_JAR_NAME = "bootstrap.jar";
  @NonNls private static final String JAVA_VM_ENV_VARIABLE = "JAVA_OPTS";
  @NonNls private static final String JAR_PARAMETER = "-jar";

  public ScriptsHelper getStartupHelper() {
    return null;
  }

  public ScriptsHelper getShutdownHelper() {
    return null;
  }

  public ScriptHelper createStartupScriptHelper(final ProgramRunner runner) {
    return new ScriptHelper() {
      public ExecutableObject getDefaultScript(CommonModel model) {
        try {
          TomcatModel tomcatModel = ((TomcatModel)model.getServerModel());
          final File catalinaFile = getCatalinaExecutableFile(tomcatModel);
          if (catalinaFile.exists()) {
            return new CommandLineExecutableObject(new String[]{catalinaFile.getAbsolutePath(), "run"}, null);
          }
          else {
            return createTomcatExecutable(model, tomcatModel, "start");
          }
        }
        catch (RuntimeConfigurationException e) {
          return null;
        }
      }
    };
  }

  private static ExecutableObject createTomcatExecutable(final CommonModel model, final TomcatModel tomcatModel,
                                                         final @NonNls String actionName) throws RuntimeConfigurationException {
    final Sdk projectJdk = ProjectRootManager.getInstance(model.getProject()).getProjectJdk();
    final @NonNls String vmExecutablePath = projectJdk == null || !(projectJdk.getSdkType() instanceof JavaSdkType)
                                            ? "java" : ((JavaSdkType)projectJdk.getSdkType()).getVMExecutablePath(projectJdk);
    return new CommandLineExecutableObject(new String[]{vmExecutablePath,
      "-Dcatalina.base=" + tomcatModel.getBaseDirectoryPath(),
      "-Dcatalina.home=" + tomcatModel.getHomeDirectory(),
      "-Djava.io.tmpdir=" + getCatalinaTempDirectory(tomcatModel),
      JAR_PARAMETER, new File(getBinDirectory(tomcatModel), BOOTSTRAP_JAR_NAME).getAbsolutePath(),
      actionName
    }, null) {

      protected GeneralCommandLine createCommandLine(String[] parameters, final Map<String, String> envVariables) {
        final String javaOptions = envVariables.get(JAVA_VM_ENV_VARIABLE);
        if (javaOptions != null) {
          List<String> newParameters = new ArrayList<String>();
          for (String parameter : parameters) {
            if (JAR_PARAMETER.equals(parameter)) {
              newParameters.addAll(StringUtil.splitHonorQuotes(javaOptions, ' '));
            }
            newParameters.add(parameter);
          }
          parameters = newParameters.toArray(new String[newParameters.size()]);
        }
        return super.createCommandLine(parameters, envVariables);
      }
    };
  }

  public ScriptHelper createShutdownScriptHelper(final ProgramRunner runner) {
    return new ScriptHelper() {
      public ExecutableObject getDefaultScript(CommonModel model) {
        try {
          TomcatModel tomcatModel = ((TomcatModel)model.getServerModel());
          final File catalinaFile = getCatalinaExecutableFile(tomcatModel);
          if (catalinaFile.exists()) {
            return new CommandLineExecutableObject(new String[]{catalinaFile.getAbsolutePath(), "stop"}, null);
          }
          else {
            return createTomcatExecutable(model, tomcatModel, "stop");
          }
        }
        catch (RuntimeConfigurationException e) {
          return null;
        }
      }
    };
  }

  private static File getCatalinaExecutableFile(final TomcatModel tomcatModel) throws RuntimeConfigurationException {
    return new File(getBinDirectory(tomcatModel), getDefaultCatalinaFileName());
  }

  private static File getBinDirectory(final TomcatModel tomcatModel) throws RuntimeConfigurationException {
    return new File(new File(tomcatModel.getHomeDirectory()), BIN_DIR);
  }

  public EnvironmentHelper getEnvironmentHelper() {
    return new EnvironmentHelper() {
      public String getDefaultJavaVmEnvVariableName(CommonModel model) {
        return JAVA_VM_ENV_VARIABLE;
      }

      public List<EnvironmentVariable> getAdditionalEnvironmentVariables(CommonModel model) {
        try {
          TomcatModel tomcatModel = ((TomcatModel)model.getServerModel());

          ArrayList<EnvironmentVariable> vars = new ArrayList<EnvironmentVariable>();
          vars.add(new EnvironmentVariable("CATALINA_HOME", tomcatModel.getHomeDirectory(), true));
          vars.add(new EnvironmentVariable("CATALINA_BASE", tomcatModel.getBaseDirectoryPath(), true));
          String tmpDir = EnvironmentUtil.getEnviromentProperties().get(CATALINA_TMPDIR_ENV_PROPERTY);
          if(tmpDir == null) {
            vars.add(new EnvironmentVariable(CATALINA_TMPDIR_ENV_PROPERTY, getCatalinaTempDirectory(tomcatModel), true));
          }
          Sdk projectJdk = ProjectRootManager.getInstance(model.getProject()).getProjectJdk();
          if (projectJdk != null) {
            vars.add(new EnvironmentVariable(JAVA_HOME_ENV_PROPERTY, projectJdk.getHomePath().replace('/', File.separatorChar), true));
          }
          else {
            String javaHome = EnvironmentUtil.getEnviromentProperties().get(JAVA_HOME_ENV_PROPERTY);
            if(javaHome != null) {
              vars.add(new EnvironmentVariable(JAVA_HOME_ENV_PROPERTY, javaHome, true));
            }
          }
          return vars;
        }
        catch (RuntimeConfigurationException e) {
          LOG.error(e);
          return null;
        }
      }
    };
  }

  private static String getCatalinaTempDirectory(final TomcatModel tomcatModel) throws RuntimeConfigurationException {
    return new File(tomcatModel.getSourceBaseDirectoryPath(), TEMP_FILE_NAME).getAbsolutePath();
  }

  @NonNls
  public static String getDefaultCatalinaFileName() {
    return SystemInfo.isWindows ? "catalina.bat" : "catalina.sh";
  }
}