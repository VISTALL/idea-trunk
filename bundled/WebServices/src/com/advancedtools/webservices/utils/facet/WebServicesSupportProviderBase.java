/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.actions.EnableWebServicesSupportUtils;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nik
 */
public abstract class WebServicesSupportProviderBase<F extends Facet<? extends BaseWebServicesFacetConfiguration>> extends
                                                                                                                   FacetBasedFrameworkSupportProvider<F> {
  public WebServicesSupportProviderBase(FacetType<F, ?> facetType) {
    super(facetType);
  }

  @NotNull
  public List<FrameworkVersion> getVersions() {
    final String defaultVersion = JWSDPWSEngine.JWSDP_PLATFORM;
    final String[] versions = {defaultVersion, AxisWSEngine.AXIS_PLATFORM, RestWSEngine.NAME};
    final List<FrameworkVersion> result = new ArrayList<FrameworkVersion>();
    for (String version : versions) {
      final LibraryInfo[] libraries = WebServicesClientLibraries.isSupported(version)
                                      ? WebServicesClientLibraries.getNecessaryLibraries(version)
                                      : LibraryInfo.EMPTY_ARRAY;
      result.add(new FrameworkVersion(version, version, libraries, defaultVersion.equals(version)));
    }
    return result;
  }

  protected void setupConfiguration(final F webServicesClientFacet, ModifiableRootModel modifiableRootModel, final FrameworkVersion version) {
    final WSEngine wsEngine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(version.getVersionName());
    webServicesClientFacet.getConfiguration().setWsEngine(wsEngine);
    final Module module = webServicesClientFacet.getModule();

    EnableWebServicesSupportUtils.ensureAnnotationsAreAllowedInJdkIfNeeded(wsEngine, module);

    // We do invoke later when module roots will be finalized
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        enableWebServicesSupport(module, wsEngine);
      }
    }, ModalityState.NON_MODAL);
  }

  protected abstract void enableWebServicesSupport(Module module, WSEngine wsEngine);
}
