/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

import com.intellij.javaee.appServerIntegrations.ApplicationServerUrlMapping;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.artifact.JavaeeArtifactUtil;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.deployment.DeploymentUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.jsp.WebDirectoryElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author nik
 */
public class TomcatUrlMapping extends ApplicationServerUrlMapping {
  public static final TomcatUrlMapping INSTANCE = new TomcatUrlMapping();

  private TomcatUrlMapping() {
  }

  @Override
  public VirtualFile findSourceFile(@NotNull final J2EEServerInstance serverInstance,
                                    @NotNull final CommonModel commonModel, @NotNull final String url) {
    final String baseUrl = ((TomcatModel)commonModel.getServerModel()).getUrlForBrowser(false);
    if (!url.startsWith(baseUrl)) {
      return null;
    }
    String relative = StringUtil.trimStart(url.substring(baseUrl.length()), "/");
    int end = StringUtil.findFirst(relative, new CharFilter() {
      public boolean accept(final char ch) {
        return ch == '?' || ch == '#' || ch == ';';
      }
    });
    if (end != -1) {
      relative = relative.substring(0, end);
    }
    relative = StringUtil.trimEnd(relative, "/");

    final Pair<DeploymentModel, String> pair = findDeploymentModel(relative, commonModel);
    if (pair != null) {
      final WebFacet webFacet = (WebFacet)pair.getFirst().getFacet();
      if (webFacet != null) {
        return findInFacet(webFacet, pair.getSecond());
      }

      final Collection<WebFacet> facets = JavaeeArtifactUtil.getInstance().getFacetsIncludedInArtifact(commonModel.getProject(),
                                                                                                       pair.getFirst().getArtifact(), WebFacet.ID);
      for (WebFacet facet : facets) {
        final VirtualFile file = findInFacet(facet, pair.getSecond());
        if (file != null) {
          return file;
        }
      }
    }
    return null;
  }

  @Nullable
  private static VirtualFile findInFacet(final WebFacet webFacet, final String path) {
    final WebDirectoryElement element = WebUtil.getWebUtil().createWebDirectoryElement(webFacet, path, false);
    return element.getOriginalVirtualFile();
  }

  @Nullable
  private static Pair<DeploymentModel, String> findDeploymentModel(final String relative, final CommonModel commonModel) {
    DeploymentModel defaultModel = null;
    for (DeploymentModel deploymentModel : commonModel.getDeploymentModels()) {
      String contextPath = StringUtil.trimStart(StringUtil.trimEnd(((TomcatModuleDeploymentModel)deploymentModel).CONTEXT_PATH, "/"), "/");
      if (contextPath.length() == 0) {
        defaultModel = deploymentModel;
      }
      else if (relative.startsWith(contextPath)) {
        return Pair.create(deploymentModel, relative.substring(contextPath.length()));
      }
    }
    return defaultModel != null ? Pair.create(defaultModel, relative) : null;
  }

  @Override
  public String getUrlForDeployedFile(@NotNull J2EEServerInstance serverInstance,
                                      @NotNull DeploymentModel deploymentModel,
                                      @NotNull JavaeeFacet javaeeFacet,
                                      @NotNull String relativePath) {
    final TomcatModel serverModel = (TomcatModel)serverInstance.getCommonModel().getServerModel();
    return DeploymentUtil.concatPaths(serverModel.getUrlForBrowser(false), ((TomcatModuleDeploymentModel)deploymentModel).CONTEXT_PATH, relativePath);
  }
}
