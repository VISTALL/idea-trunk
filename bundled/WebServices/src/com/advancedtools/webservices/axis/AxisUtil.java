package com.advancedtools.webservices.axis;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 08.12.2004
 * Time: 22:54:17
 * To change this template use File | Settings | File Templates.
 */
public class AxisUtil {
  private static final Logger LOG = Logger.getInstance("webservicesplugin.axisinvokator");
  public static final String TYPE_MAPPING_1_2 = "1.2";
  public static final String TYPE_MAPPING_1_1 = "1.1";
  public static final String REQUEST_SCOPE = "Request";
  public static final String SESSION_SCOPE = "Session";
  public static final String APPLICATION_SCOPE = "Application";

  public static final String SERVLET_PATH = "servlet";
  public static final String ORG_APACHE_AXIS_WSDL_JAVA2_WSDL = "org.apache.axis.wsdl.Java2WSDL";
  private static final String ORG_APACHE_AXIS_WSDL_WSDL2_JAVA = "org.apache.axis.wsdl.WSDL2Java";
  private static final String ORG_APACHE_AXIS_CLIENT_ADMIN_CLIENT = "org.apache.axis.client.AdminClient";

  private AxisUtil() {}

  static void invokeDeployment(final String deploymentText, final String contextName, Project project) throws RuntimeException, InvokeExternalCodeUtil.ExternalCodeException {
    invokeAxisAdminInner(deploymentText, contextName, project);
  }

  private static void invokeAxisAdminInner(String deploymentText, String contextName, Project project) throws InvokeExternalCodeUtil.ExternalCodeException {
    try {
      File tempFile = File.createTempFile("deploy", ".wsdd");
      tempFile.deleteOnExit();

      Writer writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(tempFile)));
      writer.write(deploymentText);
      writer.close();

      List<String> parameters = new LinkedList<String>();
      parameters.add("-l" + getWebServiceUrlReference(contextName, "AdminService"));
      parameters.add(tempFile.getPath());
      String[] args = parameters.toArray(new String[parameters.size()]);

      InvokeExternalCodeUtil.invokeExternalProcess(
        new InvokeExternalCodeUtil.JavaExternalProcessHandler(
          "Axis Admin",
          ORG_APACHE_AXIS_CLIENT_ADMIN_CLIENT,
          LibUtils.getLibUrlsForToolRunning(
            WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(AxisWSEngine.AXIS_PLATFORM),
            null
          ),
          args,
          null,
          false
        ),
        project
      );
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  public static boolean simpleCheckThatServerOnLocalHostIsRunning() {
    return simplePing(getTomcatUrl(), true);
  }

  private static String getTomcatUrl() {
    WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    return "http://" + instance.getHostName() + ":" + instance.getHostPort();
  }

  public static String getServiceWsdl(String contextName, String serviceName) {
    InputStream inputStream = null;
    try {
      inputStream = new URL(getWsdFileContent(contextName, serviceName)).openStream();
      final Reader reader = new InputStreamReader(inputStream);
      final char[] buf = new char[8192];
      final StringBuffer buffer = new StringBuffer(buf.length);

      while(true) {
        int read = reader.read(buf);
        if (read == -1) break;
        buffer.append(buf,0,read);
      }
      inputStream.close();
      return buffer.toString();
    }
    catch (IOException e) {
      return null;
    } finally {
      if (inputStream != null) {
        try { inputStream.close(); } catch(IOException ex) {}
      }
    }
  }

  static boolean simpleCheckExistenceOfService(String contextName, String serviceName) {
    return simplePing(getWsdFileContent(contextName, serviceName),false);
  }

  private static String getWsdFileContent(String contextName, String serviceName) {
    return getWebServiceUrlReference(contextName, serviceName) + "?wsdl";
  }

  public static String getWebServiceUrlReference(String contextName, String serviceName) {
    return getContextUrl(contextName) + WebServicesPluginSettings.getInstance().getWebServicesUrlPathPrefix() + "/" + serviceName;
  }

  public static String getWebServiceNS(String contextName, String serviceName) {
    WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    return "http://" + instance.getHostName() + (contextName.length() > 0? "/" + contextName:"") + instance.getWebServicesUrlPathPrefix() + "/" + serviceName;
  }

  private static String getContextUrl(String contextName) {
    String result = getTomcatUrl();
    if (contextName != null && contextName.length() > 0) result  = result + "/" + contextName;
    return result;
  }

  private static boolean simplePing(String url, boolean acceptAnyResponse) {
    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.connect();
      connection.disconnect();
      return acceptAnyResponse || connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }
    catch (IOException e) {
      if (LOG.isDebugEnabled()) LOG.debug(e);
      return false;
    }
  }

  public static boolean simpleHappyServlet(String contextName,String servletName) {
    return simplePing(getContextUrl(contextName) + "/servlet/" + servletName, false);
  }

  public static boolean simpleHappyAxisAdmin(String contextName) {
    return simplePing(getContextUrl(contextName) + "/servlet/AdminServlet", false);
  }

  public static boolean simpleHappyAxisMonitor(String contextName) {
    return simplePing(getContextUrl(contextName) + "/SOAPMonitor", false);
  }

  static void ensureSoapMonitorDeployed(String contextName, Project project) {
    try {
      runDeployment(
        "SOAPMonitorService",
        contextName,
        "org.apache.axis.monitor.SOAPMonitorService",
        "publishMessage",
        null,
        false,
        "<handler name=\"soapmonitor\"\n" +
          "        type=\"java:org.apache.axis.handlers.SOAPMonitorHandler\">\n" +
          "      <parameter name=\"wsdlURL\" \n" +
          "        value=\"/axis/SOAPMonitorService-impl.wsdl\"/>\n" +
          "      <parameter name=\"namespace\" \n" +
          "        value=\"http://tempuri.org/wsdl/2001/12/SOAPMonitorService-impl.wsdl\"/>\n" +
          "      <parameter name=\"serviceName\" value=\"SOAPMonitorService\"/>\n" +
          "      <parameter name=\"portName\" value=\"Demo\"/>\n" +
          "    </handler>\n",
        APPLICATION_SCOPE,
        null,
        project
      );
    } catch (InvokeExternalCodeUtil.ExternalCodeException e) {
      LOG.error(e);
    }
  }

  static void runDeployment(String nameOfService, String contextName, String classToDeploy,
                            String allowedMethods, List<String> nonelementaryTypes,
                            boolean supportSoapMonitoring,String handlerText, String scope, String style, Project project) throws
    InvokeExternalCodeUtil.ExternalCodeException {
    String deploymentPrefix = "<deployment name=\"test\" xmlns=\"http://xml.apache.org/axis/wsdd/\" \n" +
      "    xmlns:java=\"http://xml.apache.org/axis/wsdd/providers/java\">";

    if (handlerText != null) {
      deploymentPrefix += handlerText;
    }
    deploymentPrefix +=  "  <service name=\"" + nameOfService + "\" provider=\"java:RPC\"";
    if (style != null) deploymentPrefix += " style=\"" + style.toLowerCase() + "\"";
    deploymentPrefix += ">\n";

    if (supportSoapMonitoring) {
      deploymentPrefix += "<requestFlow>\n" +
        "      <handler type=\"soapmonitor\"/>\n" +
        "    </requestFlow>\n" +
        "    <responseFlow>\n" +
        "      <handler type=\"soapmonitor\"/>\n" +
        "    </responseFlow>\n";
    }

    deploymentPrefix +=
      "    <parameter name=\"className\" value=\"" + classToDeploy + "\" />\n" +
      "    <parameter name=\"allowedMethods\" value=\"" + allowedMethods + "\" />\n";

    if (scope != null) {
      deploymentPrefix += "<parameter name=\"scope\" value=\""+scope + "\"/>\n";
    }

    String deploymentText = deploymentPrefix;

    if (nonelementaryTypes != null) {
      for (String s : nonelementaryTypes) {
        deploymentText += "    <beanMapping qname=\"myNS:" + s.substring(s.lastIndexOf('.') + 1) + "\" xmlns:myNS=\"" + nameOfService + "\" languageSpecificType=\"java:" + s + "\"/>\n";
      }
    }

    deploymentText += "  </service>\n" +
      "</deployment>";

    invokeDeployment(deploymentText, contextName, project);
  }

  static void runUndeployment(String nameOfService, String contextName, Project project) throws InvokeExternalCodeUtil.ExternalCodeException {
    String deploymentText =
      "<undeployment name=\"test\" xmlns=\"http://xml.apache.org/axis/wsdd/\">\n" +
      "  <service name=\"" + nameOfService + "\"/>\n" +
      "</undeployment>";

    invokeDeployment(deploymentText, contextName, project);
  }
}
