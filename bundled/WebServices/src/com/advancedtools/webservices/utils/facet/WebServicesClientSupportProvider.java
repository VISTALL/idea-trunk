package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.actions.EnableWebServicesSupportUtils;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.javaee.web.WebUtil;
import com.intellij.openapi.module.Module;

/**
 * @author Maxim
 */
public class WebServicesClientSupportProvider extends WebServicesSupportProviderBase<WebServicesClientFacet> {
  public WebServicesClientSupportProvider() {
    super(WebServicesClientFacet.ourFacetType);
  }

  @Override
  public String getGroupId() {
    return WebUtil.WEB_FRAMEWORK_GROUP_ID;
  }

  public String getTitle() {
    return WSBundle.message("webservices.client.support.provider.name");
  }

  protected void enableWebServicesSupport(Module module, WSEngine wsEngine) {
    EnableWebServicesSupportUtils.enableWebServiceSupportForClient(module, wsEngine);
  }
}