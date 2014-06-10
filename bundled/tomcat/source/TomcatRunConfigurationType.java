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

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.javaee.appServerIntegrations.AppServerIntegration;
import com.intellij.javaee.appServerIntegrations.ApplicationServer;
import com.intellij.javaee.run.configuration.J2EEConfigurationFactory;
import com.intellij.javaee.run.configuration.J2EEConfigurationType;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.deployment.JspDeploymentManager;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.deployment.DeploymentUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.io.File;


public class TomcatRunConfigurationType extends J2EEConfigurationType {
  protected RunConfiguration createJ2EEConfigurationTemplate(ConfigurationFactory factory, Project project, boolean isLocal) {
    return J2EEConfigurationFactory.getInstance().createJ2EERunConfiguration(factory, project, new TomcatModel(),
                                                                             TomcatManager.getInstance(), isLocal,
                                                                             new TomcatStartupPolicy());
  }

  public String getDisplayName() {
    return TomcatBundle.message("run.config.tab.title.tomcat");
  }

  public String getConfigurationTypeDescription() {
    return TomcatBundle.message("run.config.tab.description.tomcat");
  }

  public Icon getIcon() {
    return TomcatManager.ICON_TOMCAT;
  }


  public AppServerIntegration getIntegration() {
    return TomcatManager.getInstance();
  }

  @Override
  public String getUrlToOpenInBrowser(@NotNull ApplicationServer server, @NotNull PsiFile psiFile) {
    final WebFacet webFacet = WebUtil.getWebFacet(psiFile);
    if (webFacet == null) return null;

    final TomcatPersistentData data = (TomcatPersistentData)server.getPersistentData();
    String serverPath = FileUtil.toSystemDependentName(data.CATALINA_BASE.length() > 0 ? data.CATALINA_BASE : data.CATALINA_HOME);
    final int port = TomcatUtil.getPort(new File(TomcatUtil.serverXML(serverPath)));
    @NonNls final String root = "http://" + CommonModel.LOCALHOST + ":" + port;
    final String relativePath = JspDeploymentManager.getInstance().computeRelativeTargetPath(psiFile, webFacet);
    if (relativePath == null) return null;
    return DeploymentUtil.concatPaths(root, relativePath);
  }

  @NotNull
  public String getId() {
    return "#com.intellij.j2ee.web.tomcat.TomcatRunConfigurationFactory";
  }
}
