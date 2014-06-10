/*
 * Copyright 2000-2005 JetBrains s.r.o.
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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.javaee.deployment.DeploymentManager;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import org.jdom.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@SuppressWarnings({"unchecked"})
public class TomcatUtil {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.tomcat.TomcatUtil");
  private static final int DEFAULT_SHUTDOWN_PORT = 8005;
  public static final int DEFAULT_PORT = 8080;
  @NonNls private static final String SERVICE_ELEMENT_NAME = "Service";
  @NonNls private static final String CONNECTOR_ELEMENT_NAME = "Connector";
  @NonNls private static final String HTTP11_PROTOCOL_VALUE = "HTTP/1.1";
  @NonNls private static final String PORT_ATTR = "port";
  @NonNls private static final String CLASS_NAME_ATTR = "className";
  @NonNls private static final String SSL_ATTR = "sslProtocol";
  @NonNls private static final List<String> HTTP_CONNECTOR_CLASS_NAMES =
      Arrays.asList("org.apache.catalina.connector.http.HttpConnector", "org.apache.coyote.tomcat4.CoyoteConnector");
  @NonNls private static final String ROOT_DIR_NAME = "ROOT";
  @NonNls private static final String XML_SUFFIX = ".xml";
  @NonNls private static final String CATALINA_NAME = "Catalina";
  @NonNls private static final String LOCALHOST_DIR = "localhost";
  @NonNls private static final String SERVLET_ELEMENT_NAME = "servlet";
  @NonNls private static final String J2EE_NS = "http://java.sun.com/xml/ns/j2ee";
  @NonNls private static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee";
  @NonNls private static final String KEEPGEN_PARAMETER_NAME = "keepgenerated";
  @NonNls private static final String MAPPEDFILE_PARAMETER_NAME = "mappedfile";
  @NonNls private static final String SCRDIR_PARAMETER_NAME = "scratchdir";
  @NonNls private static final String CLS_DI_PARAMETER_NAME = "classdebuginfo";
  @NonNls private static final String SERVLET_NAME_ELEM_NAME = "servlet-name";
  @NonNls private static final String JSP_VALUE = "jsp";
  @NonNls private static final String INIT_PARAM_ELEM_NAME = "init-param";
  @NonNls private static final String SERVLET_CLASS_ELEM_NAME = "servlet-class";
  @NonNls private static final String PARAM_NAME_ELEM_NAME = "param-name";
  @NonNls private static final String PARAM_VALUE_ELEM_NAME = "param-value";
  @NonNls private static final String CONTEXT_ELEM_NAME = "Context";
  @NonNls private static final String TOMCAT_HOME_ENV_PROPERTY = "TOMCAT_HOME";
  @NonNls private static final String CATALINA_HOME_ENV_PROPERTY = "CATALINA_HOME";
  @NonNls private static final String PROTOCOL_ATTR = "protocol";
  @NonNls private static final String SECURE_ATTR = "secure";
  @NonNls private static final String PATH_ATTR = "path";
  @NonNls private static final String PROTOCOL_HANDLER_ATTRIBUTE = "protocolHandlerClassName";
  private static final Map<File, Pair<Long, Integer>> ourCachedTomcatPort = new HashMap<File, Pair<Long, Integer>>();

  private TomcatUtil() {
  }

  public static File getSetEnvFile(String parentPath) {
    return new File(new File(parentPath, "bin"), "setenv." + (SystemInfo.isWindows ? "bat" : "sh"));
  }

  public static String getDefaultLocation() {
    String result = EnvironmentUtil.getEnviromentProperties().get(TOMCAT_HOME_ENV_PROPERTY);
    if (result == null) {
      result = EnvironmentUtil.getEnviromentProperties().get(CATALINA_HOME_ENV_PROPERTY);
    }

    if (result != null) {
      return result.replace(File.separatorChar, '/');
    }
    else {
      return "";
    }

  }

  public static int getShutdownPort(File serverXml) {
    try {
      final Document document = JDOMUtil.loadDocument(serverXml);
      final Element root = document.getRootElement();
      final String portString = root.getAttributeValue(PORT_ATTR);
      return Integer.parseInt(portString);
    }
    catch (Exception e) {
      return DEFAULT_SHUTDOWN_PORT;
    }
  }

  public static int getPort(File serverXmlFile) {
    Pair<Long, Integer> cached = ourCachedTomcatPort.get(serverXmlFile);
    if (cached != null && serverXmlFile.lastModified() == cached.getFirst()) {
      return cached.getSecond();
    }
    int port = readTomcatPort(serverXmlFile);
    ourCachedTomcatPort.put(serverXmlFile, Pair.create(serverXmlFile.lastModified(), port));
    return port;

  }

  private static int readTomcatPort(final File serverXmlFile) {
    try {

      Document document = loadXMLFile(serverXmlFile.getAbsolutePath());
      Element rootElement = document.getRootElement();
      if (rootElement == null) {
        return DEFAULT_PORT;
      }
      List<Element> services = rootElement.getChildren(SERVICE_ELEMENT_NAME);

      int port = DEFAULT_PORT;

      for (final Element service : services) {
        List<Element> connectors = service.getChildren(CONNECTOR_ELEMENT_NAME);
        for (final Element connector : connectors) {
          String protocol = connector.getAttributeValue(PROTOCOL_ATTR);

          String portAttribute = connector.getAttributeValue(PORT_ATTR);
          if (protocol != null && protocol.equals(HTTP11_PROTOCOL_VALUE)) {
            return getPortFrom(portAttribute);
          }

          String className = connector.getAttributeValue(CLASS_NAME_ATTR);

          if (connector.getAttributeValue(SSL_ATTR) != null) {
            continue;
          }

          String secure = connector.getAttributeValue(SECURE_ATTR);
          if (Boolean.TRUE.toString().equals(secure)) {
            continue;
          }

          if (protocol == null && className == null ||
              HTTP_CONNECTOR_CLASS_NAMES.contains(className) && connector.getAttributeValue(PROTOCOL_HANDLER_ATTRIBUTE) == null) {
            port = getPortFrom(portAttribute);
          }
        }
      }

      return port;
    }
    catch (Exception ignored) {
    }
    return DEFAULT_PORT;
  }

  private static int getPortFrom(String attributeValue) {
    if (attributeValue == null) {
      return DEFAULT_PORT;
    }
    try {
      return Integer.parseInt(attributeValue);
    }
    catch (NumberFormatException e) {
      return DEFAULT_PORT;
    }
  }

  @Nullable
  private static Element findContextInServerXML(String baseDirectoryPath, String contextPath) throws ExecutionException {
    String xmlPath = serverXML(baseDirectoryPath);
    Document sourceServerXmlDocument = loadXMLFile(xmlPath);
    if (contextPath == null) {
      throw new ExecutionException(TomcatBundle.message("exception.text.context.path.not.configured"));
    }

    Element sourceLocalHost = findLocalHost(sourceServerXmlDocument.getRootElement());

    Element contextElement = findContextByPath(sourceLocalHost, contextPath);

    if (contextElement == null && contextPath.equals("/")) {
      contextElement = findContextByPath(sourceLocalHost, "");
    }

    return contextElement;
  }

  /**
   * search in META-INF/context.xml
   */
  @Nullable
  private static Element findContextInContextXml(TomcatModuleDeploymentModel tomcatModel) {
    final String deploymentPath = getDeploymentPath(tomcatModel);
    if (deploymentPath == null) {
      return null;
    }

    File deploymentRoot = new File(deploymentPath);
    String contextXmlPath = TomcatConstants.CONTEXT_XML_META_DATA.getDirectoryPath() + "/" + TomcatConstants.CONTEXT_XML_META_DATA.getFileName();
    try {
      if (deploymentRoot.isDirectory()) {
        Document contextDocument = loadXMLFile(deploymentPath + File.separator + FileUtil.toSystemDependentName(contextXmlPath));
        return contextDocument.getRootElement();
      }
      if (deploymentRoot.isFile()) {
        JarFile jarFile = new JarFile(deploymentRoot);
        try {
          ZipEntry entry = jarFile.getEntry(contextXmlPath);
          if (entry != null) {
            Document document = JDOMUtil.loadDocument(jarFile.getInputStream(entry));
            return document.getRootElement();
          }
        }
        catch (JDOMException e) {
          LOG.info(e);
        }
        finally {
          jarFile.close();
        }
      }
    }
    catch (ExecutionException e) {
      LOG.info(e);
    }
    catch (IOException e) {
      LOG.info(e);
    }

    return null;
  }

  @Nullable
  private static Element findContextInHostDirectory(String baseDirectoryPath, String contextPath) {
    String contextXmlPath = getContextXML(baseDirectoryPath, contextPath);
    try {
      Document contextDocument = loadXMLFile(contextXmlPath);
      return contextDocument.getRootElement();
    }
    catch (ExecutionException ignored) {
    }

    return null;
  }

  public static String getContextXML(final String baseDirectoryPath, final String contextPath) {
    final String contextFileName = "".equals(contextPath)? ROOT_DIR_NAME : contextPath.substring(1);
    return hostDir(baseDirectoryPath) + File.separator + contextFileName.replace('/', '#').replace('\\', '#') + XML_SUFFIX;
  }

  public static String hostDir(String baseDirectoryPath) {
    return baseConfigDir(baseDirectoryPath) + File.separator + CATALINA_NAME + File.separator + LOCALHOST_DIR;
  }

  public static Document loadXMLFile(String xmlPath) throws ExecutionException {
    try {
      return JDOMUtil.loadDocument(new File(xmlPath));
    }
    catch (JDOMException e) {
      throw new ExecutionException(TomcatBundle.message("exception.text.cannot.load.file.bacause.of.1", xmlPath, e.getMessage()));
    }
    catch (IOException e) {
      throw new ExecutionException(TomcatBundle.message("exception.text.cannot.load.file.bacause.of.1", xmlPath, e.getMessage()));
    }
  }

  public static @NonNls String getHostLogFilePattern(String baseDirectoryPath) {
    return getLogsDirPath(baseDirectoryPath) + File.separator + "localhost*";
  }

  @NonNls
  public static String getLogsDirPath(String baseDirectoryPath) {
    return baseDirectoryPath + File.separator + "logs";
  }

  public static String serverXML(String baseDirectoryPath) {
    return baseConfigDir(baseDirectoryPath) + File.separator + TomcatConstants.SERVER_XML;
  }

  private static String getBackupPath(String path) {
    int i = 0;
    while(new File(path + "." + i).exists()) {
      i++;
    }
    return path + "." + i;
  }

  public static void saveXMLFile(Document xmlDocument, String xmlPath, boolean backupOriginalIfExists) throws ExecutionException {
    final File xmlFile = new File(xmlPath);

    if(backupOriginalIfExists && xmlFile.exists()) {
      String backupPath = getBackupPath(xmlPath);
      try {
        FileUtil.copy(xmlFile, new File(backupPath));
      }
      catch (IOException e) {
        throw new ExecutionException(
          TomcatBundle.message("exception.text.cannot.copy.0.to.1.because.of.2", xmlPath, backupPath, e.getMessage()));
      }
    }

    try {
      JDOMUtil.writeDocument(xmlDocument, xmlPath, "\n");
    }
    catch (IOException e) {
      throw new ExecutionException(TomcatBundle.message("exception.text.cannot.write.0.because.of.1", xmlPath, e.getMessage()));
    }
  }

  public static void configureWebXml(TomcatModel tomcatConfiguration) throws ExecutionException {
    final String webXmlPath = tomcatConfiguration.getBaseDirectoryPath() + File.separator + TomcatConstants.CATALINA_CONFIG_DIRECTORY_NAME + File.separator + TomcatConstants.WEB_XML;
    final Document webXmlDocument = loadXMLFile(webXmlPath);
    final Element rootElement = webXmlDocument.getRootElement();
    final List<Element> servlets = rootElement.getChildren(SERVLET_ELEMENT_NAME);
    Element jspServlet = findJspServlet(servlets, null);

    Namespace namespace = null;

    if (jspServlet==null) {
      // for Tomcat 5.0.28+ web.xml verasion is 2.4, there are namespaces present so search accordingly
      // @todo namespace prefix could be different
      namespace = Namespace.getNamespace("",J2EE_NS);
      jspServlet = findJspServlet(rootElement.getChildren(SERVLET_ELEMENT_NAME,namespace), namespace);

      if (jspServlet == null) {
        namespace = Namespace.getNamespace(JAVAEE_NS);
        jspServlet = findJspServlet(rootElement.getChildren(SERVLET_ELEMENT_NAME, namespace), namespace);
      }
    }

    if (jspServlet == null) {
      String pathToWebXml = tomcatConfiguration.getBaseDirectoryPath() + File.separator + TomcatConstants.WEB_XML;
      String message = TomcatBundle.message("exception.text.cannot.find.configuration", pathToWebXml);
      throw new ExecutionException(message);
    }

    if(!tomcatConfiguration.versionHigher(TomcatPersistentData.VERSION50)) {
      setParameter(jspServlet, KEEPGEN_PARAMETER_NAME, Boolean.TRUE.toString(),namespace);
      setParameter(jspServlet, MAPPEDFILE_PARAMETER_NAME, Boolean.TRUE.toString(),namespace);

      String scratchdir = getGeneratedFilesPath(tomcatConfiguration).replace('/', File.separatorChar);
      new File(scratchdir).mkdirs();

      setParameter(jspServlet, SCRDIR_PARAMETER_NAME, scratchdir,namespace);
    }

    setParameter(jspServlet, CLS_DI_PARAMETER_NAME, Boolean.TRUE.toString(),namespace);
    saveXMLFile(webXmlDocument, webXmlPath, true);
  }

  @Nullable
  private static Element findJspServlet(List<Element> servlets, Namespace namespace) {
    Element jspServlet = null;
    for (final Element servlet : servlets) {
      Element nameParam = servlet.getChild(SERVLET_NAME_ELEM_NAME, namespace);
      if (nameParam == null) {
        continue;
      }
      if (!JSP_VALUE.equalsIgnoreCase(nameParam.getText())) {
        continue;
      }
      jspServlet = servlet;
      break;
    }
    return jspServlet;
  }

  private static void setParameter(Element servletNode, String parameterName, String parameterValue, Namespace namespace) {
    Element parameter = findParameter(servletNode, parameterName,namespace);
    if (parameter == null) {
      parameter = new Element(INIT_PARAM_ELEM_NAME,namespace);
      //The content of element type "servlet" must match
      //  "(icon?,servlet-name,display-name?,description?,(servlet-class|jsp-file),init-param*,load-on-startup?,run-as?,security-role-ref*)".
      Element anchor = servletNode.getChild(SERVLET_CLASS_ELEM_NAME,namespace);
      insertChildAfter(servletNode, parameter, anchor);
      //servletNode.addContent(parameter);
      Element name = new Element(PARAM_NAME_ELEM_NAME,namespace);
      parameter.addContent(name);
      name.setText(parameterName);
    }
    Element value = parameter.getChild(PARAM_VALUE_ELEM_NAME,namespace);
    if (value == null) {
      value = new Element(PARAM_VALUE_ELEM_NAME,namespace);
      parameter.addContent(value);
    }
    value.setText(parameterValue);
  }

  @Nullable
  private static Element findParameter(Element servletNode, String parameterName,Namespace namespace) {
    List<Element> parameters = servletNode.getChildren(INIT_PARAM_ELEM_NAME,namespace);
    for (final Element param : parameters) {
      Element name = param.getChild(PARAM_NAME_ELEM_NAME, namespace);
      if (name != null && parameterName.equalsIgnoreCase(name.getText())) {
        return param;
      }
    }
    return null;
  }

  private static void insertChildAfter(Element parent, Content child, Content anchor) {
    List<Content> content = parent.getContent();
    List<Content> newContent = new ArrayList<Content>(content.size());
    for (Content contentElement : content) {
      if (contentElement instanceof Element) {
        Element element = (Element)contentElement;
        newContent.add((Content)element.clone());
      }
      else if (contentElement instanceof Text) {
        Text t = (Text)contentElement;
        newContent.add((Content)t.clone());
      }
      else {
        newContent.add(contentElement);
      }
      if (anchor.equals(contentElement)) {
        newContent.add(child);
      }
    }
    parent.setContent(newContent);
  }

  @Nullable
  public static Element findContextByPath(Element topElement, String contextPath) {
    return findElementByAttr(topElement, CONTEXT_ELEM_NAME, PATH_ATTR, contextPath);
  }

  public static Element findLocalHost(Element parentElement) throws ExecutionException {
    Element localhost = findElementByAttr(parentElement, "Host", "name", LOCALHOST_DIR);
    if(localhost == null) {
      throw new ExecutionException(TomcatBundle.message("exception.text.server.xml.does.not.contain.virtual.host.localhost"));
    }
    return localhost;
  }

  @Nullable
  private static Element findElementByAttr(Element parentElement,
                                           @NonNls String tagName,
                                           @NonNls String attrName,
                                           @NonNls final String attrValue) {
    if (tagName.equalsIgnoreCase(parentElement.getName())) {
      String path = parentElement.getAttributeValue(attrName);
      if (path != null) {
        if (path.equalsIgnoreCase(attrValue)) {
          return parentElement;
        }
      }
    }
    List<Element> children = parentElement.getChildren();
    for (final Element child : children) {
      Element elem = findElementByAttr(child, tagName, attrName, attrValue);
      if (elem != null) {
        return elem;
      }
    }
    return null;
  }

  public static String getGeneratedFilesPath(TomcatModel tomcatConfiguration) {
    String baseDirectoryPath = tomcatConfiguration.getBaseDirectoryPath();
    return FileUtil.toSystemIndependentName(baseDirectoryPath) + "/" + TomcatConstants.CATALINA_WORK_DIRECTORY_NAME + "/" +
           TomcatConstants.SCRATCHDIR_NAME;
  }

  public static Collection<String> getConfiguredContextPaths(TomcatModel configuration) throws RuntimeConfigurationException {
    Set<String> result = new HashSet<String>();

    try {
      result.addAll(getContextPathsFromServerXML(configuration));
    }
    catch (ExecutionException ignored) {
    }

    result.addAll(getContextPathsFromDirectory(configuration));

    return result;

  }

  private static Collection<String> getContextPathsFromDirectory(TomcatModel model) throws RuntimeConfigurationException {
    File hostDir = new File(hostDir(model.getSourceBaseDirectoryPath()));
    File[] files = hostDir.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return StringUtil.endsWithIgnoreCase(pathname.getName(), XML_SUFFIX);
      }
    });
    Set<String> names = new HashSet<String>();
    if (files != null) {
      for (File file : files) {
        String fName = file.getName();
        names.add("/" + fName.substring(0, fName.length() - 4));
      }
    }
    return names;
  }

  private static Collection<String> getContextPathsFromServerXML(TomcatModel model) throws ExecutionException,
                                                                                           RuntimeConfigurationException {
    String xmlPath = serverXML(model.getSourceBaseDirectoryPath());

    Document document = loadXMLFile(xmlPath);
    Element localHost = findLocalHost(document.getRootElement());

    List<Element> contextElements = localHost.getChildren(CONTEXT_ELEM_NAME);

    Set<String> contexts = new HashSet<String>(contextElements.size());

    for (final Element contextElement : contextElements) {
      String path = contextElement.getAttributeValue(PATH_ATTR);
      if (path != null) {
        contexts.add(path);
      }
    }

    return contexts;
  }

  public static String baseConfigDir(String baseDirectoryPath) {
    return baseDirectoryPath + File.separator + TomcatConstants.CATALINA_CONFIG_DIRECTORY_NAME;
  }

  public static void removeContextItem(TomcatModel tomcatModel, ContextItem contextItem) throws ExecutionException {
    if(contextItem.getElement().isRootElement()) {
      if(!contextItem.getFile().delete()) {
        throw new ExecutionException(TomcatBundle.message("exception.text.cannot.delete.file", contextItem.getFile().getAbsolutePath()));
      }
    }
    else {
      Document document = contextItem.getElement().getDocument();
      contextItem.getElement().getParent().removeContent(contextItem.getElement());
      saveXMLFile(document, serverXML(tomcatModel.getBaseDirectoryPath()), true);
    }
  }

  public static List<ContextItem> getContexts(TomcatModel tomcatModel) throws ExecutionException {
    ArrayList<ContextItem> result = new ArrayList<ContextItem>();

    if(!tomcatModel.isLocal()) return result;

    String xmlPath = serverXML(tomcatModel.getBaseDirectoryPath());
    File xmlFile = new File(xmlPath);

    Document serverXmlDocument = loadXMLFile(xmlPath);
    Element localHost = findLocalHost(serverXmlDocument.getRootElement());
    List<Element> contexts = localHost.getChildren(CONTEXT_ELEM_NAME);
    for (final Element context : contexts) {
      result.add(new ContextItem(xmlFile, context));
    }

    if (tomcatModel.versionHigher(TomcatPersistentData.VERSION50)) {
      File hostDir = new File(hostDir(tomcatModel.getBaseDirectoryPath()));

      File[] contextFiles = hostDir.listFiles();

      if (contextFiles != null) {
        for (File contextFile : contextFiles) {
          if (contextFile.getName().endsWith(XML_SUFFIX)) {
            Document document = loadXMLFile(contextFile.getAbsolutePath());
            if (document.getRootElement() != null && CONTEXT_ELEM_NAME.equals(document.getRootElement().getName())) {
              result.add(new ContextItem(contextFile, document.getRootElement()));
            }
          }
        }
      }
    }

    return result;
  }

  @Nullable
  public static Element findContextElement(String baseDirectoryPath, String contextPath, TomcatModuleDeploymentModel deploymentModel) throws ExecutionException {
    final TomcatModel serverModel = ((TomcatModel)deploymentModel.getServerModel());
    final boolean isVersion5OrHigher = serverModel.versionHigher(TomcatPersistentData.VERSION50);

    Element contextElement = null;

    if (isVersion5OrHigher) {
      contextElement = findContextInContextXml(deploymentModel);
    }

    if (contextElement == null) {
      contextElement = findContextInServerXML(baseDirectoryPath, contextPath);
    }

    if (isVersion5OrHigher && contextElement == null) {
      contextElement = findContextInHostDirectory(baseDirectoryPath, contextPath);
    }
    
    return contextElement;
  }

  @Nullable
  public static String getDeploymentPath(DeploymentModel deploymentModel) {
    return DeploymentManager.getInstance(deploymentModel.getCommonModel().getProject()).getDeploymentSourcePath(deploymentModel);
  }

  public static class ContextItem {
    private final File myFile;
    private final Element myElement;

    public ContextItem(File file, Element element) {
      myFile = file;
      myElement = element;
    }

    public File getFile() {
      return myFile;
    }

    public Element getElement() {
      return myElement;
    }
  }
}
