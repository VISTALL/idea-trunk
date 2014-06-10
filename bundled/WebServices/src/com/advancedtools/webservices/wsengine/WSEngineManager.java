package com.advancedtools.webservices.wsengine;

import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.xfire.XFireWSEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.axis2.Axis2WSEngine;
import com.advancedtools.webservices.jbossws.JBossWSEngine;
import com.advancedtools.webservices.jaxrpc.JaxRPCWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.websphere.WebSphereWSEngine;
import com.advancedtools.webservices.jaxb.JaxbMappingEngine;
import com.advancedtools.webservices.xmlbeans.XmlBeansMappingEngine;

import java.util.*;

public class WSEngineManager {
  private final Map<String, ExternalEngine> myExternalEngines = new HashMap<String, ExternalEngine>();
  private final Map<String, WSEngine> myWSEngines = new HashMap<String, WSEngine>();

  public WSEngineManager() {
    registerWSEngine(new AxisWSEngine());
    registerWSEngine(new XFireWSEngine());
    registerWSEngine(new JWSDPWSEngine());
    registerWSEngine(new Axis2WSEngine());
    registerWSEngine(new JBossWSEngine());
    registerWSEngine(new JaxRPCWSEngine());
    registerWSEngine(new RestWSEngine());
    registerWSEngine(new WebSphereWSEngine());

    registerExternalEngine(new JaxbMappingEngine());
    registerExternalEngine(new XmlBeansMappingEngine());
  }

  private void registerWSEngine(WSEngine wsEngine) {
    myWSEngines.put(wsEngine.getName(), wsEngine);
  }

  private void registerExternalEngine(ExternalEngine externalEngine) {
    myExternalEngines.put(externalEngine.getName(), externalEngine);
  }

  public WSEngine getWSEngineByName(String engineName) {
    WSEngine wsEngine = myWSEngines.get(engineName);

    if (wsEngine == null) {
      for(Map.Entry<String, WSEngine> entry:myWSEngines.entrySet()) {
        if (entry.getValue() instanceof ExternalEngineThatChangedTheName &&
            ((ExternalEngineThatChangedTheName)entry.getValue()).isYourOldName(engineName)
           ) {
          return entry.getValue();
        }
      }
    }
    return wsEngine;
  }

  public ExternalEngine getExternalEngineByName(String engineName) {
    return myExternalEngines.get(engineName);
  }

  public String[] getAvailableWSEngineNames() {
    final String[] strings = myWSEngines.keySet().toArray(new String[myWSEngines.size()]);
    Arrays.sort(strings);
    return strings;
  }
  
  public String[] getAvailableWSEngineNamesWithSupportedDeployment() {
    final List<String> engineNamesThatSupportDeployment = new ArrayList<String>();
    for(WSEngine engine:myWSEngines.values()) {
      if (engine.deploymentSupported()) engineNamesThatSupportDeployment.add(engine.getName());
    }

    String[] engineNamesArrayThatSupportDeployment = engineNamesThatSupportDeployment.toArray(new String[engineNamesThatSupportDeployment.size()]);
    Arrays.sort(engineNamesArrayThatSupportDeployment);
    return engineNamesArrayThatSupportDeployment;
  }

  public String[] getConfiguredWSEngineNames(boolean forDeployment) {
    final List<String> configuredEngineNames = new ArrayList<String>();
    for(WSEngine engine:myWSEngines.values()) {
      if (engine.getBasePath() != null && forDeployment == engine.deploymentSupported()) configuredEngineNames.add(engine.getName());
    }

    final String[] sortedConfiguredEngineNames = configuredEngineNames.toArray(new String[configuredEngineNames.size()]);
    Arrays.sort(sortedConfiguredEngineNames);
    return sortedConfiguredEngineNames;
  }
}
