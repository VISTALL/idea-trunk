package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.wsengine.WSEngineManager;

/**
 * @author Maxim
 */
public class WebServicesClientFacetConfiguration extends BaseWebServicesFacetConfiguration {
  protected boolean isServerSideSupport() {
    return false;
  }

  protected String[] getEngines(WSEngineManager wsEngineManager) {
    return wsEngineManager.getAvailableWSEngineNames();
  }
}
