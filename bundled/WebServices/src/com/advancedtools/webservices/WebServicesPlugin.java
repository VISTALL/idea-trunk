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

import com.advancedtools.webservices.axis.AxisSOAPMonitor;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.references.*;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.*;
import com.intellij.codeInsight.template.macro.MacroFactory;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.xml.XmlTag;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @by maxim
 */
public class WebServicesPlugin implements ProjectComponent, JDOMExternalizable {
  private static final String TOOL_WINDOW_ID2 = WSBundle.message("soap.messages.toolwindow.title");

  private ToolWindow toolWindow2;
  private JComponent content2;
  private AxisSOAPMonitor monitor;
  private final Project project;

  private final List<String> lastContexts = new LinkedList<String>();
  private final HashMap<Object, String> classToServiceNameMap = new HashMap<Object, String>();
  private static final @NonNls String CONTEXT_KEY = "context";
  private static final @NonNls String WSNAME_KEY = "wsname";
  private static final @NonNls String CLASS_KEY = "class";
  private static @NonNls final String NAME_KEY = "name";
  private static @NonNls final String TO_ADD_REQUIRED_LIBS = "addRequiredLibraries";
  private boolean toAddRequiredLibraries = true;
  private static WebServicesPlugin instance;
  
  private static final String WS_GROUP_NAME = WSBundle.message("webservices.live.templates.group.name");

  public WebServicesPlugin(Project _project) {
    project = _project;
  }

  public void projectOpened() {
    if (!project.isDefault()) {
      final EnvironmentFacade instance = EnvironmentFacade.getInstance();
      WSDLReferenceProvider wsdlReferenceProvider = new WSDLReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, wsdlReferenceProvider.getAttributeNames(), wsdlReferenceProvider.getFilter(),
                                                         wsdlReferenceProvider);

      WSDDReferenceProvider wsddReferenceProvider = new WSDDReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, wsddReferenceProvider.getAttributeNames(), wsddReferenceProvider.getFilter(),
                                                         wsddReferenceProvider);

      JAXBSchemaReferenceProvider jaxbProvider = new JAXBSchemaReferenceProvider();
      instance.registerXmlAttributeValueReferenceProvider(project, jaxbProvider.getAttributeNames(), jaxbProvider.getFilter(), jaxbProvider);

      JAXBJavaReferenceProvider jaxbJavaProvider = new JAXBJavaReferenceProvider();
      instance.registerReferenceProvider(project, jaxbJavaProvider.getFilter(), PsiLiteralExpression.class, jaxbJavaProvider);

      XFireServicesXmlReferenceProvider xFireServicesXmlProvider = new XFireServicesXmlReferenceProvider(project);
      instance.registerReferenceProvider(
        project,
        xFireServicesXmlProvider.getTagFilter(),
        XmlTag.class,
        xFireServicesXmlProvider
      );

      instance.registerXmlAttributeValueReferenceProvider(project, xFireServicesXmlProvider.getAttributeCandidateNames(),
                                                         xFireServicesXmlProvider.getAttributeFilter(), xFireServicesXmlProvider);

      final CxfXmlReferenceProvider cxfXmlReferenceProvider = new CxfXmlReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, cxfXmlReferenceProvider.getAttributeCandidateNames(),
                                                         cxfXmlReferenceProvider.getAttributeFilter(), cxfXmlReferenceProvider);

      JaxWSXmlReferenceProvider jaxWSXmlReferenceProvider = new JaxWSXmlReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, jaxWSXmlReferenceProvider.getAttributeCandidateNames(),
                                                         jaxWSXmlReferenceProvider.getAttributeFilter(), jaxWSXmlReferenceProvider);

      Axis2ServicesXmlReferenceProvider axis2ServicesXmlProvider = new Axis2ServicesXmlReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, axis2ServicesXmlProvider.getAttributeCandidateNames(),
                                                         axis2ServicesXmlProvider.getAttributeFilter(), axis2ServicesXmlProvider);

      instance.registerXmlTagReferenceProvider(
        project,
        axis2ServicesXmlProvider.getTagCandidateNames(),
        axis2ServicesXmlProvider.getTagFilter(),
        true,
        axis2ServicesXmlProvider
      );

      final JaxRPCMappingReferenceProvider jaxRPCMappingReferenceProvider = new JaxRPCMappingReferenceProvider(project);
      instance.registerXmlTagReferenceProvider(
        project,
        jaxRPCMappingReferenceProvider.getTagCandidateNames(),
        jaxRPCMappingReferenceProvider.getTagFilter(),
        true,
        jaxRPCMappingReferenceProvider
      );

      WADLXmlReferenceProvider wadlReferenceProvider = new WADLXmlReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, wadlReferenceProvider.getAttributeCandidateNames(), wadlReferenceProvider.getAttributeFilter(),
                                                         wadlReferenceProvider);

      JaxRPCRiXmlReferenceProvider jaxRPCRiXmlReferenceProvider = new JaxRPCRiXmlReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, jaxRPCRiXmlReferenceProvider.getAttributeCandidateNames(),
                                                         jaxRPCRiXmlReferenceProvider.getAttributeFilter(), jaxRPCRiXmlReferenceProvider);

      JBossWSXmlReferenceProvider jBossWSReferenceProvider = new JBossWSXmlReferenceProvider(project);
      instance.registerXmlAttributeValueReferenceProvider(project, jBossWSReferenceProvider.getAttributeCandidateNames(),
                                                         jBossWSReferenceProvider.getAttributeFilter(), jBossWSReferenceProvider);
    }
  }

  public void navigate(String resourceUrlString) {
    EnvironmentFacade.getInstance().openBrowserFor(resourceUrlString);
  }

  public void projectClosed() {
  }

  public String getComponentName() {
    return "WebServicesPlugin";
  }

  public static final @NonNls String INVOKE_AXIS_WS_TEMPLATE_NAME = "itws";
  public static final @NonNls String INVOKE_AXIS2_WS_TEMPLATE_NAME = "itaws";
  public static final @NonNls String INVOKE_XFIRE_WS_TEMPLATE_NAME = "ixws";
  public static final @NonNls String INVOKE_JWSDP_WS_TEMPLATE_NAME = "ijws";
  public static final @NonNls String INVOKE_UNTYPED_XFIRE_WS_TEMPLATE_NAME = "iuxws";
  public static final @NonNls String JAXB_MARSHAL_TEMPLATE_NAME = "mjo";
  public static final @NonNls String XMLBEANS_MARSHAL_TEMPLATE_NAME = "mxb";
  public static final @NonNls String XMLBEANS_UNMARSHAL_TEMPLATE_NAME = "uxb";
  public static final @NonNls String JAXB_UNMARSHAL_TEMPLATE_NAME = "ujo";
  public static final @NonNls String INVOKE_AXIS_UWS_TEMPLATE_NAME2 = "iuws";
  public static final @NonNls String INVOKE_JAXRPC_WS_TEMPLATE_NAME = "ijrws";
  public static final @NonNls String INVOKE_WEBSPHERE_WS_TEMPLATE_NAME = "iwws";
  public static final @NonNls String INVOKE_REST_WS_TEMPLATE_NAME = "jaxrs";

  private static boolean notTest = !ApplicationManager.getApplication().isUnitTestMode();

  public void initComponent() {
    registerWSLiveTemplates();
    registerXFireLiveTemplates();
    registerJAXBLiveTemplates();
    registerXmlBeansLiveTemplates();
    registerAxis2LiveTemplates();
    registerJaxWsLiveTemplates();
    registerJaxRpcLiveTemplates();
    registerWebSphereLiveTemplates();
    registerRestLiveTemplates();
  }

  private void registerJaxWsLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(INVOKE_JWSDP_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = configureWebServicesAnnotatedCompletion(INVOKE_JWSDP_WS_TEMPLATE_NAME);

      // specifically requesting false
      initContextAndSave(template, WSBundle.message("generate.jaxws.web.service.invocation.jaxws.livetemplate.description"));
    }
  }

  private Template configureWebServicesAnnotatedCompletion(String templateName) {
    Template template;
    if (EnvironmentFacade.isSelenaOrBetter()) {
      template = TemplateManager.getInstance(project).createTemplate(
        templateName,
        WS_GROUP_NAME,
        "$WebServicePortType$ $port$ = new $PortLocator$().$getPort$();\n" +
        "  //invoke business method\n" +
        "$port$.$businessMethod$($END$);"
      );

      template.addVariable("WebServicePortType", "annotated(\"javax.jws.WebService\")","\"MyService\"",true);
      template.addVariable("port","\"service\"","\"service\"", true);
      template.addVariable("PortLocator","descendantClassesEnum(\"javax.xml.ws.Service\")","\"PortLocator\"", true);
      template.addVariable("getPort","completeSmart()","\"getPort\"", true);
      template.addVariable("businessMethod","annotated(\"javax.jws.WebMethod\", \"WebServicePortType\")","\"businessMethod\"", true);
    } else {
      template = TemplateManager.getInstance(project).createTemplate(
        templateName,
        WS_GROUP_NAME);

      EnvironmentFacade facade = EnvironmentFacade.getInstance();

      final Expression expression = facade.getAnnotatedExpression("javax.jws.WebService", null);
      template.addVariable(
        "WebServicePortType",
        expression,
        new TextExpression("MyService"),
        true
      );

      template.addTextSegment(" ");
      Expression port = new TextExpression("service");
      template.addVariable("port", port, port, true);
      template.addTextSegment(" = new ");

      MacroCallNode macroCall = new MacroCallNode(MacroFactory.createMacro("descendantClassesEnum"));
      macroCall.addParameter(new TextExpression("javax.xml.ws.Service"));
      template.addVariable("WebServiceStub", macroCall, new TextExpression("PortLocator"), true);
      template.addTextSegment("().");

      Expression getPortExpr = new MacroCallNode(MacroFactory.createMacro("completeSmart"));
      template.addVariable("getPort", getPortExpr, new TextExpression("getPort()"), true);
      template.addTextSegment("();\n    //invoke business method\n");
      template.addVariableSegment("port");
      template.addTextSegment(".");

      Expression businessMethodExpr = facade.getAnnotatedExpression(
        "javax.jws.WebMethod",
        new Expression[] {TemplateImplUtil.parseTemplate("WebServicePortType") }
      );
      template.addVariable("businessMethod", businessMethodExpr, new TextExpression("businessMethod"), true);
      template.addTextSegment(" (");
      template.addEndVariable();
      template.addTextSegment(");");
    }
    return template;
  }

  private void registerWebSphereLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(INVOKE_WEBSPHERE_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_WEBSPHERE_WS_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  $WebServiceNameServiceLocator$ $locator$ = new $WebServiceNameServiceLocator$();\n" +
          "  $WebServiceName$ $service$ = $locator$.$getMethod$();\n" +
          "  // invoke business method\n" +
          "  $service$.$businessMethod$($END$);\n" +
          "} catch(Exception ex) { ex.printStackTrace(); }"
      );

      template.addVariable("WebServiceNameServiceLocator", "descendantClassesEnum(\"javax.xml.rpc.Service\")", "\"MyLocator\"", true);
      template.addVariable("locator", "\"locator\"", "\"locator\"", true);
      template.addVariable("WebServiceName", "descendantClassesEnum(\"java.rmi.Remote\")", "\"MyServiceName\"", true);
      template.addVariable("service", "\"service\"", "\"service\"", true);

      template.addVariable("getMethod", "completeSmart()", "\"getMethod\"", true);
      template.addVariable("businessMethod", "complete()", "\"businessMethod\"", true);

      template.addTextSegment(" ();");
      initContextAndSave(template, WSBundle.message("generate.websphere.webservice.invocation.websphere.livetemplate.description"));
    }
  }

  private void registerJaxRpcLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(INVOKE_JAXRPC_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_JAXRPC_WS_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  $WebServiceNameServiceLocator$ $locator$ = new $WebServiceNameServiceLocator$();\n" +
          "  $WebServiceName$ $service$ = $locator$.$getMethod$();\n" +
          "  // invoke business method\n" +
          "  $service$.$businessMethod$($END$);\n" +
          "} catch(java.rmi.RemoteException ex) { ex.printStackTrace(); }"
      );

      template.addVariable("WebServiceNameServiceLocator", "descendantClassesEnum(\"javax.xml.rpc.Service\")", "\"MyLocator\"", true);
      template.addVariable("locator", "\"locator\"", "\"locator\"", true);
      template.addVariable("WebServiceName", "descendantClassesEnum(\"java.rmi.Remote\")", "\"MyServiceName\"", true);
      template.addVariable("service", "\"service\"", "\"service\"", true);

      template.addVariable("getMethod", "completeSmart()", "get", true);
      template.addVariable("businessMethod", "complete()", "businessMethod", true);

      initContextAndSave(template, WSBundle.message("generate.jaxrpc.web.service.invocation.jaxrpc.livetemplate.description"));
    }
  }

  private void registerXmlBeansLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(XMLBEANS_MARSHAL_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        XMLBEANS_MARSHAL_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  $Type$ $object$ = $Type$.Factory.newInstance();\n" +
          "  $object$.save(new java.io.File(\"$filename$\"));\n" +
          "} catch (java.io.IOException e) {\n" +
          "  e.printStackTrace();\n" +
          "}"
      );

      template.addVariable("Type", "\"TypeToMarshal\"", "\"TypeToMarshal\"", true);
      template.addVariable("object", "\"objectToMarshal\"", "\"objectToMarshal\"", true);
      template.addVariable("filename", "complete()", "\"filename.xml\"", true);

      initContextAndSave(template, WSBundle.message("generate.xmlbeans.object.2.xml.serialization.xmlbeans.livetemplate.description"));
    }

    template = TemplateSettings.getInstance().getTemplate(XMLBEANS_UNMARSHAL_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        XMLBEANS_UNMARSHAL_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  $Type$ $object$ = $Type$.Factory.parse(\n" +
          "    new java.io.File(\"$filename$\"),\n" +
          "    null\n" +
          "  );\n" +
          "} catch (java.io.IOException e) {\n" +
          "  e.printStackTrace();\n" +
          "} catch (org.apache.xmlbeans.XmlException e) {\n" +
          "  e.printStackTrace();\n" +
          "}"
      );

      template.addVariable("Type", "\"TypeToUnmarshal\"", "\"TypeToUnmarshal\"", true);
      template.addVariable("object", "\"unmarshalledObject\"", "\"unmarshalledObject\"", true);
      template.addVariable("filename", "complete()", "\"filename.xml\"", true);
      initContextAndSave(template, WSBundle.message("generate.xmlbeans.xml.2.object.deserialization.xmlbeans.livetemplate.description"));
    }
  }

  private void registerAxis2LiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(INVOKE_AXIS2_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_AXIS2_WS_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  $WebServiceStub$ $stub$ = new $WebServiceStub$();\n" +
          "  // invoke business method\n" +
          "  $stub$.$businessMethod$($END$);\n" +
          "} catch(Exception ex) { ex.printStackTrace(); }\n"
      );

      template.addVariable("WebServiceStub", "descendantClassesEnum(\"org.apache.axis2.client.Stub\")", "\"MyServiceStub\"", true);
      template.addVariable("stub", "\"stub\"", "\"stub\"", true);
      template.addVariable("businessMethod", "complete()", "\"businessMethod\"", true);

      initContextAndSave(template, WSBundle.message("generate.axis2.web.service.invocation.axis2.livetemplate.description"));
    }
  }

  private void registerXFireLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(INVOKE_XFIRE_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = configureWebServicesAnnotatedCompletion(INVOKE_XFIRE_WS_TEMPLATE_NAME);
      
      initContextAndSave(template, WSBundle.message("generate.xfire.web.service.invocation.xfire.livetemplate.description"));
    }

    template = TemplateSettings.getInstance().getTemplate(INVOKE_UNTYPED_XFIRE_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_UNTYPED_XFIRE_WS_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  Client $client$ = new Client(new java.net.URL(\"$url$\"));\n" +
          "  Object[] $results$ = client.invoke(\"$methodname$\", new Object[] {$END$});\n" +
          "} catch(Exception e) {\n" +
          "  e.printStackTrace();\n" +
          "}"
      );

      template.addVariable("client", "\"client\"", "\"client\"", true);
      template.addVariable("url", "\"url\"", "\"url\"", true);
      template.addVariable("results", "\"results\"", "\"results\"", true);
      template.addVariable("methodname", "\"methodname\"", "\"methodname\"", true);

      initContextAndSave(template, WSBundle.message("generate.xfire.untyped.web.service.invocation.xfire.livetemplate.description"));
    }
  }

  private void registerJAXBLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(JAXB_MARSHAL_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        JAXB_MARSHAL_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  // create a JAXBContext capable of handling classes generated into package\n" +
          "  javax.xml.bind.JAXBContext jaxbContext = javax.xml.bind.JAXBContext.newInstance( \"$mypackage$\" );\n" +
          "  // create an object to marshal\n" +
          "  $Type$ $object$ = new $Type$();\n" +
          "  // create a Marshaller and do marshal\n" +
          "  javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();\n" +
          "  marshaller.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );\n" +
          "  marshaller.marshal( $object$, new java.io.FileOutputStream(\"$filename$\") );\n" +
          "} catch( javax.xml.bind.JAXBException je ) {\n" +
          "  je.printStackTrace();\n" +
          "} catch( java.io.FileNotFoundException io ) {\n" +
          "  io.printStackTrace();\n" +
          "}"
      );

      template.addVariable("mypackage", "complete()", "\"mypackage\"", true);
      template.addVariable("Type", "\"TypeToMarshal\"", "\"TypeToMarshal\"", true);
      template.addVariable("object", "\"objectToMarshal\"", "\"objectToMarshal\"", true);
      template.addVariable("filename", "complete()", "\"filename.xml\"", true);

      initContextAndSave(template, WSBundle.message("generate.jaxb.object.2.xml.serialization.jaxb.livetemplate.description"));
    }

    template = TemplateSettings.getInstance().getTemplate(JAXB_UNMARSHAL_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        JAXB_UNMARSHAL_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "  // create a JAXBContext capable of handling classes generated into package\n" +
          "  javax.xml.bind.JAXBContext jaxbContext = javax.xml.bind.JAXBContext.newInstance( \"$mypackage$\" );\n" +
          "  // create an Unmarshaller\n" +
          "  javax.xml.bind.Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();\n" +
          "  // unmarshal an instance document into a tree of Java content\n" +
          "  // objects composed of classes from the package.\n" +
          "  $Type$ $object$ = ($Type$)unmarshaller.unmarshal( new java.io.FileInputStream( \"$filename$\" ) );\n" +
          "} catch( javax.xml.bind.JAXBException je ) {\n" +
          "  je.printStackTrace();\n" +
          "} catch( java.io.IOException ioe ) {\n" +
          "  ioe.printStackTrace();\n" +
          "}"
      );

      template.addVariable("mypackage", "complete()", "\"mypackage\"", true);
      template.addVariable("Type", "\"TypeToUnmarshal\"", "\"TypeToUnmarshal\"", true);
      template.addVariable("object", "\"unmarshalledObject\"", "\"unmarshalledObject\"", true);
      template.addVariable("filename", "complete()", "\"filename.xml\"", true);
      initContextAndSave(template, WSBundle.message("generate.jaxb.xml.2.object.serialization.jaxb.livetemplate.description"));
    }
  }

  private void registerRestLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(INVOKE_REST_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_REST_WS_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
        "  com.sun.net.httpserver.HttpServer server = com.sun.jersey.api.container.httpserver.HttpServerFactory.create(\"http://localhost:9998/\");\n" +
        "  server.start();\n" +
        "  \n" +
        "  System.out.println(\"Server running\");\n" +
        "  System.out.println(\"Visit: http://localhost:9998/helloworld\");\n" +
        "  System.out.println(\"Hit return to stop...\");\n" +
        "  System.in.read();\n" +
        "  System.out.println(\"Stopping server\");   \n" +
        "  server.stop(0);\n" +
        "  System.out.println(\"Server stopped\");\n" +
        "} catch (java.io.IOException ioe) {\n" +
        "  ioe.printStackTrace(System.err);\n" +
        "}"
      );

      initContextAndSave(template, WSBundle.message("generate.axis.web.service.invocation.axis.livetemplate.description"));
    }

    template = TemplateSettings.getInstance().getTemplate(INVOKE_AXIS_UWS_TEMPLATE_NAME2);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_AXIS_UWS_TEMPLATE_NAME2,
        WS_GROUP_NAME,
        "try {" +
          "  org.apache.axis.client.Service  $service$ = new org.apache.axis.client.Service();\n" +
          "  org.apache.axis.client.Call     $call$    = (org.apache.axis.client.Call) $service$.createCall();\n" +
          "  $call$.setTargetEndpointAddress( new java.net.URL(\"$targetUrl$\") );\n" +
          "  $call$.setOperationName(new javax.xml.namespace.QName(\"$targetNs$\", \"$methodname$\"));\n" +
          "  $call$.invoke( new Object[] {$END$} );\n" +
          "} catch(javax.xml.rpc.ServiceException ex) { ex.printStackTrace(); }\n" +
          "catch(java.rmi.RemoteException ex) { ex.printStackTrace(); }\n" +
          "catch(java.net.MalformedURLException ex) { ex.printStackTrace(); }"
      );

      template.addVariable("service", "\"service\"", "\"service\"", true);
      template.addVariable("call", "\"call\"", "\"call\"", true);
      final String url = "\"http://localhost:8080/MyService\"";
      template.addVariable("targetUrl", url, url, true);
      template.addVariable("targetNs", url, url, true);
      template.addVariable("methodname", "\"businessMethod\"", "\"businessMethod\"", true);

      initContextAndSave(template, WSBundle.message("generate.axis.untyped.web.service.invocation.axis.livetemplate.description"));
    }
  }

  private void registerWSLiveTemplates() {
    Template template = TemplateSettings.getInstance().getTemplate(INVOKE_AXIS_WS_TEMPLATE_NAME);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_AXIS_WS_TEMPLATE_NAME,
        WS_GROUP_NAME,
        "try {\n" +
          "$WebServiceNameServiceLocator$ $locator$ = new $WebServiceNameServiceLocator$();\n" +
          "$WebServiceName$ $service$ = $locator$.$getMethod$();\n" +
          "  // If authorization is required\n" +
          "  //(($WS_Soap_BindingStub$)$service$).setUsername(\"user3\");\n" +
          "  //(($WS_Soap_BindingStub$)$service$).setPassword(\"pass3\");\n" +

          " // invoke business method\n" +
          "$service$.$businessMethod$($END$);\n" +
          "} catch(javax.xml.rpc.ServiceException ex) { ex.printStackTrace(); }\n" +
          "catch(java.rmi.RemoteException ex) { ex.printStackTrace(); }"
      );

      template.addVariable("WebServiceNameServiceLocator", "descendantClassesEnum(\"org.apache.axis.client.Service\")", "\"MyServiceLocator\"", true);
      template.addVariable("locator", "\"locator\"", "\"locator\"", true);
      template.addVariable("WebServiceName", "descendantClassesEnum(\"java.rmi.Remote\")", "\"MyServiceName\"", true);
      template.addVariable("service", "\"service\"", "\"service\"", true);

      template.addVariable("getMethod", "completeSmart()", "\"get\"", true);
      template.addVariable("WS_Soap_BindingStub", "descendantClassesEnum(\"org.apache.axis.client.Stub\")", "\"MyService_Soap_BindingStub\"", false);
      template.addVariable("businessMethod", "complete()", "\"businessMethod\"", true);

      initContextAndSave(template, WSBundle.message("generate.axis.web.service.invocation.axis.livetemplate.description"));
    }

    template = TemplateSettings.getInstance().getTemplate(INVOKE_AXIS_UWS_TEMPLATE_NAME2);

    if (template == null || notTest) {
      template = TemplateManager.getInstance(project).createTemplate(
        INVOKE_AXIS_UWS_TEMPLATE_NAME2,
        WS_GROUP_NAME,
        "try {" +
          "  org.apache.axis.client.Service  $service$ = new org.apache.axis.client.Service();\n" +
          "  org.apache.axis.client.Call     $call$    = (org.apache.axis.client.Call) $service$.createCall();\n" +
          "  $call$.setTargetEndpointAddress( new java.net.URL(\"$targetUrl$\") );\n" +
          "  $call$.setOperationName(new javax.xml.namespace.QName(\"$targetNs$\", \"$methodname$\"));\n" +
          "  $call$.invoke( new Object[] {$END$} );\n" +
          "} catch(javax.xml.rpc.ServiceException ex) { ex.printStackTrace(); }\n" +
          "catch(java.rmi.RemoteException ex) { ex.printStackTrace(); }\n" +
          "catch(java.net.MalformedURLException ex) { ex.printStackTrace(); }"
      );

      template.addVariable("service", "\"service\"", "\"service\"", true);
      template.addVariable("call", "\"call\"", "\"call\"", true);
      final String url = "\"http://localhost:8080/MyService\"";
      template.addVariable("targetUrl", url, url, true);
      template.addVariable("targetNs", url, url, true);
      template.addVariable("methodname", "\"businessMethod\"", "\"businessMethod\"", true);

      initContextAndSave(template, WSBundle.message("generate.axis.untyped.web.service.invocation.axis.livetemplate.description"));
    }
  }


  private static void initContextAndSave(final Template template, String description) {
    TemplateImpl templateImpl = ((TemplateImpl) template);
    templateImpl.setToShortenLongNames(false);

    TemplateContext templateContext = templateImpl.getTemplateContext();
    for(TemplateContextType contextType: Extensions.getExtensions(TemplateContextType.EP_NAME)) {
      if (contextType.isInContext(StdFileTypes.JSP)) {
        templateContext.setEnabled(contextType, true);
      }
    }

    //templateContext.JAVA_CODE.setValue(true);
    //templateContext.JSP.setValue(true);
    template.setToReformat(true);
    
    ((TemplateImpl)template).setDescription(description);

    final Application application = ApplicationManager.getApplication();

    if (application.isDispatchThread() ||
        application.isHeadlessEnvironment() // for tests
       ) {
      TemplateSettings.getInstance().addTemplate(template);
    } else {
      final Runnable runnable = new Runnable() {
        public void run() {
          TemplateSettings.getInstance().addTemplate(template);
        }
      };
      if (application.isReadAccessAllowed()) {
        application.invokeLater(runnable);
      } else {
        application.invokeAndWait(runnable, ModalityState.defaultModalityState());
      }
    }
  }

  public void disposeComponent() {
  }

  public static WebServicesPlugin getInstance(Project project) {
    WebServicesPlugin component = project.getComponent(WebServicesPlugin.class);
    if (component == null && instance != null && ApplicationManager.getApplication().isUnitTestMode()) {
      component = instance;
    }
    return component;
  }

  public List<String> getLastContexts() {
    return lastContexts;
  }

  public void addLastContext(String context) {
    if (context == null) return;
    if (lastContexts.size() == 0 || !lastContexts.get(0).equals(context)) {
      lastContexts.add(0, context);
    }
  }

  public void setServiceNameForClass(PsiClass clazz, String name) {
    classToServiceNameMap.put(clazz.getQualifiedName(), name);
  }

  public String getServiceNameForClass(PsiClass clazz) {
    return clazz != null ? classToServiceNameMap.get(clazz.getQualifiedName()) : null;
  }

  public void readExternal(Element element) throws InvalidDataException {
    List children = element.getChildren(CONTEXT_KEY);

    for (Object aChildren1 : children) {
      final Element el = (Element) aChildren1;

      lastContexts.add(el.getText());
    }

    children = element.getChildren(WSNAME_KEY);
    for (Object aChildren : children) {
      final Element el = (Element) aChildren;
      final String attributeValue = el.getAttributeValue(CLASS_KEY);
      final String attributeValue2 = el.getAttributeValue(NAME_KEY);

      classToServiceNameMap.put(attributeValue, attributeValue2);
    }

    String addRequiredClassLibs = element.getAttributeValue(TO_ADD_REQUIRED_LIBS);
    if (addRequiredClassLibs != null) {
      toAddRequiredLibraries = Boolean.valueOf(addRequiredClassLibs);
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    for (final String s : lastContexts) {
      final Element child = new Element(CONTEXT_KEY);

      element.addContent(child);
      child.setText(s);
    }

    for (Map.Entry<Object, String> entry : classToServiceNameMap.entrySet()) {
      final Element child = new Element(WSNAME_KEY);

      element.addContent(child);
      child.getAttributes().add(
        new Attribute(CLASS_KEY, entry.getKey().toString())
      );

      child.getAttributes().add(
        new Attribute(NAME_KEY, entry.getValue().toString())
      );
    }

    element.setAttribute(TO_ADD_REQUIRED_LIBS, Boolean.toString(toAddRequiredLibraries));
  }

  public static String message(String key) {
    return WSBundle.message(key);
  }

  public boolean hasSoapMessagesToolWindow(int port) {
    return monitor != null && monitor.hasPageWithPort(port);
  }

  public void createOrShowSoapMessagesToolWindow(int port) {
    if (toolWindow2 == null) {
      monitor = new AxisSOAPMonitor();
      content2 = monitor.getMainPanel();
      ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
      toolWindow2 = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID2, content2, ToolWindowAnchor.BOTTOM);
      toolWindow2.setTitle(TOOL_WINDOW_ID2);
    }

    monitor.addPage(port);

    toolWindow2.activate(null);
  }

  public boolean isToAddRequiredLibraries() {
    return toAddRequiredLibraries;
  }

  public void setToAddRequiredLibraries(boolean toAddRequiredLibraries) {
    this.toAddRequiredLibraries = toAddRequiredLibraries;
  }

  public static void setInstance(WebServicesPlugin _instance) {
    assert ApplicationManager.getApplication().isUnitTestMode();
    instance = _instance;
  }
  
  public static void setTestTemplates(boolean value) {
    notTest = true;
  } 
}
