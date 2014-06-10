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

package com.advancedtools.webservices;

import com.advancedtools.webservices.references.*;
import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class WebServicesResourceProvider implements StandardResourceProvider{
  
  public void registerResources(ResourceRegistrar registrar) {

    addStdResource(WebServicesPluginSettings.HTTP_WWW_W3_ORG_2003_03_WSDL, "wsdl12.xsd", registrar);

    addStdResource(WebServicesPluginSettings.HTTP_SCHEMAS_XMLSOAP_ORG_WSDL, "wsdl11.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/wsdl/soap12/", "wsdl11soap12.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/wsdl/http/", "wsdl11_httpbinding.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/wsdl/mime/", "wsdl11_mimebinding.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/wsdl/soap/", "wsdl11_soapbinding.xsd", registrar);

    addStdResource("http://www.w3.org/2000/10/XMLSchema", "XMLSchema.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/soap/encoding/", "soap-encoding.xsd", registrar);

    addStdResource("http://www.w3.org/2003/05/soap-encoding", "soap-encoding1_2.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/ws/2003/03/business-process", "business-process.xsd", registrar);

    addStdResource("http://www.ibm.com/webservices/xsd/j2ee_jaxrpc_mapping_1_1.xsd", "j2ee_jaxrpc_mapping_1_1.xsd", registrar);

    addStdResource("http://www.w3.org/2003/11/wsdl", "wsdl20.xsd", registrar);

    addStdResource("http://java.sun.com/xml/ns/jaxws", "wsdl_customizationschema_2_0.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/soap/envelope/", "soap-envelope.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/ws/2004/09/policy", "ws-policy.xsd", registrar);

    addStdResource("http://schemas.xmlsoap.org/ws/2004/08/eventing", "ws-eventing.xsd", registrar);

    addStdResource("http://www.w3.org/2005/08/addressing", "ws-addr.xsd", registrar);

    addStdResource("http://www.w3.org/2006/05/addressing/wsdl", "ws-addr2006.xsd", registrar);

    addStdResource("http://java.sun.com/xml/ns/jaxb", "bindingschema_2_0.xsd", registrar);

    addStdResource("http://research.sun.com/wadl/2006/10", "wadl20061109.xsd", registrar);

    addStdResource("http://jersey.dev.java.net/", "jersey.xsd", registrar);

    addStdResource("http://cxf.apache.org/schemas/configuration/soap.xsd", "cxf-soap.xsd", registrar);

    addStdResource("http://cxf.apache.org/schemas/jaxws.xsd", "cxf-jaxws.xsd", registrar);

    addStdResource(WSDDReferenceProvider.WSDD_JAVA_PROVIDER_NAMESPACE, "wsdd_provider_java.xsd", registrar);
    addStdResource(WSDDReferenceProvider.WSDD_NAMESPACE, "wsdd.dtd", registrar);
    addStdResource(XFireServicesXmlReferenceProvider.HTTP_XFIRE_CODEHAUS_ORG_CONFIG_1_0, "XFire.Services.dtd", registrar);
    addStdResource(JaxWSXmlReferenceProvider.OUR_NS, "sun-jaxws.xsd", registrar);

    addStdResource(JaxRPCRiXmlReferenceProvider.OUR_NS, "Sun.JaxRPC.dtd", registrar);
    addStdResource(JBossWSXmlReferenceProvider.HTTP_WWW_JBOSS_ORG_JBOSSWS_TOOLS, "jbossws-tool_1_0.xsd", registrar);
    addStdResource("http://www.jboss.com/ws-security/config", "jboss-ws-security_1_0.xsd", registrar);
    addStdResource("urn:jboss:jaxws-config:2.0", "jboss-jaxws-config_2_0.xsd", registrar);
    addStdResource("urn:jboss:jaxrpc-config:2.0", "jboss-jaxrpc-config_2_0.xsd", registrar);

  }

  private void addStdResource(@NonNls String url, @NonNls String fileName, ResourceRegistrar registrar) {
    registrar.addStdResource(url, "/schemas/"+fileName, getClass());
  }


}
