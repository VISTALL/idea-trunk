package com.advancedtools.webservices.jaxrpc;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisUtil;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.utils.*;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @by Maxim
 */
public class JaxRPCWSEngine implements WSEngine {
  public static final String JAX_RPC = "JAX_RPC";
  private final String[] pathComponents = new String[] {"WEB-INF", WebServicesPluginSettings.JAXRPC_RI_RUNTIME_XML};

  public boolean hasSeparateClientServerJavaCodeGenerationOption() {
    return true;
  }

  public boolean allowsTestCaseGeneration() {
    return false;
  }

  @Nullable
  public String[] getSupportedMappingTypesForJavaFromWsdl() {
    return new String[0];
  }

  public String getDeploymentServletName() {
    return "JAXRPCServlet";
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
    DeployUtils.addFileToModuleFromTemplate(
      currentModule,
      pathComponents,
      WebServicesPluginSettings.JAXRPC_RI_RUNTIME_XML,
      true
    );
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    if (clazz.isInterface() || findWSInterface(clazz) == null) {
      return "Select WebService implementation class that implements WebService interface";
    }
    return null;
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    return null;
  }

  public void generateWsdlFromJava(final GenerateWsdlFromJavaOptions options, final Function<File, Void> onSuccessAction, final Function<Exception, Void> onException, Runnable editAgain) {
    final PsiClass psiClass = options.getClassForOperation();
    final PsiFile file = psiClass.getContainingFile();

    try {
      File tempDir = FileUtils.createTempDir("jaxrpcgen");

      PsiClass remoteInterface = findWSInterface(psiClass);

      @NonNls String serviceFileContent = "<service name=\"" + psiClass.getName() + "\"  \n" +
        "targetNamespace=\"" + options.getWebServiceNamespace() + "\"  \n" +
        "      typeNamespace=\"" + options.getWebServiceNamespace() + "\"  \n" +
        "      packageName=\"" + EnvironmentFacade.getInstance().getPackageFor(file.getContainingDirectory()).getQualifiedName() +"\">  \n" +
        "    <interface name=\"" + remoteInterface.getQualifiedName() + "\"  \n" +
        "          servantName=\"" + psiClass.getQualifiedName() + "\"/>  \n" +
        "</service>\n";
      String additionalOptions = "    \t       server=\"true\"\n" + buildWsStyleAndUseInBindings(
        options.getBindingStyle(),
        options.getUseOfItems()
      );
      String resultsDir = InvokeExternalCodeUtil.toAntPath(tempDir.getPath());

      ExternalProcessHandler externalProcessHandler = createWsCompileHandler(
        serviceFileContent,
        additionalOptions,
        resultsDir,
        options.getClassPathEntries(),
        new LibraryDescriptorContext() {
          public boolean isForRunningGeneratedCode() // false -> to run engine
          {
            return false;
          }

          public String getBindingType() {
            return null;
          }

          public Module getTargetModule() {
            return options.getModule();
          }
        }
      );

      final File wsdlFile = new File(tempDir, psiClass.getName() + ".wsdl");

      InvokeExternalCodeUtil.invokeExternalProcess2(
        externalProcessHandler,
        options.getModule().getProject(),
        options.getSuccessRunnable(new Function<File, Void>() {
          public Void fun(File s) {
            if (s.exists()) {
              try {
                final File f = new File(file.getContainingDirectory().getVirtualFile().getPath(), psiClass.getName() + ".wsdl");
                FileUtils.copyWsdlWithReplacementOfSoapAddress(wsdlFile, f, options.getWebServiceURL());
                onSuccessAction.fun(f);
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

  private ExternalProcessHandler createWsCompileHandler(String configurationText, String additionalAntTaskOptions,
                                                        String resultsDir, String[] classPathEntries, LibraryDescriptorContext libraryDescriptorContext) throws IOException {
    OutputStream out;
    File configFile = File.createTempFile("jaxrpc",".xml");
    configFile.deleteOnExit();
    out = new BufferedOutputStream( new FileOutputStream(configFile) );
    final @NonNls StringBuffer configText = new StringBuffer();
    configText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<configuration \n" +
      "    xmlns=\"http://java.sun.com/xml/ns/jax-rpc/ri/config\">\n" +
      configurationText +
      "</configuration>");
    out.write(configText.toString().getBytes());
    out.close();

    final LibraryDescriptor[] libraryDescriptors = JWSDPWSEngine.getLibInfosIfGlassFishOrMetroInstall(getBasePath(), libraryDescriptorContext);

    final @NonNls StringBuilder myBuffer = new StringBuilder();
    myBuffer.append("<project default=\"dowsgen\">\n").
      append("<property name=\"jwsdp.home\" value=\"").append(InvokeExternalCodeUtil.toAntPath(getBasePath())).append("\"/>\n" +
      "<path id=\"build.classpath\">\n");

    if (libraryDescriptors == null) {
      myBuffer.append("    <fileset dir=\"${jwsdp.home}/jaxrpc/lib\"/>\n").
      append("    <fileset dir=\"${jwsdp.home}/jwsdp-shared/lib\">\n").
      append("      <include name=\"activation.jar\"/>\n").
      append("      <include name=\"mail.jar\"/> \n").
      append("      <include name=\"jax-qname.jar\"/>\n").
      append("    </fileset>\n").
      append("    <fileset dir=\"${jwsdp.home}/saaj/lib\"/>\n").
      append("    <fileset dir=\"${jwsdp.home}/jaxp/lib\"/>\n");
    } else {
      myBuffer.append("    <fileset dir=\"${jwsdp.home}/lib\">\n").
      append("      <include name=\"webservices-rt.jar\"/>\n").
      append("      <include name=\"webservices-tools.jar\"/>\n").
      append("      <include name=\"appserv-ws.jar\"/>\n").
      append("      <include name=\"javaee.jar\"/>\n").
      append("    </fileset>\n");
    }

    myBuffer.append("  </path>\n").
      append("<taskdef name=\"wscompile\" classname=\"com.sun.xml.rpc.tools.ant.Wscompile\">\n").
      append("    <classpath refid=\"build.classpath\"/>\n").
      append("  </taskdef>\n").
      append("<target name=\"dowsgen\">\n").
      append("<wscompile keep=\"true\"\n");

    if (additionalAntTaskOptions != null) myBuffer.append(additionalAntTaskOptions);

    myBuffer.append("\t       base=\"").append(resultsDir).append("\"\n").
    //append("               httpproxy=\"${proxy.host}:${proxy.port}\"\n").
    append("\t       config=\"").append(InvokeExternalCodeUtil.toAntPath(configFile.getPath())).append("\">\n").
    append("\t       <classpath refid=\"build.classpath\"/>\n").
    append("    </wscompile>\n").
    append("</target>").
    append("</project>");

    ExternalProcessHandler externalProcessHandler = new InvokeExternalCodeUtil.ANTExternalProcessHandler(
      "JAXRPC wscompile",
      myBuffer.toString(),
      classPathEntries,
      libraryDescriptorContext.getTargetModule()
    );

    externalProcessHandler.setOutputConsumer(new InvokeExternalCodeUtil.OutputConsumer() {
        public boolean handle(String output, String errOutput) throws InvokeExternalCodeUtil.ExternalCodeException {
          final String errorMarker = "error:";
          final int errorIndex = output.indexOf(errorMarker);
          if (errorIndex != -1) {
            throw new InvokeExternalCodeUtil.ExternalCodeException(output.substring(errorIndex + errorMarker.length()));
          }
          return true;
        }
      });
    return externalProcessHandler;
  }

  private static PsiClass findWSInterface(PsiClass psiClass) {
    PsiClass remoteInterface = null;
    final PsiClass rmiInterface = EnvironmentFacade.getInstance().findClass("java.rmi.Remote", psiClass.getProject(), null);
    if (rmiInterface == null) return null;

    for(PsiClass cls:psiClass.getSupers()) {
      if (cls.isInterface() && InheritanceUtil.isInheritor(cls, rmiInterface, true)) {
        remoteInterface = cls;
        break;
      }
    }
    return remoteInterface;
  }

  public void deployWebService(final DeployWebServiceOptions createOptions, final Module module, final Runnable onSuccessAction, final Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate){
    try {
      PsiClass clazz = createOptions.getWsClass();
      PsiFile file = clazz.getContainingFile();
      @NonNls String configText = "<service name=\"" + createOptions.getWsName() + "\"  \n" +
        "targetNamespace=\"" + createOptions.getWsNamespace() + "\"  \n" +
        "      typeNamespace=\"" + createOptions.getWsNamespace() + "\"  \n" +
        "      packageName=\"" + EnvironmentFacade.getInstance().getPackageFor(file.getContainingDirectory()).getQualifiedName() +"\">  \n" +
        "    <interface name=\"" + findWSInterface(clazz).getQualifiedName() + "\"  \n" +
        "          servantName=\"" + createOptions.getWsClassName() + "\"/>  \n" +
        "</service>\n";
      final String wsStyleOption = buildWsStyleAndUseInBindings(createOptions.getUseOfItems(), createOptions.getBindingStyle());

      final String sourceOutputDir = LibUtils.findOutputDir(module);
      
      @NonNls String additionalAntTaskOptions = "\tserver=\"true\"\n" +
                                        "\tsourceBase=\""+ sourceOutputDir +"\"\n" +
                                        wsStyleOption +
                                        "\tmapping=\""+ DeployUtils.determineWhereToPlaceTheFileUnderWebInf(module, createOptions.getWsName() + ".model.xml") +"\"\n"
        ;

      final String[] classPathForModule = InvokeExternalCodeUtil.buildClasspathStringsForModule(module);
      ExternalProcessHandler externalProcessHandler = createWsCompileHandler(
        configText,
        additionalAntTaskOptions,
        sourceOutputDir,
        classPathForModule,
        new LibraryDescriptorContext() {
          public boolean isForRunningGeneratedCode() // false -> to run engine
          {
            return true;
          }

          public String getBindingType() {
            return null;
          }

          public Module getTargetModule() {
            return module;
          }
        }
      );
      InvokeExternalCodeUtil.runViaConsole(
        externalProcessHandler,
        module.getProject(),
        new Runnable() {
          public void run() {
            try {
              continueDeployment(createOptions, module, sourceOutputDir);
              onSuccessAction.run();
            } catch (IOException ex) {
              onExceptionAction.fun(ex);
            }
          }
        },
        onExceptionAction,
        null,
        null
      );

    } catch (IOException e) {
      onExceptionAction.fun(e);
    }
  }

  private void continueDeployment(DeployWebServiceOptions createOptions, Module module, String sourceOutputDir) throws IOException {
    final PsiClass psiClass = findWSInterface(createOptions.getWsClass());

    String interfaceQName = psiClass.getQualifiedName();
    String interfaceName = psiClass.getName();

    String wsdlFileName = createOptions.getWsName() + ".wsdl";
    final File generatedFileName = new File(sourceOutputDir + "/" +wsdlFileName);
    FileUtils.copyWsdlWithReplacementOfSoapAddress(
        generatedFileName,
        new File(DeployUtils.determineWhereToPlaceTheFileUnderWebInf(module, wsdlFileName)),
      AxisUtil.getWebServiceUrlReference("", createOptions.getWsName())
    );

    FileUtil.asyncDelete(generatedFileName);

    DeployUtils.addToConfigFile(
        pathComponents,
        true,
        module,
        "<endpoint\n" +
        "    name='" + createOptions.getWsName() + "'\n" +
        "    interface='" + interfaceQName + "'\n" +
        "    implementation='" + createOptions.getWsClassName() + "'\n" +
        "    tie='"+interfaceQName+"_Tie'\n" +
        "    model='/WEB-INF/" + createOptions.getWsName() + ".model.xml'\n" +
        "    wsdl='/WEB-INF/" + createOptions.getWsName() + ".wsdl'\n" +
        "    service='{" + createOptions.getWsNamespace() +"}" + createOptions.getWsName() +"'\n" +
        "    port='{"+createOptions.getWsNamespace() + "}" + interfaceName + "'\n" +
        "    urlpattern='" + createUrlPatternForWebService(createOptions) + "'/>",
      this
    );
  }

  public static String createUrlPatternForWebService(DeployWebServiceOptions createOptions) {
    return EnvironmentFacade.escapeXmlString(WebServicesPluginSettings.getInstance().getWebServicesUrlPathPrefix() + "/" + createOptions.getWsName());
  }

  private static String buildWsStyleAndUseInBindings(String wsStyle, String wsUseStyle) {
    final String wsStyleOption =
      (wsStyle.equals(WS_DOCUMENT_STYLE) ? "documentliteral":
        wsStyle.equals(WS_RPC_STYLE) ? "rpcliteral":
          wsStyle.equals(WS_WRAPPED_STYLE) ? "documentliteral,wsi,unwrap":
          null
      );
    final String wsUseOption = wsUseStyle == null ? null:
      wsUseStyle.equals(WS_USE_ENCODED) ? null:
        wsStyle.equals(WS_RPC_STYLE) ? null:null;

    return wsStyleOption != null ? "\tfeatures=\""+wsStyleOption+"\"\n":"";
  }

  public void undeployWebService(final String webServiceName, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    DeployUtils.removeFromConfigFile(
      pathComponents,
      true,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag xmlTag) {
          return webServiceName.equals(xmlTag.getAttributeValue("name"));
        }
      }
    );

    onSuccessAction.run();
  }

  public String[] getAvailableWebServices(Module module) {
    final List<String> availableWS = new ArrayList<String>(1);

    DeployUtils.processTagsInConfigFile(
      pathComponents,
      true,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag xmlTag) {
          final String wsname = xmlTag.getAttributeValue("name");
          if (wsname != null) {
            availableWS.add(wsname);
          }
          return true;
        }
      }
    );

    return availableWS.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public String[] getWebServicesOperations(String webServiceName, Module module) {
    return new String[0];
  }

  public ExternalProcessHandler getGenerateJavaFromWsdlHandler(final GenerateJavaFromWsdlOptions options) throws InvokeExternalCodeUtil.ExternalCodeException {
    @NonNls String configText = "<wsdl \n" +
      "    location=\"" + options.getSavedWsdlFile().getPath() + "\" \n" +
      "    packageName=\"" + options.getPackagePrefix() + "\" /> \n" +
      "";
    @NonNls String additionalAntTaskOptions = (options.isServersideSkeletonGeneration() ? "\tserver=\"true\"":"\tclient=\"true\"\n") +
                                      "\tsourceBase=\"" + options.getOutputPath() +"\"\n";
    try {
      return createWsCompileHandler(
        configText,
        additionalAntTaskOptions,
        options.getOutputPath(),
        null,
        new LibraryDescriptorContext() {
          public boolean isForRunningGeneratedCode() // false -> to run engine
          {
            return false;
          }

          public String getBindingType() {
            return null;
          }

          public Module getTargetModule() {
            return options.getSelectedModule();
          }
        }
      );
    } catch(IOException e) {
      throw new InvokeExternalCodeUtil.ExternalCodeException(e);
    }
  }

  public String getName() {
    return JAX_RPC;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    final String basePath = getBasePath();
    if (basePath == null) return LibraryDescriptor.EMPTY_ARRAY;
    LibraryDescriptor[] glassFishLibs = JWSDPWSEngine.getLibInfosIfGlassFishOrMetroInstall(basePath, context);
    if (glassFishLibs != null) return glassFishLibs;

    return new LibraryDescriptor[] {
      new LibraryInfo(
        "JaxRPC",
        new String[] {
          "jaxrpc/lib/jaxrpc-impl.jar",
          "jaxrpc/lib/jaxrpc-api.jar",
          "jaxrpc/lib/jaxrpc-spi.jar",
          "saaj/lib" + File.separator + "saaj-api.jar",
          "saaj/lib" + File.separator + "saaj-impl.jar",
          "fastinfoset/lib" + File.separator + "FastInfoset.jar",
          "jwsdp-shared/lib" + File.separator + "mail.jar",
          "sjsxp/lib" + File.separator + "sjsxp.jar",
          "sjsxp/lib" + File.separator + "jsr173_api.jar",
        }
      ),
      new LibraryInfo(
        "Activation",
        new String[] {
          "jwsdp-shared/lib/activation.jar"
        }
      )
    };
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getJwsdpPath();
  }

  public boolean supportsJaxWs2() {
    return false;
  }

  public boolean deploymentSupported() {
    return true;
  }
}
