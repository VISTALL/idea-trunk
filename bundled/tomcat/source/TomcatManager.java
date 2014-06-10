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

import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.appServerIntegrations.AppServerDeployedFileUrlProvider;
import com.intellij.javaee.appServerIntegrations.AppServerIntegration;
import com.intellij.javaee.appServerIntegrations.ApplicationServerHelper;
import com.intellij.javaee.deployment.DeploymentProvider;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.openapi.ex.AppServerIntegrationsManager;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;


public class TomcatManager extends AppServerIntegration {
  public static final Icon ICON_TOMCAT = IconLoader.getIcon("/runConfigurations/tomcat.png");
  private final TomcatDeploymentProvider myTomcatDeploymentProvider = new TomcatDeploymentProvider();
  private final ApplicationServerHelper myApplicationServerHelper = new TomcatApplicationServerHelper();

  public static TomcatManager getInstance() {
    return AppServerIntegrationsManager.getInstance().getIntegration(TomcatManager.class);
  }

  public Icon getIcon() {
    return ICON_TOMCAT;
  }

  public String getPresentableName() {
    return TomcatBundle.message("tomcat.application.server.name");
  }

  @NotNull @NonNls
  public String getComponentName() {
    return "#com.intellij.j2ee.web.tomcat.TomcatManager";
  }

  public DeploymentProvider getDeploymentProvider() {
    return myTomcatDeploymentProvider;
  }

  public ApplicationServerHelper getApplicationServerHelper() {
    return myApplicationServerHelper;
  }

  @NotNull
  public Collection<FacetTypeId<? extends JavaeeFacet>> getSupportedFacetTypes() {
    return JavaeeFacetUtil.getInstance().getSingletonCollection(WebFacet.ID);
  }

  public @NotNull AppServerDeployedFileUrlProvider getDeployedFileUrlProvider() {
    return TomcatUrlMapping.INSTANCE;
  }
}
