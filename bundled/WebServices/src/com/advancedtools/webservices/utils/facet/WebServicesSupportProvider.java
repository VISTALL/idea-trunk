package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.actions.EnableWebServicesSupportUtils;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.module.Module;

/**
 * @author Maxim
 */
public class WebServicesSupportProvider extends WebServicesSupportProviderBase<WebServicesFacet> {
  public WebServicesSupportProvider() {
    super(WebServicesFacet.ourFacetType);
  }

  public String getTitle() {
    return WSBundle.message("webservices.support.provider.name");
  }

  protected void enableWebServicesSupport(Module module, WSEngine wsEngine) {
    EnableWebServicesSupportUtils.enableWebServicesServerSupport(module, wsEngine);
  }
}