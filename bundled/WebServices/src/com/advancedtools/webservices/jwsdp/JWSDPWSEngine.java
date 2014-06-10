package com.advancedtools.webservices.jwsdp;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.actions.WebServicePlatformUtils;
import com.advancedtools.webservices.jaxrpc.JaxRPCWSEngine;
import com.advancedtools.webservices.utils.*;
import com.advancedtools.webservices.wsengine.*;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author maxim
 */
public class JWSDPWSEngine implements WSEngine, ExternalEngineThatChangedTheName, ExternalEngineThatBundlesJEEJars {
  private static final Logger LOG = Logger.getInstance("webservicesplugin.jwsdpengine");

  private static final String JWSDP_RT_LIBRARY_NAME = "JWSDP Runtime";
  private static final String JWSDP_LIBRARY_NAME = "JWSDP";
  private static final String JAXWSRI_LIBRARY_NAME = "JAXWS RI";
  private static final String JAXWSRI_RT_LIBRARY_NAME = "JAXWS RI runtime";
  private static final String JAXWSRI_2_1_RT_LIBRARY_NAME = "JAXWS 2.1 RI runtime";
  private static final String JWSDP_IN_GLASS_FISH_LIBRARY_NAME = "Web Services Library in GlassFish";
  private static final String JWSDP_IN_GLASS_FISH2_LIBRARY_NAME = "Web Services Library in GlassFish v2";
  private static final String METRO_LIBRARY_NAME = "Web Services Library in Metro";

  public static final String JWSDP_PLATFORM = WSBundle.message("glassfish.jax.ws.2.x.ri.metro.1.x.jwsdp.2.0");
  private static final String OLD_JWSDP_PLATFORM = "Glassfish / JAXWS2.X RI / JWSDP 2.0";

  private final @NonNls String[] pathComponents = new String[] {"WEB-INF","sun-jaxws.xml"};
  private static final boolean CREATE_AT_CONTENT_ROOT = true;

  public static final @NonNls String wsWebMethod = "javax.jws.WebMethod";
  public static final @NonNls HashSet<String> wsClassesSet = new HashSet<String>(Arrays.asList("javax.jws.WebService"));
  public static final @NonNls HashSet<String> wsMethodsSet = new HashSet<String>(Arrays.asList(wsWebMethod));  
  public static final @NonNls HashSet<String> wsOneWayMethodSet = new HashSet<String>(Arrays.asList("javax.jws.Oneway"));

  private static final String SELECTED_CLASS_IS_NOT_MARKED_WITH_JAVAX_JWS_WEB_SERVICE_ANNOTATION = 
    WSBundle.message("selected.class.is.not.marked.with.javax.jws.webservice.annotation.validation.message");

  @NonNls
  public static final String GLASSFISH1_WEB_SERVICES_JAR_NAME = "appserv-ws.jar";
  @NonNls
  public static final String GLASSFISH2_WEB_SERVICES_JAR_NAME = "webservices-tools.jar";
  @NonNls
  public static final String JAXWS_RT_JAR_NAME = "jaxws-rt.jar";

  public String getName() {
    return JWSDP_PLATFORM;
  }

  public static LibraryDescriptor[] getLibInfosIfGlassFishOrMetroInstall(@NotNull String basePath,@NotNull LibraryDescriptorContext context) {
    File glassFishLib = new File(basePath + File.separatorChar + "lib" + File.separatorChar + GLASSFISH1_WEB_SERVICES_JAR_NAME);
    File glassFishLib2 = new File(basePath + File.separatorChar + "lib" + File.separatorChar + GLASSFISH2_WEB_SERVICES_JAR_NAME);

    if (glassFishLib.exists() && !glassFishLib2.exists()) {
      return new LibraryDescriptor[] {
        new LibraryInfo(
          JWSDP_IN_GLASS_FISH_LIBRARY_NAME,
          new String[] {
            "lib" + File.separatorChar + GLASSFISH1_WEB_SERVICES_JAR_NAME,
            "lib" + File.separatorChar + "javaee.jar"
          },
          false
        )
      };
    } else {
      if (glassFishLib2.exists()) {
        if (context.getTargetModule() != null) FileUtils.addClassAsCompilerResource(context.getTargetModule().getProject());

        @NonNls String[] glassfish2Libs = {
          "lib" + File.separatorChar + GLASSFISH2_WEB_SERVICES_JAR_NAME
        };

        if (context.isForRunningGeneratedCode()) {
          glassfish2Libs = ArrayUtil.append(glassfish2Libs, "lib" + File.separatorChar + "webservices-rt.jar");
        }
        String [] libs = glassfish2Libs;

        File apiLib = new File(basePath + File.separatorChar + "lib" + File.separatorChar + "webservices-api.jar");
        boolean metro = apiLib.exists();

        if (metro) {
          final @NonNls String[] metroLibs = {
            "lib" + File.separatorChar + "webservices-api.jar",
            "lib" + File.separatorChar + "webservices-extra-api.jar",
            "lib" + File.separatorChar + "webservices-extra.jar",
          };
          libs = ArrayUtil.mergeArrays(libs, metroLibs, String.class);
        } else {
          libs = ArrayUtil.append(libs, "lib" + File.separatorChar + "javaee.jar");
        }

        return new LibraryDescriptor[] {
          new LibraryInfo(
            metro ? METRO_LIBRARY_NAME : JWSDP_IN_GLASS_FISH2_LIBRARY_NAME,
            libs,
            false
          )
        };
      }
    }
    return null;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    //final String basePath = getBasePath();
    //FileUtils.addClassAsCompilerResource(context.getTargetModule().getProject()); // JAXWS RI / JEE5u1 / Glassfish v2 / Metro generate class files as resources
    //
    //if (basePath == null) return LibraryDescriptor.EMPTY_ARRAY;
    //LibraryDescriptor[] glassFishLibs = getLibInfosIfGlassFishOrMetroInstall(basePath, context);
    //if (glassFishLibs != null) return glassFishLibs;
    //
    //if (isStandAloneJwsdp2()) {
    //  final @NonNls String commonJWSDPPrefix = "jaxws/lib" + File.separator;
    //  final @NonNls String sharedJWSDPPrefix = "jwsdp-shared/lib" + File.separator;
    //
    //  if (context.isForRunningGeneratedCode()) {
    //    final ExternalEngine jaxbEngine = WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(JaxbMappingEngine.JAXB_2_ENGINE);
    //    final LibraryDescriptor[] jaxbLibInfos = jaxbEngine.getLibraryDescriptors(context);
    //
    //    LibraryDescriptor ourDescriptor = new LibraryInfo(
    //      JWSDP_RT_LIBRARY_NAME,
    //      new String[] {
    //        commonJWSDPPrefix + "jaxws-api.jar",
    //        commonJWSDPPrefix + "jsr181-api.jar",
    //        commonJWSDPPrefix + "jsr250-api.jar",
    //        commonJWSDPPrefix + "jaxws-rt.jar",
    //        sharedJWSDPPrefix + "resolver.jar",
    //        "saaj/lib" + File.separator + "saaj-api.jar",
    //        "saaj/lib" + File.separator + "saaj-impl.jar"
    //      }
    //    );
    //    return ArrayUtil.append(jaxbLibInfos, ourDescriptor);
    //  } else {
    //    return new LibraryDescriptor[] {
    //      new LibraryInfo(
    //        JWSDP_LIBRARY_NAME,
    //        new String[]{
    //          commonJWSDPPrefix + "jaxws-tools.jar",
    //          commonJWSDPPrefix + "jaxws-rt.jar",
    //          sharedJWSDPPrefix + "resolver.jar",
    //          sharedJWSDPPrefix + "relaxngDatatype.jar",
    //          "jaxb/lib" + File.separator + "jaxb-xjc.jar",
    //          "sjsxp/lib" + File.separator + "sjsxp.jar"
    //        }
    //      )
    //    };
    //  }
    //} else {   //JAXWS RI
    //  final String commonJWSDPPrefix = LibUtils.accessingLibraryJarsFromPluginBundledLibs(basePath) ? "":"lib" + File.separator;
    //
    //  final ExternalEngine jaxbEngine = WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(JaxbMappingEngine.JAXB_2_ENGINE);
    //  final LibraryDescriptor[] jaxbLibInfos = jaxbEngine.getLibraryDescriptors(context);
    //  final boolean isJaxWs2_1Ri = new File(basePath + File.separator + commonJWSDPPrefix + "streambuffer.jar").exists();
    //  LibraryInfo ourDescriptor;
    //
    //  if (context.isForRunningGeneratedCode()) {
        //ourDescriptor = new LibraryInfo(
        //  isJaxWs2_1Ri ? JAXWSRI_2_1_RT_LIBRARY_NAME : JAXWSRI_RT_LIBRARY_NAME,
        //  new String[] {
        //    commonJWSDPPrefix + "jaxws-api.jar",
        //    commonJWSDPPrefix + "jsr181-api.jar",
        //    commonJWSDPPrefix + "jsr250-api.jar",
        //    commonJWSDPPrefix + "jaxws-rt.jar",
        //    commonJWSDPPrefix + "resolver.jar",
        //    commonJWSDPPrefix + "saaj-api.jar",
        //    commonJWSDPPrefix + "saaj-impl.jar"
        //  }
        //);
        //
        //if (isJaxWs2_1Ri) {
        //  ourDescriptor.appendJars(
        //    new String[] {
        //      commonJWSDPPrefix + "http.jar",
        //      commonJWSDPPrefix + "streambuffer.jar",
        //      commonJWSDPPrefix + "stax-ex.jar",
        //    }
        //  );
        //}
      //} else {
        //ourDescriptor =
        //  new LibraryInfo(
        //    JAXWSRI_LIBRARY_NAME,
        //    new String[]{
        //      commonJWSDPPrefix + "jaxws-api.jar",
        //      commonJWSDPPrefix + "jaxws-tools.jar",
        //      commonJWSDPPrefix + "jaxws-rt.jar",
        //      commonJWSDPPrefix + "resolver.jar",
        //      commonJWSDPPrefix + "relaxngDatatype.jar",
        //      commonJWSDPPrefix + "jaxb-xjc.jar",
        //      commonJWSDPPrefix + "sjsxp.jar"
        //    });
      //}
      //
      //return ArrayUtil.append(jaxbLibInfos, ourDescriptor);
    //}
    return LibraryDescriptor.EMPTY_ARRAY;
  }

  private boolean isStandAloneJwsdp2() {
    return new File(getBasePath() + File.separator + "jaxws").exists();
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getJwsdpPath();
  }

  public boolean hasSeparateClientServerJavaCodeGenerationOption() {
    return false;
  }

  public boolean allowsTestCaseGeneration() {
    return false;
  }

  public String[] getSupportedMappingTypesForJavaFromWsdl() {
    return null;
  }

  public String getDeploymentServletName() {
    return "WSServlet";
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
    DeployUtils.addFileToModuleFromTemplate(
      currentModule,
      pathComponents,
      "sun-jaxws.xml",
      CREATE_AT_CONTENT_ROOT
    );
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    if (clazz.isInterface()) return WSBundle.message("jaxws.does.not.support.wsdl.generation.from.interface.validation.message");
    if (isClassInDefaultPackageWithNoTargetNs(clazz)) {
      return WSBundle.message("no.targetnamespace.is.specified.validation.message");
    }

    return checkProperlyAnnotated(clazz);
  }

  public static boolean isClassInDefaultPackageWithNoTargetNs(PsiClass clazz) {
    final String name = clazz.getName();

    if ( name != null && name.equals(clazz.getQualifiedName())) {
      PsiAnnotation annotation = AnnotationUtil.findAnnotation(clazz, wsClassesSet);
      
      if (annotation != null) {
        final PsiAnnotationMemberValue annotationMemberValue = annotation.findAttributeValue("targetNamespace");
        
        if (annotationMemberValue != null &&
            ( !(annotationMemberValue instanceof PsiLiteralExpression) ||
              StringUtil.stripQuotesAroundValue(annotationMemberValue.getText()).length() > 0
            )
           ) return false;
        return true;
      }
    }
    return false;
  }
  
  private String checkProperlyAnnotated(PsiClass clazz) {
    return AnnotationUtil.findAnnotation(clazz, JWSDPWSEngine.wsClassesSet) == null ?
      SELECTED_CLASS_IS_NOT_MARKED_WITH_JAVAX_JWS_WEB_SERVICE_ANNOTATION :
      null;
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    if (isClassInDefaultPackageWithNoTargetNs(clazz)) {
      return WSBundle.message("no.targetnamespace.is.specified.validation.message");
    }
    return checkProperlyAnnotated(clazz);
  }

  public void generateWsdlFromJava(final GenerateWsdlFromJavaOptions options, final Function<File, Void> onSuccessAction, final Function<Exception, Void> onException, Runnable editAgain) {
    final PsiClass psiClass = options.getClassForOperation();

    final String generateWsdlDir = psiClass.getContainingFile().getContainingDirectory().getVirtualFile().getPath();
    final File tempFile = new File(
      generateWsdlDir + "/" + (psiClass.getName() + ".wsdl")
    );

    try {
      File tempDir = FileUtils.createTempDir("jaxwsgen");

      final String wsQName = "{" + options.getWebServiceNamespace() + "}" + psiClass.getName();
      doGenerateServerDeploymentCode(
        "Generate Wsdl From Java",
        options.getModule(),
        options.getClassForOperation().getQualifiedName(),
        tempDir.getPath(),
        new String[] { "-wsdl", "-r", generateWsdlDir, "-servicename", wsQName, "-portname", wsQName},
        new Runnable() {
          public void run() {
            try {
              FileUtils.copyWsdlWithReplacementOfSoapAddress(tempFile,tempFile, options.getWebServiceURL());
              options.getSuccessRunnable(onSuccessAction, tempFile).run();
            } catch (IOException e) {
              onException.fun(e);
            }
          }
        },
        onException,
        options.isParametersStillValidPredicate(), 
        editAgain
      );
    } catch (IOException e) {
      onException.fun(e);
      LOG.error(e);
    }
  }

  public String[] getWebServicesOperations(String webServiceName, Module module) {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public void deployWebService(DeployWebServiceOptions createOptions, Module module, Runnable onSuccessAction, 
                               Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    DeployUtils.addToConfigFile(
      pathComponents,
      CREATE_AT_CONTENT_ROOT,
      module,
      "<endpoint\n" +
        "        name='" + createOptions.getWsName() + "'\n" +
        "        implementation='" + createOptions.getWsClassName() + "'\n" +
        "        url-pattern='" + JaxRPCWSEngine.createUrlPatternForWebService(createOptions) + "'/>",
      this
    );

    String generateSourcesDir = LibUtils.findOutputDir(module);
    doGenerateServerDeploymentCode(
      "Generate Deployment Code",
      module,
      createOptions.getWsClassName(),
      generateSourcesDir,
      null,
      onSuccessAction,
      onExceptionAction,
      canRestartPredicate,
      restartAction
    );
  }

  private void doGenerateServerDeploymentCode(String title, Module module, String wsclassname,
                                              String generateSourcesDir, @NonNls String[] additionalOptions,
                                              Runnable onSuccessAction, Function<Exception, Void> onException,
                                              Function<Void, Boolean> runAgainPredicate,
                                              Runnable editAgain
                                              ) {

    @NonNls List<String> parameters = new ArrayList<String>(1);

    if (additionalOptions != null) {
      for(String additionalOption:additionalOptions) parameters.add(additionalOption);
    }

    parameters.add( "-d" );
    parameters.add( generateSourcesDir );

    parameters.add("-classpath");
    parameters.add(InvokeExternalCodeUtil.buildClasspathForModule(module));

    parameters.add( wsclassname );

    final String basePath = getBasePath();
    final boolean runningOnJDK6 = basePath == null && !WebServicePlatformUtils.isOldWsGenAccessibleForModule(module);

    final InvokeExternalCodeUtil.JavaExternalProcessHandler externalProcessHandler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      title,
      runningOnJDK6 ? "com.sun.tools.internal.ws.WsGen":"com.sun.tools.ws.WsGen",
      LibUtils.getLibUrlsForToolRunning(this,module),
      parameters.toArray(new String[parameters.size()]),
      module,
      true
    );

    // Metro puts some strange logging when do invoke generate wsdl so we force it to get away
    try {
      final File tempFile = File.createTempFile("logging", ".config");
      FileUtils.saveStreamContentAsFile(tempFile.getPath(), new StringBufferInputStream(".level= WARNING"));
      externalProcessHandler.addCommandLineProperty("java.util.logging.config.file", tempFile.getPath());
    } catch (IOException e) {
      onException.fun(e);
      return;
    }

    InvokeExternalCodeUtil.addEndorsedJarDirectory(externalProcessHandler, this, module);
    externalProcessHandler.setOutputConsumer(new InvokeExternalCodeUtil.OutputConsumer() {
      public boolean handle(String output, String errOutput) throws InvokeExternalCodeUtil.ExternalCodeException {
        if (output.length() > 0) {
          throw new InvokeExternalCodeUtil.ExternalCodeException(errOutput.length() > 0 ? errOutput:output);
        }
        return true;
      }
    });

    InvokeExternalCodeUtil.runViaConsole(
      externalProcessHandler,
      module.getProject(),
      onSuccessAction,
      onException,
      runAgainPredicate,
      editAgain
    );
  }

  public void undeployWebService(final String webServiceName, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    DeployUtils.removeFromConfigFile(
      pathComponents,
      CREATE_AT_CONTENT_ROOT,
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
      CREATE_AT_CONTENT_ROOT,
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

  public static final InvokeExternalCodeUtil.OutputConsumer ERROR_CHECKER = new InvokeExternalCodeUtil.OutputConsumer() {
    public boolean handle(String output, String errOutput) throws InvokeExternalCodeUtil.ExternalCodeException {
      final String error = "error:";

      if (output.startsWith(error)) {
        throw new InvokeExternalCodeUtil.ExternalCodeException(output.substring(error.length()).trim());
      }
      final String error2 = "[ERROR]";
      for (String out : output.replace('\r', '\n').split("\n")) {
        if (out.startsWith(error2)) {
          throw new InvokeExternalCodeUtil.ExternalCodeException(out.substring(error2.length()).trim());
        }
      }
      return true;
    }
  };

  public ExternalProcessHandler getGenerateJavaFromWsdlHandler(GenerateJavaFromWsdlOptions options) {
    List<String> parameters = new ArrayList<String>(3);

    parameters.add("-p");
    parameters.add(options.getPackagePrefix());

    parameters.add("-d");
    parameters.add(options.getOutputPath());

    parameters.add("-s");
    parameters.add(options.getOutputPath());

    if (options.useExtensions()) {
      parameters.add("-extension");
    }

    parameters.add("-wsdllocation");
    parameters.add(options.getWsdlUrl());

    parameters.add(options.getWsdlUrl());

    final String basePath = getBasePath();

    final String wsImportClass = basePath != null || WebServicePlatformUtils.isOldWsGenAccessibleForModule(options.getSelectedModule())
                                 ? "com.sun.tools.ws.WsImport"
                                 : "com.sun.tools.internal.ws.WsImport";
    final InvokeExternalCodeUtil.JavaExternalProcessHandler handler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "WS Import",
      wsImportClass,
      LibUtils.getLibUrlsForToolRunning(this, options.getSelectedModule()),
      //LibUtils.getLibsUrlsFromLibInfos(
      //  getLibraryDescriptors(options.getToolRunningContext()),
      //  basePath
      //),
      parameters.toArray(new String[parameters.size()]),
      options.getSelectedModule(),
      true
    );

    InvokeExternalCodeUtil.addEndorsedJarDirectory(handler, this, options.getSelectedModule());

    handler.setOutputConsumer(ERROR_CHECKER);
    return handler;
  }

  public boolean supportsJaxWs2() {
    return getBasePath() == null || !isStandAloneJwsdp2();
  }

  public boolean deploymentSupported() {
    return true;
  }

  public boolean isYourOldName(@NotNull String name) {
    return OLD_JWSDP_PLATFORM.equals(name);
  }

  public String[] getJEEJarNames(@NotNull ExternalEngine.LibraryDescriptorContext context) {
    return getJaxWsJarsForOverriding(this, context);
  }

  private static final @NonNls String[] jdk16ApiFilesToOverride = { "webservices-api.jar", "jaxws-api.jar", "jaxb-api.jar" };
  
  public static String[] getJaxWsJarsForOverriding(@NotNull ExternalEngine engine, @NotNull LibraryDescriptorContext context) {
    final LibraryDescriptor[] libraryDescriptors = engine.getLibraryDescriptors(context);
    final List<String> result = new ArrayList<String>(jdk16ApiFilesToOverride.length);
    
    if (libraryDescriptors != null) {
      for(LibraryDescriptor descr:libraryDescriptors) {
        for(String jarName:descr.getLibJars()) {
          for(String suffix:jdk16ApiFilesToOverride) {
            if (jarName.endsWith(suffix)) result.add(jarName);
          }
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }
}
