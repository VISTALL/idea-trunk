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

package com.advancedtools.webservices.jbossws;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.ExternalProcessHandler;
import com.advancedtools.webservices.utils.FileUtils;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Maxim
 */
public class JBossWSEngine implements WSEngine {
  private static final Logger LOG = Logger.getInstance("webservicesplugin.jbosswsengine");

  private static final String JBOSS_WS_NAME = "JBossWS";
  @NonNls private static final String JBOSS_WSTOOLS_MAIN = "org.jboss.ws.tools.WSTools";

  public boolean hasSeparateClientServerJavaCodeGenerationOption() {
    return false;
  }

  public boolean allowsTestCaseGeneration() {
    return false;
  }

  @Nullable
  public String[] getSupportedMappingTypesForJavaFromWsdl() {
    return new String[0];
  }

  public String getDeploymentServletName() {
    return null;
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    return clazz.isInterface() ? null:"JBossWS generates WSDL only from interface";
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    return clazz.isInterface() ? null:"Please, use WebService interface";
  }

  public void generateWsdlFromJava(GenerateWsdlFromJavaOptions options, final Function<File, Void> onSuccessAction, final Function<Exception, Void> onException, Runnable editAgain) {
    try {
      @NonNls StringBuffer configText = new StringBuffer();

      configText.append("<configuration xmlns=\"http://www.jboss.org/jbossws-tools\">\n");

      configText.append("<java-wsdl>\n");
      final PsiClass psiClass = options.getClassForOperation();
      configText.append("<service name=\"").append(psiClass.getName())
        .append("\" parameterStyle=\"").append(options.getUseOfItems().toLowerCase())
        .append("\" style=\"").append(options.getBindingStyle().toLowerCase())
        .append("\" endpoint=\"").append(psiClass.getQualifiedName()).append("\"/>\n").
        append("<namespaces target-namespace=\"").append(options.getWebServiceNamespace()).append("\" type-namespace=\"").append(options.getWebServiceNamespace()).append("\"/>\n");
      configText.append("</java-wsdl>\n");
      configText.append("</configuration>");

      File tempDir = FileUtils.createTempDir("jbosswsgen");

      final ExternalProcessHandler externalProcessHandler = buildWSToolsHandler(configText.toString(), options.getModule(),tempDir);
      final File wsdlFile = new File(tempDir, "wsdl" + File.separatorChar + psiClass.getName() + ".wsdl");

      InvokeExternalCodeUtil.invokeExternalProcess2(
        externalProcessHandler,
        options.getModule().getProject(),
        options.getSuccessRunnable(new Function<File, Void>() {
          public Void fun(File s) {
            if (s.exists()) {
              try {
                final File wsdlFile = FileUtils.saveStreamContentAsFile(
                  psiClass.getContainingFile().getContainingDirectory().getVirtualFile().getPath() + File.separatorChar + psiClass.getName() + ".wsdl",
                  new BufferedInputStream(new FileInputStream(s))
                );
                onSuccessAction.fun(wsdlFile);
              } catch (IOException e) {
                onException.fun(e);
              }
            }
            return null;
          }
        }, wsdlFile),
        onException,
        options.isParametersStillValidPredicate(),
        editAgain
      );
    } catch (IOException e) {
      onException.fun(e);
    }
  }

  public void deployWebService(DeployWebServiceOptions createOptions, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    @NonNls StringBuffer textBuffer = new StringBuffer();
    textBuffer.append("<configuration xmlns=\"http://www.jboss.org/jbossws-tools\">\n");
    textBuffer.append("<java-wsdl>\n");

    textBuffer.append("<service name=\"").append(createOptions.getWsName())
      .append("\" parameterStyle=\"").append(createOptions.getUseOfItems().toLowerCase())
      .append("\" style=\"").append(createOptions.getBindingStyle().toLowerCase())
      .append("\" endpoint=\"").append(createOptions.getWsClassName()).append("\"/>\n").
      append("<namespaces target-namespace=\"").append(createOptions.getWsNamespace()).append("\" type-namespace=\"").append(createOptions.getWsNamespace()).append("\"/>\n").
      append("<mapping file=\"jaxrpc.mapping.xml\"/>\n").
      append("<webservices servlet-link=\"").append(createOptions.getWsName()).append("\"/>");
    textBuffer.append("</java-wsdl>\n");
    textBuffer.append("</configuration>");

    final File outputPath = new File(LibUtils.findOutputDir(module));

    ExternalProcessHandler externalProcessHandler = buildWSToolsHandler(
      textBuffer.toString(),
      module,
      outputPath
    );

    @NonNls String[] fileNamesToMove = {"webservices.xml", "jaxrpc.mapping.xml", "wsdl/" + createOptions.getWsName() + ".wsdl"};
    // TODO: These files and possibly others should be moved under web root (WEB-INF)

    InvokeExternalCodeUtil.runViaConsole(externalProcessHandler, module.getProject(), onSuccessAction, onExceptionAction, null, null);
  }

  public void undeployWebService(String webServiceName, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    onSuccessAction.run();
  }

  public String[] getAvailableWebServices(Module module) {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String[] getWebServicesOperations(String webServiceName, Module module) {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public ExternalProcessHandler getGenerateJavaFromWsdlHandler(GenerateJavaFromWsdlOptions options) {
    @NonNls StringBuffer configText = new StringBuffer();

    String nsFromUrl = options.getWsdlUrl();
    if (nsFromUrl.endsWith("?wsdl")) {
      nsFromUrl = nsFromUrl.substring(0, nsFromUrl.length() - 5);
    }
    configText.append("<configuration xmlns=\"http://www.jboss.org/jbossws-tools\">\n");
    configText.append("<global><package-namespace package=\"").append(options.getPackagePrefix()).append("\" namespace=\"").append(nsFromUrl).append("\" /></global>\n");
    configText.append("<wsdl-java file=\"").append(options.getSavedWsdlFile().getPath()).append("\">\n");
    configText.append("<mapping file=\"").append(options.getPackagePrefix().replace('.','/')).append('/').append("jax-rpc-mapping.xml\" />\n").append("  </wsdl-java>\n").append("</configuration>");

    return buildWSToolsHandler(configText.toString(), options.getSelectedModule(),new File(options.getOutputPath()));
  }

  private ExternalProcessHandler buildWSToolsHandler(String configText, Module contextModule, File launchContext) {
    File tempFile;

    try {
      tempFile = File.createTempFile("jbossws","config.xml");
      tempFile.deleteOnExit();
      OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
      out.write(configText.getBytes());
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    final List<String> commandLineParameters = new LinkedList<String>();

    String classPath = InvokeExternalCodeUtil.buildClasspathForModule(contextModule);
    if (classPath.length() > 0) {
      commandLineParameters.add("-cp");
      commandLineParameters.add(classPath);
    }

    commandLineParameters.add("-config");
    commandLineParameters.add(tempFile.getPath());

    InvokeExternalCodeUtil.JavaExternalProcessHandler javaExternalProcessHandler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "JBossWS WSTools",
      JBOSS_WSTOOLS_MAIN,
      LibUtils.getLibUrlsForToolRunning(this, null),
      commandLineParameters.toArray(new String[commandLineParameters.size()]),
      null,
      false
    );
    javaExternalProcessHandler.addCommandLineProperty("java.endorsed.dirs",getBasePath() + File.separatorChar + "lib" + File.separatorChar + "endorsed");
    javaExternalProcessHandler.setLaunchDir(launchContext);
    return javaExternalProcessHandler;
  }

  public String getName() {
    return JBOSS_WS_NAME;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    @NonNls String client = "client";
    String baseDir = client + File.separatorChar;

    boolean jbossws5 =  new File(getBasePath(), client + "/jbossws-native-client.jar").exists();
    boolean jbossws2 = new File(getBasePath(), client + "/jbossws-framework.jar").exists() && !jbossws5;
    boolean jbossws1_2 = !new File(getBasePath(), client + "/jbossws14-client.jar").exists() && !jbossws2;

    if (jbossws5) return new LibraryDescriptor[] {
      new LibraryInfo(
        JBOSS_WS_NAME,
        new String[] {
          baseDir + "jboss-xml-binding.jar",
          baseDir + "activation.jar",
          baseDir + "javassist.jar",
                 
          baseDir + "jbossws-native-jaxrpc.jar",
          baseDir + "jbossws-native-jaxws.jar",
          baseDir + "jbossws-native-jaxws-ext.jar",
          baseDir + "jbossws-native-saaj.jar",
          baseDir + "jbossws-native-client.jar",
          baseDir + "wsdl4j.jar",

          baseDir + "jbossall-client.jar",

          baseDir + "log4j.jar",
          baseDir + "mail.jar",
        },
        true
      )
    };
    return new LibraryDescriptor[] {
      new LibraryInfo(
        JBOSS_WS_NAME,
        new String[] {
          baseDir + "jboss-xml-binding.jar",
          baseDir + "activation.jar",
          baseDir + "javassist.jar",
          
          // JBossWS 1.2 / 2.0
          baseDir + "jboss-jaxrpc.jar",
          baseDir + "jboss-jaxws.jar",
          baseDir + "jboss-saaj.jar",
          baseDir + "jbossws-client.jar",
          baseDir + "wsdl4j.jar",
          
          baseDir + "jbossall-client.jar",
          baseDir + "jbossretro-rt.jar",
          baseDir + "jboss-backport-concurrent.jar",
          baseDir + "jbossws-client.jar",
          baseDir + "jbossws14-client.jar",
          
          baseDir + "log4j.jar",
          baseDir + "mail.jar",
        },
        true
      )
    }
           ;
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getJBossWSPath();
  }

  public boolean supportsJaxWs2() {
    return false;
  }

  public boolean deploymentSupported() {
    return false;
  }
}
