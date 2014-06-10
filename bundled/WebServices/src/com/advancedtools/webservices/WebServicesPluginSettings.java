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

import com.advancedtools.webservices.axis2.Axis2Utils;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.inspections.*;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.utils.facet.WebServicesClientLibraries;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.wsengine.WSEngineManager;
import com.advancedtools.webservices.xfire.XFireUtils;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.ide.fileTemplates.*;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.util.Processor;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

/**
 * @by maxim
 */
public class WebServicesPluginSettings extends FileTypeFactory implements ApplicationComponent, JDOMExternalizable,
  InspectionToolProvider, FileTemplateGroupDescriptorFactory, SearchableConfigurable {
  private final LinkedList<String> lastPackagePrefixes = new LinkedList<String>();
  private final LinkedList<String> lastWsdlUrls = new LinkedList<String>();
  private final LinkedList<String> lastJaxbUrls = new LinkedList<String>();
  private final Map<String, Long> myPackGzLibs = new HashMap<String, Long>();
  private int myMemorySizeToLaunchVM = DEFAULT_MEMORY_SIZE_TO_LAUNCH_VM;
  private final LinkedList<String> lastXmlBeansUrls = new LinkedList<String>();

  private boolean makeGeneratedFilesReadOnly = true;

  private String myJwsdpPath;
  private String myXFirePath;
  private String myAxis2Path;
  private String myJBossWSPath;
  private String myWebSphereWSPath;
  private String myXmlBeansPath;
  private String mySunWebDevelopmentPackPath;

  private String myLastPlatform;
  private String myLastBinding;
  private @NonNls String myWebServicesUrlPathPrefix = "/services";
  private @NonNls String myLastRestClientHost = "http://localhost:9998";
  private String myAskToInstallSoapUI = Boolean.toString(true);

  private final WSEngineManager myEngineManager = new WSEngineManager();

  private static final @NonNls String LAST_IDEA_VERSION = "LastIDEAVersion";
  private static final @NonNls String GEN_CLIENT_STUB_URL_KEY = "StubUrl";
  private static final @NonNls String GEN_JAXB_STUB_URL_KEY = "JAXBUrl";
  private static final @NonNls String GEN_XMLBEANS_STUB_URL_KEY = "XmlBeansUrl";

  private static final @NonNls String WEB_SERVICES_URL_PATH_PREFIX_KEY = "WebServicesUrlPathPrefix";
  private static final @NonNls String GENERATED_FILES_READ_WRITE_KEY = "WritableGeneratedFiles";

  private static final @NonNls String JWSDP_PATH_KEY = "PathToJWSDP";
  private static final @NonNls String SWDP_PATH_KEY = "PathToSWDP";
  private static final @NonNls String XFIRE_PATH_KEY = "PathToXFire";

  private static final @NonNls String XMLBEANS_PATH_KEY = "PathToXmlBeans";
  private static final @NonNls String AXIS2_PATH_KEY = "PathToAxis2";
  private static final @NonNls String JBOSSWS_PATH_KEY = "PathToJBossWS";
  private static final @NonNls String MEMORY_SIZE_TO_LAUNCH_VM_KEY = "MemorySizeToLaunchVM";

  private static final @NonNls String WEBSPHERE_WS_PATH_KEY = "PathToWebSphere";
  private static final @NonNls String SERVER_NAME_KEY = "ServerName";
  private static final @NonNls String LAST_PLATFORM_KEY = "SelectedPlatform";

  private static final @NonNls String LAST_BINDING_KEY = "SelectedBinding";
  private static final @NonNls String SERVER_PORT_KEY = "ServerPort";
  private static final @NonNls String GEN_CLIENT_STUB_PREFIX_KEY = "StubPackage";

  private static final @NonNls String LAST_REST_CLIENT_HOST_KEY = "RestClientHost";
  private static final @NonNls String ASK_TO_INSTALL_SOAPUI = "ASK_TO_INSTALL_SOAPUI";
  private static final @NonNls String LIBS = "Libs";
  private static final @NonNls String LIB = "Lib";
  private static final @NonNls String LIB_PATH = "path";
  private static final @NonNls String LIB_SIZE = "size";

  @NonNls public static final String HTTP_SCHEMAS_XMLSOAP_ORG_WSDL = "http://schemas.xmlsoap.org/wsdl/";
  @NonNls public static final String HTTP_WWW_W3_ORG_2003_03_WSDL = "http://www.w3.org/2003/03/wsdl";

  @NonNls public static final String WSDD_FILE_EXTENSION = "wsdd";
  @NonNls public static final String XJB_FILE_EXTENSION = "xjb";
  @NonNls public static final String WADL_FILE_EXTENSION = "wadl";
  @NonNls public static final String WSDL_FILE_EXTENSION = "wsdl";
  @NonNls public static final String XSD_FILE_EXTENSION = "xsd";

  private static final @NonNls String DEFAULT_HOST_NAME = "localhost";
  private static final @NonNls String DEFAULT_HOST_PORT = "8080";

  @NonNls public static final String XFIRE_SERVICES_XML = "services.xml";
  @NonNls public static final String SUN_JAXWS_XML = "sun-jaxws.xml";
  @NonNls public static final String JAXRPC_XML = "jax-rpc.xml";
  @NonNls public static final String JAXRPC_RI_RUNTIME_XML = "jaxrpc-ri-runtime.xml";

  private static final int MAX_HISTORY_ITEMS = 25;
  private static WebServicesPluginSettings myInstance;
  private static final @NonNls String PACK_GZ_SUFFIX = ".pack.gz";
  @NonNls public static final String CXF_SERVLET_XML = "cxf-servlet.xml";
  @NonNls public static final String XML_FILE_EXTENSION = "xml";
  
  @NonNls
  public static final String JAXWS_WEBSERVICE_TEMPLATE_NAME = "jaxws.webservice.java";
  
  @NonNls
  public static final String POJO_WEBSERVICE_TEMPLATE_NAME = "pojo.webservice.java";
  
  @NonNls
  public static final String J2EE1_4_WEBSERVICE_TEMPLATE_NAME = "j2ee1_4.webservice.java";
  
  @NonNls
  public static final String J2EE1_4_WEBSERVICE_INTERFACE_TEMPLATE_NAME = "j2ee1_4.webservice.interface.java";

  @NonNls
  public static final String RESTFUL_WEBSERVICE_TEMPLATE_NAME = "rest.webservice.java";
  
  @NonNls
  public static final String DEFAULT_WEBSERVICE_CLIENT_TEMPLATE_NAME = "default.webservice.client.java";
  
  @NonNls 
  public static final String PACKAGE_NAME_TEMPLATE_ARG =  "PACKAGE_NAME";
  
  @NonNls 
  public static final String CLASS_NAME_TEMPLATE_ARG =  "CLASS_NAME";
  
  @NonNls 
  public static final String INTERFACE_NAME_TEMPLATE_ARG =  "INTERFACE_NAME";
  private static final int DEFAULT_MEMORY_SIZE_TO_LAUNCH_VM = 128;

  public String getComponentName() {
    return "WebServicesPluginSettings";
  }

  public void initComponent() {

    EnvironmentFacade.getInstance().executeOnPooledThread(new Runnable() {
      public void run() {
        if (ApplicationManager.getApplication().isUnitTestMode()) return;
        final Set<File> libJarsToUnpack = new THashSet<File>();
        final Set<File> jarsToExpand = new THashSet<File>();

        final String pluginPath = LibUtils.detectPluginPath() + File.separatorChar;

        final File[] xmlbeansDir = new File[1];
        final File[] jaxwsDir = new File[1];

        final File[] dirsWithLibs = new File(pluginPath + "lib").listFiles(new FileFilter() {
          public boolean accept(File file) {
            final @NonNls String fileName = file.getName();
            if (fileName.indexOf("jaxws-") >= 0) jaxwsDir[0] = file;
            if (fileName.indexOf("xmlbeans-") >= 0) xmlbeansDir[0] = file;
            return file.isDirectory();
          }
        });

        final @NonNls String ext = ".jar" + PACK_GZ_SUFFIX;

        final FilenameFilter filter = new FilenameFilter() {
          public boolean accept(File file, String s) {
            return s.endsWith(ext);
          }
        };

        if (dirsWithLibs != null) {
          for (File dir : dirsWithLibs) {
            final File[] files = dir.listFiles(filter);
            if (files != null) for (File f : files) libJarsToUnpack.add(f);
          }
        }

        final @NonNls String[] dirsThatContainsJarsToExpand = {"docs/axis1_4"};
        for (String dir : dirsThatContainsJarsToExpand) {
          final File[] files = new File(pluginPath + dir).listFiles(filter);
          if (files != null) for (File f : files) jarsToExpand.add(f);
        }

        try {
          boolean expandedSomething = false;
          
          for (File file : libJarsToUnpack) {
            expandedSomething |= expandOneFile(file, true);
          }

          for (File file : jarsToExpand) {
            expandedSomething |= expandOneFile(file, false);
          }

          if (libJarsToUnpack.size() > 0 && expandedSomething) {
            myJwsdpPath = null;//LibUtils.getExtractedResourcesWebServicesDir() + File.separatorChar + jaxwsDir[0].getName();
            myXmlBeansPath = LibUtils.getExtractedResourcesWebServicesDir() + File.separatorChar + xmlbeansDir[0].getName();
          }
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  private static boolean expandOneFile(File file, boolean leaveJar) throws IOException {
    final String name = file.getName();
    File parentFile = file.getParentFile();
    final String baseOutputPath = LibUtils.getExtractedResourcesWebServicesDir() + File.separatorChar + parentFile.getName();
    new File(baseOutputPath).mkdirs();
    
    final String outputFilePath = baseOutputPath + File.separatorChar + name.substring(0, name.length() - PACK_GZ_SUFFIX.length());

    if (new File(outputFilePath).exists() && !getInstance().isLibChanged(file)) return false;

    parentFile.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        if (pathname.getName().endsWith(".LICENSE")) {
          File outLicenseFile = new File(baseOutputPath, pathname.getName());
          if (!outLicenseFile.exists()) {
            try {
              FileUtil.copy(pathname, outLicenseFile);
            } catch (IOException e) {}
          }
        }
        return false;
      }
    });
    
    final OutputStream out = new BufferedOutputStream(
      new FileOutputStream(outputFilePath)
    );

    final JarOutputStream jarOutputStream = new JarOutputStream(out);
    final GZIPInputStream inputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
    try {
      Pack200.newUnpacker().unpack(inputStream, jarOutputStream);
    } finally {
      inputStream.close();
      jarOutputStream.close();
    }

    if (!leaveJar) {
      final JarInputStream jarInput = new JarInputStream(new BufferedInputStream(new FileInputStream(outputFilePath)));
      JarEntry entry;

      try {
        final byte[] buf = new byte[8192];

        while ((entry = jarInput.getNextJarEntry()) != null) {
          final String outputFileName = baseOutputPath + File.separatorChar + entry.getName();
          File outFile = new File(outputFileName);

          if (entry.isDirectory()) {
            outFile.mkdirs();
          } else {
            outFile.getParentFile().mkdirs();
            final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outFile));
            try {
              while (true) {
                int read = jarInput.read(buf);
                if (read == -1) break;
                stream.write(buf, 0, read);
              }
            } finally {
              stream.close();
            }
          }
        }
      } finally {
        jarInput.close();
      }
    }
    
    return true;
  }

  public void disposeComponent() {
  }

  public List<String> getLastPackagePrefixes() {
    return lastPackagePrefixes;
  }

  public List<String> getLastWsdlUrls() {
    return lastWsdlUrls;
  }

  public List<String> getLastJAXBUrls() {
    return lastJaxbUrls;
  }

  public boolean isLibChanged(File lib) {
    final Long size = myPackGzLibs.get(lib.getPath());

    if (size == null) {
      myPackGzLibs.put(lib.getPath(), lib.length());
    }

    return size == null || size.longValue() != lib.length();
  }

  public List<String> getLastXmlBeansUrls() {
    return lastXmlBeansUrls;
  }

  public void addLastWsdlUrl(String url) {
    doAdd(lastWsdlUrls, url);
  }

  public void addLastJAXBUrl(String url) {
    doAdd(lastJaxbUrls, url);
  }

  public void addLastXmlBeansUrl(String url) {
    doAdd(lastXmlBeansUrls, url);
  }

  public void setLastRestClientHost(String host) {
    myLastRestClientHost = host;
  }

  public String getLastRestClientHost() {
    return myLastRestClientHost;
  }

  public boolean isAllowedToAskAboutSoapUI() {
    return Boolean.parseBoolean(myAskToInstallSoapUI);
  }

  public void setAllowedToAskInstallSoapUI(boolean allowed) {
    myAskToInstallSoapUI = Boolean.toString(allowed);
  }

  private static void doAdd(LinkedList<String> lastWsdlUrls, String url) {
    if (lastWsdlUrls.size() == 0 || !lastWsdlUrls.get(0).equals(url)) {
      lastWsdlUrls.add(0, url);
    }
    while (lastWsdlUrls.size() > MAX_HISTORY_ITEMS) {
      lastWsdlUrls.removeLast();
    }
  }

  public void addLastPackagePrefix(String packagePrefix) {
    if (lastPackagePrefixes.size() == 0 || !lastPackagePrefixes.get(0).equals(packagePrefix)) {
      lastPackagePrefixes.add(0, packagePrefix);
    }
  }

  public void readExternal(Element element) throws InvalidDataException {
    readUrls(element, GEN_JAXB_STUB_URL_KEY, lastJaxbUrls);
    readUrls(element, GEN_XMLBEANS_STUB_URL_KEY, lastXmlBeansUrls);
    readUrls(element, GEN_CLIENT_STUB_URL_KEY, lastWsdlUrls);
    readUrls(element, GEN_CLIENT_STUB_PREFIX_KEY, lastPackagePrefixes);

    String value = element.getAttributeValue(GENERATED_FILES_READ_WRITE_KEY);
    if (Boolean.TRUE.toString().equals(value)) makeGeneratedFilesReadOnly = false;

    value = element.getAttributeValue(SERVER_NAME_KEY);
    if (value != null) hostName = value;

    value = element.getAttributeValue(LAST_PLATFORM_KEY);
    if (value != null) {
      final WSEngine engine = getEngineManager().getWSEngineByName(value);
      myLastPlatform = engine != null ? engine.getName() : null;
    }

    value = element.getAttributeValue(LAST_BINDING_KEY);
    if (value != null) myLastBinding = value;

    value = element.getAttributeValue(WEB_SERVICES_URL_PATH_PREFIX_KEY);
    if (value != null) myWebServicesUrlPathPrefix = value;

    value = element.getAttributeValue(SERVER_PORT_KEY);
    if (value != null) hostPort = value;

    value = element.getAttributeValue(JWSDP_PATH_KEY);
    if (value != null) myJwsdpPath = value;

    value = element.getAttributeValue(SWDP_PATH_KEY);
    if (value != null) mySunWebDevelopmentPackPath = value;

    value = element.getAttributeValue(XFIRE_PATH_KEY);
    if (value != null) myXFirePath = value;

    value = element.getAttributeValue(AXIS2_PATH_KEY);
    if (value != null) myAxis2Path = value;

    value = element.getAttributeValue(JBOSSWS_PATH_KEY);
    if (value != null) myJBossWSPath = value;

    value = element.getAttributeValue(MEMORY_SIZE_TO_LAUNCH_VM_KEY);
    if (value != null) {
      int size;
      try {
        size = Math.max(Integer.parseInt(value), DEFAULT_MEMORY_SIZE_TO_LAUNCH_VM);
      } catch (NumberFormatException ex) {
        size = DEFAULT_MEMORY_SIZE_TO_LAUNCH_VM;
      }

      myMemorySizeToLaunchVM = size;
    }

    value = element.getAttributeValue(WEBSPHERE_WS_PATH_KEY);
    if (value != null) myWebSphereWSPath = value;

    value = element.getAttributeValue(XMLBEANS_PATH_KEY);
    if (value != null) myXmlBeansPath = value;

    value = element.getAttributeValue(LAST_REST_CLIENT_HOST_KEY);
    if (value != null) myLastRestClientHost = value;

    value = element.getAttributeValue(ASK_TO_INSTALL_SOAPUI);
    if (value != null) {
      if (value.equals(Boolean.toString(true)) || value.equals(Boolean.toString(false))) {
        myAskToInstallSoapUI = value;
      }
    }

    final Element libs = element.getChild(LIBS);
    if (libs != null) {
      for (Object node : libs.getChildren(LIB)) {
        if (node instanceof Element) {
          Element lib = (Element)node;
          final String path = lib.getAttributeValue(LIB_PATH);
          Long size;
          try {
            size = Long.valueOf(lib.getAttributeValue(LIB_SIZE));
          } catch (NumberFormatException e) {
            size = null;
          }
          if (path != null && size != null) {
            myPackGzLibs.put(path, size);
          }
        }
      }
    }

  }

  private static void readUrls(Element element, String genJaxbStubUrlKey, LinkedList<String> lastJaxbUrls) {
    List children;
    children = element.getChildren(genJaxbStubUrlKey);

    for (Object aChildren : children) {
      Element o = (Element) aChildren;

      lastJaxbUrls.add(o.getText());
      if (lastJaxbUrls.size() == MAX_HISTORY_ITEMS) break;
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    saveUrls(lastPackagePrefixes, GEN_CLIENT_STUB_PREFIX_KEY, element);
    saveUrls(lastWsdlUrls, GEN_CLIENT_STUB_URL_KEY, element);
    saveUrls(lastJaxbUrls, GEN_JAXB_STUB_URL_KEY, element);
    saveUrls(lastXmlBeansUrls, GEN_XMLBEANS_STUB_URL_KEY, element);

    if (!makeGeneratedFilesReadOnly) {
      element.setAttribute(GENERATED_FILES_READ_WRITE_KEY, Boolean.TRUE.toString());
    }

    if (!DEFAULT_HOST_NAME.equals(hostName)) {
      element.setAttribute(SERVER_NAME_KEY, hostName);
    }

    if (myLastPlatform != null) {
      element.setAttribute(LAST_PLATFORM_KEY, myLastPlatform);
    }

    if (myLastBinding != null) {
      element.setAttribute(LAST_BINDING_KEY, myLastBinding);
    }

    if (myWebServicesUrlPathPrefix != null) {
      element.setAttribute(WEB_SERVICES_URL_PATH_PREFIX_KEY, myWebServicesUrlPathPrefix);
    }

    if (!DEFAULT_HOST_PORT.equals(hostPort)) {
      element.setAttribute(SERVER_PORT_KEY, hostPort);
    }

    if (myJwsdpPath != null) element.setAttribute(JWSDP_PATH_KEY, myJwsdpPath);
    if (mySunWebDevelopmentPackPath != null) element.setAttribute(SWDP_PATH_KEY, mySunWebDevelopmentPackPath);
    if (myXFirePath != null) element.setAttribute(XFIRE_PATH_KEY, myXFirePath);
    if (myAxis2Path != null) element.setAttribute(AXIS2_PATH_KEY, myAxis2Path);
    if (myJBossWSPath != null) element.setAttribute(JBOSSWS_PATH_KEY, myJBossWSPath);
    if (myMemorySizeToLaunchVM != DEFAULT_MEMORY_SIZE_TO_LAUNCH_VM) element.setAttribute(MEMORY_SIZE_TO_LAUNCH_VM_KEY, String.valueOf(myMemorySizeToLaunchVM));
    if (myWebSphereWSPath != null) element.setAttribute(WEBSPHERE_WS_PATH_KEY, myWebSphereWSPath);
    if (myXmlBeansPath != null) element.setAttribute(XMLBEANS_PATH_KEY, myXmlBeansPath);
    if (myLastRestClientHost != null) element.setAttribute(LAST_REST_CLIENT_HOST_KEY, myLastRestClientHost);
    if (myAskToInstallSoapUI != null) element.setAttribute(ASK_TO_INSTALL_SOAPUI, myAskToInstallSoapUI);

    Element libs = new Element(LIBS);
    for (String path : myPackGzLibs.keySet()) {
      final Element lib = new Element(LIB);
      lib.setAttribute(LIB_PATH, path);
      lib.setAttribute(LIB_SIZE, myPackGzLibs.get(path).toString());
      libs.addContent(lib);
    }
    element.addContent(libs);
  }

  private static void saveUrls(List<String> lastWsdlUrls, String genClientStubUrlKey, Element element) {
    for (String lastUrl : lastWsdlUrls) {
      Element child = new Element(genClientStubUrlKey);
      element.addContent(child);
      child.setText(lastUrl);
    }
  }

  public static WebServicesPluginSettings getInstance() {
    final Application application = ApplicationManager.getApplication();
    if (application == null) return null;
    WebServicesPluginSettings instance = application.getComponent(WebServicesPluginSettings.class);
    if (instance == null && myInstance != null && application.isUnitTestMode()) {
      instance = myInstance;
    }
    return instance;
  }

  public static void setInstance(WebServicesPluginSettings _instance) {
    assert ApplicationManager.getApplication().isUnitTestMode();
    myInstance = _instance;
  }

  @NonNls
  public String getDisplayName() {
    return "Web Services";
  }

  public Icon getIcon() {
    return IconLoader.getIcon("WebServicesSettings.png");
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return "reference.settings.ide.settings.webservices";
  }

  public boolean toMakeSelectedFilesReadOnly() {
    return makeGeneratedFilesReadOnly;
  }

  public void setToMakeSelectedFilesReadOnly(boolean value) {
    makeGeneratedFilesReadOnly = value;
  }

  public String getXmlBeansPath() {
    return myXmlBeansPath;
  }

  public String getAxis2Path() {
    return myAxis2Path;
  }

  public Class[] getInspectionClasses() {
    return getInspectionClassesStatic();
  }

  private static Class[] getInspectionClassesStatic() {
    return new Class[]{
      ValidExternallyBoundObjectInspection.class,
      EmptyWebServiceInspection.class,
      OneWayWebMethodInspection.class,
      NonJaxWsWebServicesInspection.class,
      WebMethodExposedImplicitlyInspection.class,
      PathAnnotationInspection.class,
      NoResourceMethodsFoundInspection.class,
      //UnreachableResourceMethodInspection.class,
      VoidMethodMarkedWithGETInspection.class,
      ValidMimeAnnotationInspection.class
    };
  }

  public static LocalInspectionTool[] getInspectons() {
    try {
      Class[] inspectionClasses = getInspectionClassesStatic();
      LocalInspectionTool[] result = new LocalInspectionTool[inspectionClasses.length];
      for (int i = 0; i < inspectionClasses.length; ++i) {
        result[i] = (LocalInspectionTool) inspectionClasses[i].newInstance();
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final Icon icon = IconLoader.findIcon("/javaee/WebService.png");
    FileTemplateGroupDescriptor root = new FileTemplateGroupDescriptor(WSBundle.message("webservices.file.templates.group.name"), icon);
    final Icon javaIcon = StdFileTypes.JAVA.getIcon();

    root.addTemplate(new FileTemplateDescriptor(JAXWS_WEBSERVICE_TEMPLATE_NAME, javaIcon));
    root.addTemplate(new FileTemplateDescriptor(POJO_WEBSERVICE_TEMPLATE_NAME, javaIcon));
    root.addTemplate(new FileTemplateDescriptor(J2EE1_4_WEBSERVICE_TEMPLATE_NAME, javaIcon));
    root.addTemplate(new FileTemplateDescriptor(J2EE1_4_WEBSERVICE_INTERFACE_TEMPLATE_NAME, javaIcon));
    root.addTemplate(new FileTemplateDescriptor(DEFAULT_WEBSERVICE_CLIENT_TEMPLATE_NAME, javaIcon));
    root.addTemplate(new FileTemplateDescriptor(RESTFUL_WEBSERVICE_TEMPLATE_NAME, javaIcon));
    return root;
  }
  
  public String getTemplateText(@NotNull String templateName, @NotNull Properties properties) {
    final FileTemplate fileTemplate = FileTemplateManager.getInstance().getJ2eeTemplate(templateName);
    assert fileTemplate != null;
    
    try {
      return fileTemplate.getText(properties);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    final FileType fileType = consumer.getStandardFileTypeByName("XML");
    assert fileType != null;
    consumer.consume(fileType, WSDD_FILE_EXTENSION + FileTypeConsumer.EXTENSION_DELIMITER + WADL_FILE_EXTENSION + FileTypeConsumer.EXTENSION_DELIMITER + XJB_FILE_EXTENSION);
  }

  public String getId() {
    return getHelpTopic();
  }

  public Runnable enableSearch(final String option) {
    return null;
  }

  static class MySettings {
    private JTextField serverName;
    private JTextField serverPort;
    private JPanel myPanel;
    private JLabel serverNameText;
    private JLabel serverPortText;
    private TextFieldWithBrowseButton pathToJWSDP;
    private JLabel pathToJWSDPText;
    private JLabel pathToXFireText;
    private TextFieldWithBrowseButton pathToXFire;

    private JLabel pathToXmlBeansText;
    private TextFieldWithBrowseButton pathToXmlBeans;

    private JLabel pathToAxis2Text;
    private TextFieldWithBrowseButton pathToAxis2;

    private JLabel pathToJBossWSText;
    private TextFieldWithBrowseButton pathToJBossWS;
    private TextFieldWithBrowseButton pathToSWDP;
    private JLabel pathToSWDPText;
    private JLabel pathToWebSphereText;
    private TextFieldWithBrowseButton pathToWebSphere;
    private JLabel webServiceUrlPathPrefixText;
    private JTextField webServiceUrlPathPrefix;
    private JLabel maxMemorySizeText;
    private JTextField maxMemorySize;

    MySettings(String webServicesPathPrefix, String hostName, String hostPort, String jwsdpPath, String xfirePath, String xmlBeansPath,
               String axis2Path, String jbossWSPath, String swdpPath, String webSpherePath, int maxMemorySizeValue) {
      webServiceUrlPathPrefixText.setLabelFor(webServiceUrlPathPrefix);
      webServiceUrlPathPrefixText.setDisplayedMnemonic('t');

      serverNameText.setLabelFor(serverName);
      serverNameText.setDisplayedMnemonic('n');

      serverPortText.setLabelFor(serverPort);
      serverPortText.setDisplayedMnemonic('p');

      pathToJWSDPText.setLabelFor(pathToJWSDP.getTextField());
      pathToJWSDPText.setDisplayedMnemonic('w');

      pathToAxis2Text.setLabelFor(pathToAxis2.getTextField());
      pathToAxis2Text.setDisplayedMnemonic('a');

      pathToXFireText.setLabelFor(pathToXFire.getTextField());
      pathToXFireText.setDisplayedMnemonic('x');

      pathToJBossWSText.setLabelFor(pathToJBossWS.getTextField());
      pathToJBossWSText.setDisplayedMnemonic('j');

      pathToXmlBeansText.setLabelFor(pathToXmlBeans.getTextField());
      pathToXmlBeansText.setDisplayedMnemonic('b');

      pathToSWDPText.setLabelFor(pathToSWDP.getTextField());
      pathToSWDPText.setDisplayedMnemonic('d');

      pathToWebSphereText.setLabelFor(pathToWebSphere.getTextField());
      pathToWebSphereText.setDisplayedMnemonic('h');

      maxMemorySizeText.setLabelFor(maxMemorySize);
      maxMemorySizeText.setDisplayedMnemonic('m');
      
      configureSelectPath(
        pathToJWSDP,
        WSBundle.message("choose.glassfish.or.jaxws.ri.or.jwsdp.directory.dialog.title"),
        new Processor<VirtualFile>() {
          public boolean process(VirtualFile t) {
            boolean standAloneJWSDP = t.findChild("jaxb") != null;

            if (!standAloneJWSDP) {
              final VirtualFile libFile = t.findChild("lib");
              if (libFile != null) {
                VirtualFile child = libFile.findChild(JWSDPWSEngine.GLASSFISH1_WEB_SERVICES_JAR_NAME);
                if (child == null) child = libFile.findChild(JWSDPWSEngine.GLASSFISH2_WEB_SERVICES_JAR_NAME);
                if (child == null) child = libFile.findChild(JWSDPWSEngine.JAXWS_RT_JAR_NAME);
                return child != null;
              }
            }
            return standAloneJWSDP;
          }
        }
      );

      configureSelectPath(
        pathToAxis2,
        WSBundle.message("choose.axis.2.directory.dialog.title"),
        new Processor<VirtualFile>() {
          public boolean process(VirtualFile t) {
            final VirtualFile libFile = t.findChild("lib");
            if (libFile == null || !libFile.isDirectory()) return false;

            for (VirtualFile f : libFile.getChildren()) {
              if (Axis2Utils.isAxis2JarFile(f.getName())) return true;
            }
            return false;
          }
        }
      );

      configureSelectPath(
        pathToXFire,
        WSBundle.message("choose.xfire.directory.dialog.title"),
        new Processor<VirtualFile>() {
          public boolean process(VirtualFile t) {
            return XFireUtils.isXFireOrCxfHome(t);
          }
        }
      );

      configureSelectPath(
        pathToXmlBeans,
        WSBundle.message("choose.xml.beans.directory.dialog.title"),
        new Processor<VirtualFile>() {
          public boolean process(VirtualFile t) {
            final VirtualFile child = t.findChild("lib");
            return child != null && child.findChild("xbean.jar") != null;
          }
        }
      );

      configureSelectPath(
        pathToJBossWS,
        WSBundle.message("choose.jboss.application.server.directory.with.jboss.ws.installed.dialog.title"),
        new Processor<VirtualFile>() {
          public boolean process(VirtualFile t) {
            final VirtualFile libFile = t.findChild("client");
            if (libFile == null || !libFile.isDirectory()) return false;

            return libFile.findChild("jbossws-client.jar") != null || libFile.findChild("jbossws-native-client.jar") != null;
          }
        }
      );

      configureSelectPath(
        pathToSWDP,
        WSBundle.message("choose.sun.web.development.pack.directory.dialog.title"),
        new Processor<VirtualFile>() {
          public boolean process(VirtualFile dir) {
            return containsEngineLibs(dir, RestWSEngine.NAME);
          }
        }
      );

      configureSelectPath(
        pathToWebSphere,
        WSBundle.message("choose.web.sphere.path.dialog.title"),
        new Processor<VirtualFile>() {
          public boolean process(VirtualFile t) {
            return t.findChild("runtimes") != null && t.findChild("bin") != null;
          }
        }
      );

      reset(webServicesPathPrefix, hostName, hostPort, jwsdpPath, xfirePath, xmlBeansPath, axis2Path, jbossWSPath, swdpPath,
        webSpherePath, maxMemorySizeValue);
    }

    private static void configureSelectPath(final TextFieldWithBrowseButton pathToJWSDP, final String title,
                                            final Processor<VirtualFile> acceptanceProcessor) {
      pathToJWSDP.getButton().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false) {
            public boolean isFileSelectable(VirtualFile virtualFile) {
              return acceptanceProcessor.process(virtualFile);
            }
          };

          fileChooserDescriptor.setTitle(title);

          Window mostRecentFocusedWindow = WindowManagerEx.getInstanceEx().getMostRecentFocusedWindow();
          Project project = null;
          while (mostRecentFocusedWindow != null) {
            if (mostRecentFocusedWindow instanceof DataProvider) {
              project = (Project) ((DataProvider) mostRecentFocusedWindow).getData(DataConstants.PROJECT);
              if (project != null) break;
            }
            mostRecentFocusedWindow = (Window) mostRecentFocusedWindow.getParent();
          }

          FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
            fileChooserDescriptor,
            WindowManagerEx.getInstanceEx().suggestParentWindow(project)
          );

          String lastPathToJWSDP = pathToJWSDP.getText();
          VirtualFile file = lastPathToJWSDP != null && lastPathToJWSDP.length() > 0 ? EnvironmentFacade.getInstance().findRelativeFile(lastPathToJWSDP, null) : null;

          final VirtualFile[] virtualFiles = fileChooser.choose(file, project);
          if (virtualFiles != null && virtualFiles.length == 1) {
            pathToJWSDP.setText(virtualFiles[0].getPresentableUrl());
          }
        }
      });
    }

    private void reset(String webServicesPathPrefix, String hostName, String hostPort, String jwsdpPath,
                       String xfirePath, String xmlBeansPath, String axis2Path, String jbossWSPath,
                       String swdpPath, String webSpherePath, int maxMemorySizeValue) {
      serverName.setText(hostName);
      serverPort.setText(hostPort);

      pathToJWSDP.setText(jwsdpPath != null ? jwsdpPath : "");
      pathToXFire.setText(xfirePath != null ? xfirePath : "");
      pathToXmlBeans.setText(xmlBeansPath != null ? xmlBeansPath : "");
      pathToAxis2.setText(axis2Path != null ? axis2Path : "");
      pathToJBossWS.setText(jbossWSPath != null ? jbossWSPath : "");
      pathToSWDP.setText(swdpPath != null ? swdpPath : "");

      pathToWebSphere.setText(webSpherePath != null ? webSpherePath : "");
      webServiceUrlPathPrefix.setText(webServiceUrlPathPrefix != null ? webServicesPathPrefix : "");

      maxMemorySize.setText(String.valueOf(maxMemorySizeValue));
    }
  }

  private MySettings settings;
  private String hostName = DEFAULT_HOST_NAME;
  private String hostPort = DEFAULT_HOST_PORT;

  public JComponent createComponent() {
    settings = new MySettings(myWebServicesUrlPathPrefix, hostName, hostPort, myJwsdpPath, myXFirePath, myXmlBeansPath, myAxis2Path,
      myJBossWSPath, mySunWebDevelopmentPackPath, myWebSphereWSPath, myMemorySizeToLaunchVM);

    return settings.myPanel;
  }

  public boolean isModified() {
    return !hostName.equals(settings.serverName.getText()) ||
      !hostPort.equals(settings.serverPort.getText()) ||
      !myWebServicesUrlPathPrefix.equals(settings.webServiceUrlPathPrefix.getText()) ||

      checkValueChanged(myJwsdpPath, settings.pathToJWSDP) ||
      checkValueChanged(myXFirePath, settings.pathToXFire) ||
      checkValueChanged(myXmlBeansPath, settings.pathToXmlBeans) ||
      checkValueChanged(myAxis2Path, settings.pathToAxis2) ||
      checkValueChanged(myJBossWSPath, settings.pathToJBossWS) ||
      checkValueChanged(myWebSphereWSPath, settings.pathToWebSphere) ||
      checkValueChanged(mySunWebDevelopmentPackPath, settings.pathToSWDP) ||
      myMemorySizeToLaunchVM != parseIntNoException(settings.maxMemorySize.getText())
      ;
  }

  private static boolean checkValueChanged(String path, TextFieldWithBrowseButton pathUIField) {
    return (path != null && !path.equals(pathUIField.getText())) ||
      (path == null && pathUIField.getText().length() > 0);
  }

  public void apply() throws ConfigurationException {
    try {
      String text = settings.serverPort.getText();
      Integer.parseInt(text);
      hostPort = text;
    } catch (NumberFormatException ex) {
      throw new ConfigurationException("Port should be numeric");
    }

    try {
      String text = settings.serverName.getText();
      new URL("http://" + text);
      hostName = text;
    } catch (MalformedURLException ex) {
      throw new ConfigurationException("Invalid url");
    }

    String text = settings.pathToJWSDP.getText().trim();
    myJwsdpPath = text.length() > 0 ? text : null;

    text = settings.pathToXFire.getText().trim();
    myXFirePath = text.length() > 0 ? text : null;

    text = settings.pathToXmlBeans.getText().trim();
    myXmlBeansPath = text.length() > 0 ? text : null;

    text = settings.pathToAxis2.getText().trim();
    myAxis2Path = text.length() > 0 ? text : null;

    //String previousJBossWSPath = myJBossWSPath;
    text = settings.pathToJBossWS.getText().trim();
    myJBossWSPath = text.length() > 0 ? text : null;

    text = settings.pathToSWDP.getText().trim();
    mySunWebDevelopmentPackPath = text.length() > 0 ? text : null;

    text = settings.pathToWebSphere.getText().trim();
    myWebSphereWSPath = text.length() > 0 ? text : null;

    text = settings.webServiceUrlPathPrefix.getText().trim();

    if (text.endsWith("/")) text = text.substring(0, text.length() - 1);
    if (text.length() > 0 && !text.startsWith("/")) text = "/" + text;

    myWebServicesUrlPathPrefix = text;
    myMemorySizeToLaunchVM = parseIntNoException(settings.maxMemorySize.getText());

//    if (previousJBossWSPath != null && myJBossWSPath == null) {
//      ExternalResourceManagerEx.getInstanceEx().addResource(
//        "http://www.jboss.org/jbossws-tools",
//        null
//      );
//    } else if (myJBossWSPath != null && previousJBossWSPath == null) {
//      String basePath = myJBossWSPath + File.separatorChar + "schemas" + File.separatorChar;
//      ExternalResourceManagerEx.getInstanceEx().addResource(
//        "http://www.jboss.org/jbossws-tools",
//        basePath + "jbossws-tool_1_0.xsd"
//      );
//    }
  }

  private static int parseIntNoException(String text) {
    try {
      return Integer.parseInt(text.trim());
    } catch (NumberFormatException ex) {
      return MAX_HISTORY_ITEMS;
    }
  }

  public void reset() {
    settings.reset(myWebServicesUrlPathPrefix, hostName, hostPort, myJwsdpPath, myXFirePath, myXmlBeansPath, myAxis2Path, myJBossWSPath,
      mySunWebDevelopmentPackPath, myWebSphereWSPath, myMemorySizeToLaunchVM);
  }

  public void disposeUIResources() {
    settings = null;
  }

  public String getHostName() {
    return hostName;
  }

  public String getHostPort() {
    return hostPort;
  }

  public String getJwsdpPath() {
    return myJwsdpPath;
  }

  public String getXFirePath() {
    return myXFirePath;
  }

  public String getLastPlatform() {
    return myLastPlatform;
  }

  public void setLastPlatform(String myLastPlatform) {
    this.myLastPlatform = myLastPlatform;
  }

  public String getLastBinding() {
    return myLastBinding;
  }

  public void setLastBinding(String myLastBinding) {
    this.myLastBinding = myLastBinding;
  }

  public WSEngineManager getEngineManager() {
    return myEngineManager;
  }

  public String getJBossWSPath() {
    return myJBossWSPath;
  }

  public String getSunWebDevelopmentPackPath() {
    return mySunWebDevelopmentPackPath;
  }

  public String getWebSphereWSPath() {
    return myWebSphereWSPath;
  }

  public String getWebServicesUrlPathPrefix() {
    return myWebServicesUrlPathPrefix;
  }

  public int getMemorySizeToLaunchVM() {
    return myMemorySizeToLaunchVM;
  }

  private static boolean containsEngineLibs(VirtualFile dir, String engineName) {
    if (!dir.isDirectory()) return false;
    String[] jars = WebServicesClientLibraries.getJarShortNames(engineName);
    if (jars == null) return false;
    for (String jar : jars) {
      if (dir.findChild(jar) == null) return false;
    }
    return true;
  }
}
