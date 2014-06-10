package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.wsengine.WSEngineManager;

/**
 * @author Maxim
 */
public class WebServicesFacetConfiguration extends BaseWebServicesFacetConfiguration {

  protected boolean isServerSideSupport() {
    return true;
  }

  protected String[] getEngines(WSEngineManager wsEngineManager) {
    return wsEngineManager.getAvailableWSEngineNamesWithSupportedDeployment();
  }
}
