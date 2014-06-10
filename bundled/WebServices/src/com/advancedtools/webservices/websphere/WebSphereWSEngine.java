package com.advancedtools.webservices.websphere;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.ExternalProcessHandler;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @by Maxim
 */
public class WebSphereWSEngine implements WSEngine {
  public static final String WEBSPHERE_PLATFORM = "WebSphere 6.X";

  public boolean hasSeparateClientServerJavaCodeGenerationOption() {
    return true;
  }

  public boolean allowsTestCaseGeneration() {
    return true;
  }

  @Nullable
  public String[] getSupportedMappingTypesForJavaFromWsdl() {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getDeploymentServletName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void generateWsdlFromJava(GenerateWsdlFromJavaOptions options, Function<File, Void> onSuccessAction,
                                   Function<Exception, Void> onException, Runnable editAgain) {
    
    final PsiClass psiClass = options.getClassForOperation();

    final String generateWsdlDir = psiClass.getContainingFile().getContainingDirectory().getVirtualFile().getPath();
    final File tempFile = new File(
      generateWsdlDir + "/" + (psiClass.getName() + ".wsdl")
    );

    final List<String> params = new ArrayList<String>();
    params.add("-location");
    params.add(options.getWebServiceURL());
    
    params.add("-namespace");
    params.add(options.getWebServiceNamespace());

    params.add("-classpath");
    params.add(InvokeExternalCodeUtil.buildClasspathForModule(options.getModule()));

    params.add("-output");
    params.add(tempFile.getPath());

    params.add("-methods");
    params.add(options.getMethods());

    params.add("-style");
    params.add(options.getBindingStyle());

    params.add("-soapAction");
    params.add(options.getSoapAction());

    params.add("-use");
    params.add(options.getUseOfItems());

    params.add(psiClass.getQualifiedName());

    final InvokeExternalCodeUtil.BatchExternalProcessHandler batchExternalProcessHandler =
      new InvokeExternalCodeUtil.BatchExternalProcessHandler(WEBSPHERE_PLATFORM + " WSDL from Java", "Java2WSDL", params);
    batchExternalProcessHandler.setLaunchDir(new File(getBasePath(),"bin"));

    InvokeExternalCodeUtil.invokeExternalProcess2(
      batchExternalProcessHandler,
      options.getModule().getProject(),
      options.getSuccessRunnable(onSuccessAction, tempFile),
      onException,
      options.isParametersStillValidPredicate(),
      editAgain
    );
  }

  public void deployWebService(DeployWebServiceOptions createOptions, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    onSuccessAction.run();
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

  public ExternalProcessHandler getGenerateJavaFromWsdlHandler(GenerateJavaFromWsdlOptions options) throws InvokeExternalCodeUtil.ExternalCodeException {
    final List<String> params = new ArrayList<String>();

    params.add("-o");
    params.add(options.getOutputPath());

    // TODO: let the user choose particular options of client/server generation
    if (options.isServersideSkeletonGeneration()) {
      params.add("-role");
      params.add("server");
      params.add("-container");
      params.add("web");
    } else {
      params.add("-role");
      params.add("client");
    }

    params.add("-genJava");
    params.add("Overwrite");
    params.add("-genXML");
    params.add("Overwrite");

    if (options.isToGenerateTestCase()) params.add("-testCase");
    if (!options.isSupportWrappedStyleOperation()) params.add("-noWrappedOperations");
    if (options.generateClassesForArrays()) params.add("-noWrappedArrays");
    if (options.isGenerateAllElements()) params.add("-all");

    if (options.getUser().length() > 0) {
      params.add("-user");
      params.add(options.getUser());

      params.add("-password");
      params.add(new String(options.getPassword()));
    }

    params.add("-NStoPkg");

    File savedWsdlFile = options.getSavedWsdlFile();
    final String detectedNsFromWsdl = LibUtils.retrieveTargetNamespace(savedWsdlFile);

    String nsFromUrl = detectedNsFromWsdl != null ? detectedNsFromWsdl:options.getWsdlUrl();
    if (nsFromUrl.endsWith("?wsdl")) {
      nsFromUrl = nsFromUrl.substring(0, nsFromUrl.length() - 5);
    }

    params.add(nsFromUrl +"="+options.getPackagePrefix());

    params.add(options.getWsdlUrl());

    final InvokeExternalCodeUtil.BatchExternalProcessHandler batchExternalProcessHandler =
      new InvokeExternalCodeUtil.BatchExternalProcessHandler(WEBSPHERE_PLATFORM + " Java from WSDL", "WSDL2Java", params);
    batchExternalProcessHandler.setLaunchDir(new File(getBasePath(),"bin"));
    return batchExternalProcessHandler;
  }

  public String getName() {
    return WEBSPHERE_PLATFORM;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    if (context.isForRunningGeneratedCode()) {
      final String basePath = getBasePath();
      final String runtimesDir = "runtimes";
      File file = new File(basePath, runtimesDir);
      final File[] files = file.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".jar") && name.indexOf("webservices.thinclient") >= 0;
        }
      });

      if (files != null && files.length == 1) {
        return new LibraryDescriptor[] {
          new LibraryInfo("IBM WebServices Thin Client Library", runtimesDir + "/" + files[0].getName()),
          new LibraryInfo("Xerces", "java/jre/lib/xml.jar"),
          new LibraryInfo("J2EE", "lib/j2ee.jar"),
        };
      }
    }
    return new LibraryDescriptor[0];
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getWebSphereWSPath();
  }

  public boolean supportsJaxWs2() {
    return false;
  }

  public boolean deploymentSupported() {
    return false;
  }
}
