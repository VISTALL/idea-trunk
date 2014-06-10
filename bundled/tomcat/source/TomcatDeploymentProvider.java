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
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.facet.pointers.FacetPointer;
import com.intellij.javaee.deployment.*;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.javaee.web.artifact.WebArtifactUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactPointer;
import com.intellij.packaging.artifacts.ArtifactType;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TomcatDeploymentProvider extends DeploymentProviderEx {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2ee.web.tomcat.TomcatDeploymentProvider");
  @NonNls private static final String CONTEXT_ELEMENT_NAME = "Context";
  @NonNls private static final String PATH_ATTR = "path";
  @NonNls private static final String DOC_BASE_ATTR = "docBase";
  @NonNls private static final String WORKDIR_ATTR = "workDir";
  @NonNls private static final String APP_BASE_ATTR = "appBase";
  @NonNls private static final String AUTO_DEPLOY_ATTR = "autoDeploy";
  @NonNls private static final String UNPACK_WARS_ATTR = "unpackWARs";
  @NonNls private static final String MANAGER_XML_FILE_NAME = "manager.xml";
  @NonNls private static final String TOMCAT_USERS_XML = "tomcat-users.xml";
  @NonNls private static final String MANAGER_CONTEXT_PATH = "/manager";

  public void doDeploy(Project project, J2EEServerInstance instance, DeploymentModel model) {
    final TomcatModuleDeploymentModel tomcatModel = (TomcatModuleDeploymentModel)model;
    try {
      final TomcatModel serverModel = (TomcatModel)model.getServerModel();
      final List<TomcatUtil.ContextItem> contexts = TomcatUtil.getContexts(serverModel);
      for (TomcatUtil.ContextItem contextItem : contexts) {
        String docBase = contextItem.getElement().getAttributeValue(DOC_BASE_ATTR);
        if (docBase != null && docBase.equals(TomcatUtil.getDeploymentPath(model))) {
          TomcatUtil.removeContextItem(serverModel, contextItem);
        }
      }
      addApplicationContext(tomcatModel);
      setDeploymentStatus(instance, tomcatModel, DeploymentStatus.DEPLOYED);

      if (!serverModel.versionHigher(TomcatPersistentData.VERSION50) && instance.isConnected()) {
        new Tomcat4Deployer(serverModel).deploy(getContextPath(tomcatModel));
      }
    }
    catch (ExecutionException e) {
      final JavaeeFacet facet = model.getFacet();
      if (facet != null) {
        Messages.showErrorDialog(project, e.getMessage(), TomcatBundle.message("message.text.error.deploying.facet", facet.getName()));
      }
      else {
        Messages.showErrorDialog(project, e.getMessage(), TomcatBundle.message("message.text.error.deploying.artifact", model.getArtifact().getName()));
      }
      setDeploymentStatus(instance, tomcatModel, DeploymentStatus.FAILED);
    }
  }

  private static void setDeploymentStatus(J2EEServerInstance instance, TomcatModuleDeploymentModel model, DeploymentStatus status) {
    final CommonModel configuration = instance.getCommonModel();
    final TomcatModel tomcatConfiguration = ((TomcatModel)configuration.getServerModel());
    final Project project = tomcatConfiguration.getProject();
    DeploymentManager.getInstance(project).setDeploymentStatus(model, status, configuration, instance);
  }

  public DeploymentModel createNewDeploymentModel(CommonModel commonModel, ArtifactPointer artifactPointer) {
    return new TomcatModuleDeploymentModel(commonModel, artifactPointer);
  }

  public DeploymentModel createNewDeploymentModel(CommonModel configuration, FacetPointer<JavaeeFacet> javaeeFacetPointer) {
    return new TomcatModuleDeploymentModel(configuration, javaeeFacetPointer);
  }

  @Override
  public SettingsEditor<DeploymentModel> createAdditionalDeploymentSettingsEditor(CommonModel commonModel, Artifact artifact) {
    return new TomcatDeploymentSettingsEditor(commonModel, artifact);
  }

  public SettingsEditor<DeploymentModel> createAdditionalDeploymentSettingsEditor(CommonModel configuration, JavaeeFacet javaeeFacet) {
    return new TomcatDeploymentSettingsEditor(configuration, javaeeFacet);
  }

  @Override
  public Collection<? extends ArtifactType> getSupportedArtifactTypes() {
    return Arrays.asList(WebArtifactUtil.getInstance().getExplodedWarArtifactType(), WebArtifactUtil.getInstance().getWarArtifactType());
  }

  public void startUndeploy(J2EEServerInstance activeInstance, DeploymentModel model) {
    final CommonModel configuration = activeInstance.getCommonModel();
    final TomcatModel serverModel = ((TomcatModel)configuration.getServerModel());
    final TomcatModuleDeploymentModel tomcatModel = (TomcatModuleDeploymentModel)model;
    if (!serverModel.isLocal()) {
      final JavaeeFacet facet = model.getFacet();
      if (facet != null) {
        Messages.showErrorDialog(configuration.getProject(), TomcatBundle.message("message.text.deployment.not.supported.for.remote"),
                                 TomcatBundle.message("message.text.error.deploying.facet", facet.getName()));
      }
      else {
        Messages.showErrorDialog(configuration.getProject(), TomcatBundle.message("message.text.deployment.not.supported.for.remote"),
                                 TomcatBundle.message("message.text.error.deploying.artifact", model.getArtifact().getName()));
      }
      setDeploymentStatus(activeInstance, tomcatModel, DeploymentStatus.FAILED);
    }
    else {
      final String contextPath = getContextPath(tomcatModel);
      if (serverModel.versionHigher(TomcatPersistentData.VERSION50)) {
        final String contextXML = TomcatUtil.getContextXML(serverModel.getBaseDirectoryPath(), contextPath);
        final File contextXmlFile = new File(contextXML);
        if(contextXmlFile.exists()) {
          contextXmlFile.delete();
        }
      }
      else {
        try {
          addOrRemoveContextElementInServerXml(serverModel, contextPath, null);
          new Tomcat4Deployer(serverModel).undeploy(contextPath);
        }
        catch (ExecutionException ignored) {
        }
      }
      setDeploymentStatus(activeInstance, tomcatModel, DeploymentStatus.NOT_DEPLOYED);
    }
  }

  public void updateDeploymentStatus(J2EEServerInstance instance, DeploymentModel model) {
    try {
      CommonModel configuration = instance.getCommonModel();
      TomcatModel serverModel = ((TomcatModel)configuration.getServerModel());
      final TomcatModuleDeploymentModel tomcatDeploymentModel = (TomcatModuleDeploymentModel)model;
      String contextPath = getContextPath(tomcatDeploymentModel);

      final Element contextElement = TomcatUtil.findContextElement(serverModel.getBaseDirectoryPath(), contextPath, tomcatDeploymentModel);

      final DeploymentStatus status;
      if (serverModel.isLocal()) {
        status = contextElement != null ? DeploymentStatus.DEPLOYED : DeploymentStatus.NOT_DEPLOYED;
      }
      else {
        status = DeploymentStatus.UNKNOWN;
      }
      DeploymentManager.getInstance(serverModel.getProject()).setDeploymentStatus(model, status, configuration, instance);
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
  }

  public String getHelpId() {
    return null;
  }

  private static void addApplicationContext(TomcatModuleDeploymentModel tomcatModuleDeploymentModel) throws ExecutionException {
    try {
      TomcatModel serverModel = (TomcatModel)tomcatModuleDeploymentModel.getServerModel();
      String contextPath = getContextPath(tomcatModuleDeploymentModel);

      Element contextElement = TomcatUtil.findContextElement(serverModel.getSourceBaseDirectoryPath(), contextPath, tomcatModuleDeploymentModel);

      if (contextElement == null) {
        contextElement = new Element(CONTEXT_ELEMENT_NAME);
        //contextElement.addContent((Comment)TomcatConstants.CONTEXT_COMMENT.clone());
      }

      final String deploymentPath = TomcatUtil.getDeploymentPath(tomcatModuleDeploymentModel);
      if (deploymentPath == null) {
        throw new ExecutionException(TomcatBundle.message("exception.text.neither.exploded.directory.nor.jar.file.configured"));
      }

      if (!new File(deploymentPath).exists()) {
        throw new ExecutionException(TomcatBundle.message("exception.text.file.not.found.for.web.module", deploymentPath));
      }

      //remove unpacked WAR directory
      if(DeploymentSource.FROM_JAR == tomcatModuleDeploymentModel.getDeploymentSource()) {
        final String contextXML = TomcatUtil.getContextXML(serverModel.getSourceBaseDirectoryPath(), contextPath);
        final String xmlName = new File(contextXML).getName();
        final String dirName = xmlName.substring(0, xmlName.length() - 4);

        final Document serverXmlDocument = TomcatUtil.loadXMLFile(TomcatUtil.serverXML(serverModel.getBaseDirectoryPath()));
        final Element localHost = TomcatUtil.findLocalHost(serverXmlDocument.getRootElement());

        final String appBase = localHost.getAttributeValue(APP_BASE_ATTR);
        FileUtil.delete(new File(appBase, dirName));
      }

      contextElement.setAttribute(PATH_ATTR, contextPath);
      contextElement.setAttribute(DOC_BASE_ATTR, deploymentPath);

      if(serverModel.versionHigher(TomcatPersistentData.VERSION50)) {
        final String contextXML = TomcatUtil.getContextXML(serverModel.getBaseDirectoryPath(), contextPath);
        final File targetContextXmlFile = new File(contextXML);
        targetContextXmlFile.getParentFile().mkdirs();

        final Document xmlDocument;
        if(contextElement.getDocument() != null && contextElement.isRootElement()) {
          xmlDocument = (Document)contextElement.getDocument().clone();
        }
        else{
          xmlDocument = new Document();
          xmlDocument.setRootElement((Element)contextElement.clone());
        }
        TomcatUtil.saveXMLFile(xmlDocument, targetContextXmlFile.getPath(), true);
      }
      else {
        String root = FileUtil.toSystemDependentName(TomcatUtil.getGeneratedFilesPath(serverModel));
        String scratchdir = root + File.separator + TomcatConstants.CATALINA_WORK_DIRECTORY_NAME + File.separator
                            + new File(TomcatUtil.getContextXML(serverModel.getBaseDirectoryPath(), contextPath)).getName();
        new File(scratchdir).mkdirs();

        contextElement.setAttribute(WORKDIR_ATTR, scratchdir);

        addOrRemoveContextElementInServerXml(serverModel, contextPath, contextElement);
      }
    }
    catch (RuntimeConfigurationException e) {
      throw new ExecutionException(e.getMessage());
    }
  }

  private static void addOrRemoveContextElementInServerXml(final TomcatModel serverModel, final String contextPath,
                                                           final @Nullable Element newContext) throws ExecutionException {
    Document serverXmlDocument = TomcatUtil.loadXMLFile(TomcatUtil.serverXML(serverModel.getBaseDirectoryPath()));

    Element localHost = TomcatUtil.findLocalHost(serverXmlDocument.getRootElement());
    Element oldContext = TomcatUtil.findContextByPath(localHost, contextPath);
    if(oldContext != null) {
      localHost.removeContent(oldContext);
    }
    if (newContext != null) {
      localHost.addContent((Element)newContext.clone());
    }
    TomcatUtil.saveXMLFile(serverXmlDocument, TomcatUtil.serverXML(serverModel.getBaseDirectoryPath()), true);
  }

  public DeploymentMethod[] getAvailableMethods() {
    return null;
  }

  public static String getContextPath(TomcatModuleDeploymentModel deploymentSettings) {
    String contextPath = deploymentSettings.CONTEXT_PATH;
    if(!StringUtil.startsWithChar(contextPath, '/')) {
      contextPath =  "/" + contextPath;
    }

    if(contextPath.equals("/")) {
      contextPath = "";
    }

    return contextPath;
  }

  public static void prepareServer(TomcatModel tomcatModel) throws ExecutionException {
    try {
      if(!tomcatModel.isLocal()) return;

      String baseDirectoryPath = tomcatModel.getBaseDirectoryPath();
      FileUtil.delete(new File(baseDirectoryPath));

      String sourceBaseDirectoryPath = tomcatModel.getSourceBaseDirectoryPath();

      File sourceBase = new File(TomcatUtil.baseConfigDir(sourceBaseDirectoryPath));
      File workBase = new File(TomcatUtil.baseConfigDir(baseDirectoryPath));

      try {
        FileUtil.copyDir(sourceBase, workBase);
        if (!tomcatModel.getHomeDirectory().equals(sourceBaseDirectoryPath)) {
          File setEnvFile = TomcatUtil.getSetEnvFile(sourceBaseDirectoryPath);
          if (setEnvFile.exists()) {
            FileUtil.copy(setEnvFile, TomcatUtil.getSetEnvFile(baseDirectoryPath));
          }
        }
        File logsDir = new File(TomcatUtil.getLogsDirPath(baseDirectoryPath));
        logsDir.mkdir();
      }
      catch (IOException e) {
        throw new ExecutionException(TomcatBundle.message("message.text.error.copying.configuration.files.from.0.to.1.because.of.2",
                                                          sourceBase.getPath(), workBase.getPath(), e.getMessage()));
      }
      patchCatalinaProperties(new File(workBase, "catalina.properties"), sourceBaseDirectoryPath);

      List<TomcatUtil.ContextItem> contexts = TomcatUtil.getContexts(tomcatModel);

      for (TomcatUtil.ContextItem contextItem : contexts) {
        if (tomcatModel.DEPLOY_TOMCAT_MANAGER && MANAGER_XML_FILE_NAME.equals(contextItem.getFile().getName())) {
          continue;
        }
        if (!tomcatModel.versionHigher(TomcatPersistentData.VERSION50)) {
          final Element element = contextItem.getElement();
          if (element != null && MANAGER_CONTEXT_PATH.equals(element.getAttributeValue(PATH_ATTR))) {
            continue;
          }
        }

        TomcatUtil.removeContextItem(tomcatModel, contextItem);
      }

      String xmlPath = TomcatUtil.serverXML(baseDirectoryPath);

      Document serverXmlDocument = TomcatUtil.loadXMLFile(xmlPath);
      Document original = (Document)serverXmlDocument.clone();
      Element localHost = TomcatUtil.findLocalHost(serverXmlDocument.getRootElement());

      String appBase = localHost.getAttributeValue(APP_BASE_ATTR);

      if(appBase == null) appBase = "";

      if(!new File(appBase).isAbsolute()) {
        appBase = new File(sourceBaseDirectoryPath, appBase).getAbsolutePath();
      }
      localHost.setAttribute(APP_BASE_ATTR, appBase);
      localHost.setAttribute(AUTO_DEPLOY_ATTR, Boolean.TRUE.toString());
      localHost.setAttribute(UNPACK_WARS_ATTR, Boolean.TRUE.toString());
      TomcatUtil.saveXMLFile(localHost.getDocument(), xmlPath, true);

      File tomcatUsers = new File(workBase, TOMCAT_USERS_XML);
      if (tomcatUsers.exists() && !tomcatModel.versionHigher(TomcatPersistentData.VERSION50)) {
        Tomcat4Deployer.addManagerUser(tomcatUsers);
      }

      TomcatUtil.configureWebXml(tomcatModel);
    }
    catch (RuntimeConfigurationException e) {
      LOG.assertTrue(false);
    }
  }

  private static void patchCatalinaProperties(final File file, final String catalinaBasePath) {
    if (!file.exists()) return;

    try {
      File oldFile = new File(file.getAbsolutePath() + ".0");
      FileUtil.rename(file, oldFile);
      BufferedReader input = new BufferedReader(new FileReader(oldFile));
      PrintWriter output = new PrintWriter(file);
      try {
        String line;
        while ((line = input.readLine()) != null) {
          output.println(StringUtil.replace(line, "${catalina.base}", FileUtil.toSystemIndependentName(catalinaBasePath)));
        }
      }
      finally {
        output.close();
        input.close();
      }
    }
    catch (IOException e) {
      LOG.info(e);
    }
  }
}
