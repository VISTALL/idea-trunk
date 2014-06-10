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

package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.LibUtils;
import com.intellij.facet.ui.libraries.LibraryInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class WebServicesClientLibraries {
  public static final @NonNls String JB_DWLD_HOST = "http://download.jetbrains.com"; 
  public static final @NonNls String WSC_LIB_HOST = JB_DWLD_HOST + "/idea/j2ee_libs/";

  // Platforms for WebClients
  public static final String JAXRS = RestWSEngine.NAME;
  public static final String JAXWS = JWSDPWSEngine.JWSDP_PLATFORM;
  public static final String AXIS = AxisWSEngine.AXIS_PLATFORM;

  public static final String[] SUPPORTED_PLATFORM = new String[] {JAXRS, JAXWS, AXIS};

  // Platforms' short names
  public static final @NonNls HashMap<String, String> PLATFORM_FOLDERS = new HashMap<String, String>();
  static {
    PLATFORM_FOLDERS.put(JAXRS, "jersey");
    PLATFORM_FOLDERS.put(JAXWS, "jaxws");
    PLATFORM_FOLDERS.put(AXIS,  "axis");
  }

  public static final HashMap<String, String> DEFAULT_VERSIONS = new HashMap<String, String>();
  static {
    DEFAULT_VERSIONS.put(JAXWS, "2.1");
    DEFAULT_VERSIONS.put(JAXRS, "1.0.3");
    DEFAULT_VERSIONS.put(AXIS, "1.4");
  }

  public static boolean isSupported(String platformName) {
    for (String platform : SUPPORTED_PLATFORM) {
      if (platform.equals(platformName)) return true;
    }
    return false;
  }

  private static final HashMap<String, List<LibraryInfoWrapper>> LIBS = new HashMap<String, List<LibraryInfoWrapper>>();

  public static LibraryInfo[] getNecessaryLibraries(@NotNull String platform, @NotNull String version) {
    List<LibraryInfoWrapper> libs = LIBS.get(platform + version);
    if (libs == null) return new LibraryInfo[0];
    LibraryInfo[] infos = new LibraryInfo[libs.size()];
    for (int i = 0; i < infos.length; i++) {
      infos[i] = libs.get(i).createLibraryinfo();
    }
    return infos;
  }

  public static LibraryInfo[] getNecessaryLibraries(@NotNull String platform) {
    return getNecessaryLibraries(platform, DEFAULT_VERSIONS.get(platform));
  }

  private static void addJar(@NotNull String platform, @NotNull String version, @NotNull @NonNls String jarname, @NonNls String... requiredClasses) {
    String key = key(platform, version);
    List<LibraryInfoWrapper> list = LIBS.get(key);
    if (list == null) {
      list = new ArrayList<LibraryInfoWrapper>();
      LIBS.put(key, list);
    }
    list.add(new LibraryInfoWrapper(platform, version, jarname, requiredClasses));
  }

  @NonNls
  private static String getDownloadPrefix(@NotNull String platform, @NotNull String version, @NotNull String jarfile) {
    String path = LibUtils.getExtractedResourcesWebServicesDir() + File.separator + PLATFORM_FOLDERS.get(platform) + File.separator + version;
    return (new File(path + File.separator + jarfile).exists()) ?
      "file://" + path + "/" : WSC_LIB_HOST + PLATFORM_FOLDERS.get(platform) + "/" + version + "/" ;
  }

  private static String key(@NotNull String platform, @NotNull String version) {
    return platform + version;
  }

  @Nullable
  public static String[] getJarShortNames(@NotNull String platform, @NotNull String version) {
    List<LibraryInfoWrapper> libs = LIBS.get(platform + version);

    if (libs == null || libs.size() == 0) return null;

    String[] jars = new String[libs.size()];
    for (int i = 0; i < jars.length; i++) {
      jars[i] = libs.get(i).myJarname;
    }
    return jars;
  }

  @Nullable
  public static String[] getJarShortNames(@NotNull String platform) {
    return getJarShortNames(platform, DEFAULT_VERSIONS.get(platform));
  }
 
  static {
    String version = "1.0.3";
    addJar(JAXRS, version, "activation-1.1.jar", "com.sun.activation.registries.LineTokenizer");
    addJar(JAXRS, version, "asm-3.1.jar", "org.objectweb.asm.AnnotationVisitor");
    addJar(JAXRS, version, "FastInfoset-1.2.2.jar", "com.sun.xml.fastinfoset.Encoder");
    addJar(JAXRS, version, "grizzly-servlet-webserver-1.8.6.4.jar", "com.sun.grizzly.Grizzly");
    addJar(JAXRS, version, "http-20070405.jar", "com.sun.net.httpserver.HttpServer");
    addJar(JAXRS, version, "jackson-lgpl-0.9.4.jar", "org.codehaus.jackson.JsonFactory");
    addJar(JAXRS, version, "jaxb-api-2.1.jar", "javax.xml.bind.JAXB");
    addJar(JAXRS, version, "jaxb-impl-2.1.10.jar", "com.sun.xml.txw2.Document");
    addJar(JAXRS, version, "jaxb-xjc.jar", "com.sun.org.apache.xml.internal.resolver.Resolver");
    addJar(JAXRS, version, "jdom-1.0.jar", "org.jdom.Document");
    addJar(JAXRS, version, "jersey-bundle-1.0.3.jar", "com.sun.jersey.server.impl.wadl.WadlFactory");
    addJar(JAXRS, version, "jettison-1.0.1.jar", "org.codehaus.jettison.XsonNamespaceContext");
    addJar(JAXRS, version, "jsr311-api-1.0.jar", "javax.ws.rs.core.Request");
    addJar(JAXRS, version, "mail-1.4.jar", "javax.mail.Message");
    addJar(JAXRS, version, "localizer.jar", "com.sun.localization.Localizer");    
    addJar(JAXRS, version, "persistence-api-1.0.2.jar", "javax.persistence.Entity");
    addJar(JAXRS, version, "rome-0.9.jar", "com.sun.syndication.feed.synd.SyndFeed");
    addJar(JAXRS, version, "stax-api-1.0-2.jar", "javax.xml.stream.FactoryFinder");
    addJar(JAXRS, version, "wadl-core.jar", "org.jvnet.ws.wadl2java.Wadl2Java");
    addJar(JAXRS, version, "wadl-cmdline.jar", "org.jvnet.ws.wadl2java.Main");
    addJar(JAXRS, version, "wadl2java.jar", "com.sun.research.ws.wadl2java.Main");    

    //Axis 1.4
    version = "1.4";
    addJar(AXIS, version, "axis.jar", "org.apache.axis.AxisEngine");
    addJar(AXIS, version, "commons-discovery-0.2.jar", "org.apache.commons.discovery.tools.Service");
    addJar(AXIS, version, "commons-logging-1.0.4.jar", "org.apache.commons.logging.Log");
    addJar(AXIS, version, "jaxrpc.jar", "javax.xml.rpc.ServiceFactory");
    addJar(AXIS, version, "log4j-1.2.8.jar", "org.apache.log4j.Logger");
    addJar(AXIS, version, "saaj.jar", "javax.xml.soap.MessageFactory");
    addJar(AXIS, version, "wsdl4j-1.5.1.jar", "javax.wsdl.factory.WSDLFactory");

    //JAXWS 2.1
    version = "2.1";
    addJar(JAXWS, version, "activation.jar", "javax.activation.ActivationDataFlavor");
    addJar(JAXWS, version, "FastInfoset.jar", "org.jvnet.fastinfoset.FastInfosetParser");
    addJar(JAXWS, version, "http.jar", "com.sun.net.httpserver.HttpServer");
    addJar(JAXWS, version, "jaxb-api.jar", "javax.xml.bind.JAXB");
    addJar(JAXWS, version, "jaxb-impl.jar", "com.sun.xml.bind.AnyTypeAdapter");
    addJar(JAXWS, version, "jaxb-xjc.jar", "com.sun.xml.dtdparser.DTDParser");
    addJar(JAXWS, version, "jaxws-api.jar", "javax.xml.ws.Service");
    addJar(JAXWS, version, "jaxws-rt.jar", "com.sun.xml.ws.api.SOAPVersion");
    addJar(JAXWS, version, "jaxws-tools.jar", "com.sun.tools.ws.WsGen");
    addJar(JAXWS, version, "jsr173_api.jar", "javax.xml.stream.FactoryFinder");
    addJar(JAXWS, version, "jsr181-api.jar", "javax.jws.WebService");
    addJar(JAXWS, version, "jsr250-api.jar", "javax.annotation.Resource");
    addJar(JAXWS, version, "resolver.jar", "com.sun.org.apache.xml.internal.resolver.Resolver");
    addJar(JAXWS, version, "saaj-api.jar", "javax.xml.soap.SOAPFactory");
    addJar(JAXWS, version, "saaj-impl.jar", "com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnection");
    addJar(JAXWS, version, "sjsxp.jar", "com.sun.xml.stream.Version");
    addJar(JAXWS, version, "stax-ex.jar", "org.jvnet.staxex.Base64Data");
    addJar(JAXWS, version, "streambuffer.jar", "com.sun.xml.stream.buffer.stax.StreamBufferCreator");
  }

  private WebServicesClientLibraries() {}

  private static class LibraryInfoWrapper {
    String myPlatform;
    String myVersion;
    String myJarname;
    String[] myRequiredClasses;

    LibraryInfoWrapper(@NotNull String platform, @NotNull String version, @NotNull @NonNls String jarname, @NonNls String... requiredClasses) {
      myPlatform = platform;
      myVersion = version;
      myJarname = jarname;
      myRequiredClasses = requiredClasses;
    }

    LibraryInfo createLibraryinfo() {
      return new LibraryInfo(myJarname,
                             myVersion,
                             getDownloadPrefix(myPlatform, myVersion, myJarname) + myJarname,
                             JB_DWLD_HOST,
                             myRequiredClasses);
    }
  }
}
