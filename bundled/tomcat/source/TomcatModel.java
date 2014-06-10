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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.execution.configurations.PredefinedLogFile;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.javaee.appServerIntegrations.ApplicationServer;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentProvider;
import com.intellij.javaee.run.configuration.CheckUtil;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.configuration.PredefinedLogFilesProvider;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.execution.DefaultOutputProcessor;
import com.intellij.javaee.run.execution.OutputProcessor;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TomcatModel implements ServerModel, PredefinedLogFilesProvider {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2ee.web.tomcat.TomcatModel");
  @NonNls private static final String TOMCAT_LOCALHOST_LOG_ID = "TOMCAT_LOCALHOST_LOG_ID";
  @NonNls private static final String SERVER_XML_FILE_NAME = "server.xml";
  public boolean DEPLOY_TOMCAT_MANAGER = false;
  public String BASE_DIRECTORY_NAME = "";
  private CommonModel myCommonModel;

  public int getDefaultPort() {
    return TomcatUtil.DEFAULT_PORT;
  }

  public void setCommonModel(CommonModel comminModel) {
    myCommonModel = comminModel;
  }

  public void checkConfigurationInt() throws RuntimeConfigurationException {
    if (myCommonModel.isLocal()) {
      CheckUtil.checkExists(TomcatBundle.message("file.kind.catalina.home.directory"), getHomeDirectory());
      CheckUtil.checkExists(TomcatBundle.message("file.kind.catalina.config.directory"), TomcatUtil.baseConfigDir(getSourceBaseDirectoryPath()));
    }
  }

  public String getHomeDirectory() throws RuntimeConfigurationException {
    ApplicationServer applicationServer = myCommonModel.getApplicationServer();
    if (applicationServer == null) throw new RuntimeConfigurationError(TomcatBundle.message("exception.text.application.server.not.specified"));
    TomcatPersistentData tomcatData = ((TomcatPersistentData)applicationServer.getPersistentData());
    return FileUtil.toSystemDependentName(tomcatData.CATALINA_HOME);
  }

  public J2EEServerInstance createServerInstance() throws ExecutionException {
    TomcatServerInstance tomcatServerInstance = new TomcatServerInstance(myCommonModel);
    if (myCommonModel.isLocal()) {
      TomcatDeploymentProvider.prepareServer(this);
    }
    return tomcatServerInstance;
  }

  public String getSourceBaseDirectoryPath() throws RuntimeConfigurationException {
    ApplicationServer applicationServer = myCommonModel.getApplicationServer();
    if (applicationServer == null) throw new RuntimeConfigurationError(TomcatBundle.message("exception.text.application.server.not.specified")) ;

    TomcatPersistentData tomcatData = ((TomcatPersistentData)applicationServer.getPersistentData());
    if (tomcatData.CATALINA_BASE.length() > 0) {
      return FileUtil.toSystemDependentName(tomcatData.CATALINA_BASE);
    }
    return getHomeDirectory();
  }

  public String getBaseDirectoryPath() {
    return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
      public String compute() {
        try {
          final File file;
          final File tomcatSystemDir = new File(PathManager.getSystemPath(), "tomcat");
          if (BASE_DIRECTORY_NAME.length() == 0) {
            final String nameCandidate = PathUtil.suggestFileName(getName()  + "_" + getProject().getLocationHash());
            file = FileUtil.findSequentNonexistentFile(tomcatSystemDir, nameCandidate, "");
            BASE_DIRECTORY_NAME = file.getName();
          }
          else {
            file = new File(tomcatSystemDir, BASE_DIRECTORY_NAME);
          }
          return file.getCanonicalPath();
        }
        catch (IOException e) {
          LOG.error(e);
          return "";
        }
      }
    });
  }

  public boolean versionHigher(String version) {
    ApplicationServer applicationServer = myCommonModel.getApplicationServer();
    if (applicationServer == null) {
      return false;
    }
    TomcatPersistentData tomcatData = ((TomcatPersistentData)applicationServer.getPersistentData());

    return tomcatData.VERSION.compareTo(version) >= 0;
  }

  public void readExternal(Element element) throws InvalidDataException {
    boolean clearBaseDirectory = BASE_DIRECTORY_NAME == null;
    DefaultJDOMExternalizer.readExternal(this, element);
    if (clearBaseDirectory) {
      BASE_DIRECTORY_NAME = "";
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }

  public SettingsEditor<CommonModel> getEditor() {
    if (myCommonModel.isLocal()) {
      return new TomcatLocalRunConfigurationEditor();
    }
    return null;
  }

  public DeploymentProvider getDeploymentProvider() {
    return myCommonModel.isLocal() ? TomcatManager.getInstance().getDeploymentProvider() : null;
  }

  public String getVersionName() {
    ApplicationServer applicationServer = myCommonModel.getApplicationServer();
    if (applicationServer == null) {
      return TomcatBundle.message("unknown.version.presentation");
    }
    TomcatPersistentData tomcatData = ((TomcatPersistentData)applicationServer.getPersistentData());
    return tomcatData.VERSION;
  }

  public int getLocalPort() {
    try {
      return TomcatUtil.getPort(getServerXmlFile());
    }
    catch (RuntimeConfigurationException e) {
      return getDefaultPort();
    }
  }

  private File getServerXmlFile() throws RuntimeConfigurationException {
    return new File(TomcatUtil.serverXML(getSourceBaseDirectoryPath()));
  }

  public List<Pair<String, Integer>> getAddressesToCheck() {
    List<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
    result.add(Pair.create(myCommonModel.getHost(), myCommonModel.getPort()));
    if (isLocal()) {
      try {
        final int port = TomcatUtil.getShutdownPort(getServerXmlFile());
        result.add(Pair.create(myCommonModel.getHost(), port));
      }
      catch (RuntimeConfigurationException e) {
      }
    }
    return result;
  }

  public String getDefaultUrlForBrowser() {
    return getUrlForBrowser(true);
  }

  public String getUrlForBrowser(final boolean addContextPath) {
    @NonNls StringBuilder result = new StringBuilder();
    result.append("http://");
    result.append(myCommonModel.getHost());
    result.append(":");
    result.append(String.valueOf(myCommonModel.getPort()));
    if (addContextPath) {
      String defaultContext = getDefaultContext();
      if (defaultContext != null && !defaultContext.equals("/")) {
        if (!StringUtil.startsWithChar(defaultContext, '/')){
          result.append("/");
        }
        result.append(defaultContext);
      }
    }
    result.append("/");
    return result.toString();
  }

  @Nullable
  private String getDefaultContext() {
    for (DeploymentModel model : myCommonModel.getDeploymentModels()) {
      if (model instanceof TomcatModuleDeploymentModel) {
        return ((TomcatModuleDeploymentModel)model).CONTEXT_PATH;
      }
    }
    return null;
  }

  public OutputProcessor createOutputProcessor(ProcessHandler j2EEOSProcessHandlerWrapper, J2EEServerInstance serverInstance) {
    return new DefaultOutputProcessor(j2EEOSProcessHandlerWrapper);
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
    Sdk projectJdk = ProjectRootManager.getInstance(myCommonModel.getProject()).getProjectJdk();
    if (projectJdk != null && !(projectJdk.getSdkType() instanceof JavaSdk)) {
      String msg = TomcatBundle.message("project.jdk.0.is.not.java.sdk", projectJdk.getName());
      throw new RuntimeConfigurationError(msg);
    }

    Set<String> contexts = new HashSet<String>();
    for (DeploymentModel deploymentModel : myCommonModel.getDeploymentModels()) {
      final TomcatModuleDeploymentModel model = (TomcatModuleDeploymentModel)deploymentModel;
      if (!contexts.add(model.CONTEXT_PATH)) {
        throw new RuntimeConfigurationError(TomcatBundle.message("error.duplicated.context.path.text", model.CONTEXT_PATH));
      }
    }
  }

  public Project getProject() {
    return myCommonModel.getProject();
  }

  public TomcatModel clone() throws CloneNotSupportedException {
    TomcatModel tomcatModel = (TomcatModel)super.clone();
    tomcatModel.BASE_DIRECTORY_NAME = null;
    return tomcatModel;
  }

  public boolean isLocal() {
    return myCommonModel.isLocal();
  }

  public String getName() {
    return myCommonModel.getName();
  }


  @NotNull
  public PredefinedLogFile[] getPredefinedLogFiles() {
    return new PredefinedLogFile[] {
      new PredefinedLogFile(TOMCAT_LOCALHOST_LOG_ID, true)
    };
  }

  @Nullable
  public LogFileOptions getOptionsForPredefinedLogFile(PredefinedLogFile predefinedLogFile) {
    if (TOMCAT_LOCALHOST_LOG_ID.equals(predefinedLogFile.getId())) {
      final String hostLogFilePattern = TomcatUtil.getHostLogFilePattern(getBaseDirectoryPath());
      return new LogFileOptions(TomcatBundle.message("log.file.alias.tomcat.log"), hostLogFilePattern, predefinedLogFile.isEnabled(), true, false);
    }
    return null;
  }
}
