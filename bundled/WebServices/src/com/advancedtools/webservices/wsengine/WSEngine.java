package com.advancedtools.webservices.wsengine;

import com.advancedtools.webservices.utils.ExternalProcessHandler;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @by maxim
 */
public interface WSEngine extends ExternalEngine {
  boolean hasSeparateClientServerJavaCodeGenerationOption();
  boolean allowsTestCaseGeneration();
  @Nullable String[] getSupportedMappingTypesForJavaFromWsdl();

  @NonNls String getDeploymentServletName();
  void doAdditionalWSServerSetup(Module currentModule);

  String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz);

  String checkNotAcceptableClassForDeployment(PsiClass clazz);

  boolean supportsJaxWs2();

  boolean deploymentSupported();

  interface DeployWebServiceOptions {
    String getWsName();
    String getWsClassName();
    String getWsNamespace();
    String getUseOfItems();
    String getBindingStyle();
    PsiClass getWsClass();
  }

  // TODO: make single deployment options interface
  interface GenerateWsdlFromJavaOptions {
    PsiClass getClassForOperation();
    String getTypeMappingVersion();
    String getSoapAction();
    String getBindingStyle();
    String getUseOfItems();
    String getGenerationType();
    String getMethods();

    Module getModule();
    String getWebServiceNamespace();
    String getWebServiceURL();
    String[] getClassPathEntries();
    Function<Void,Boolean> isParametersStillValidPredicate();
    Runnable getSuccessRunnable(Function<File, Void> successAction,File file);
  }

  void generateWsdlFromJava(GenerateWsdlFromJavaOptions options, Function<File, Void> onSuccessAction, Function<Exception, Void> onException, Runnable editAgain);

  void deployWebService(DeployWebServiceOptions createOptions, Module module, @NotNull Runnable onSuccessAction,
                        @NotNull Function<Exception, Void> onExceptionAction, @Nullable Runnable restartAction, Function<Void, Boolean> canRestartPredicate);
  void undeployWebService(String webServiceName, Module module, @NotNull Runnable onSuccessAction,
                          @NotNull Function<Exception, Void> onExceptionAction, @Nullable Runnable restartAction, Function<Void, Boolean> canRestartPredicate);

  String[] getAvailableWebServices(Module module);
  String[] getWebServicesOperations(String webServiceName, Module module);

  interface GenerateJavaFromWsdlOptions {
    LibraryDescriptorContext getToolRunningContext();
    String getPackagePrefix();
    String getWsdlUrl();
    String getOutputPath();

    String getBindingType();
    boolean isServersideSkeletonGeneration();
    boolean isToGenerateTestCase();

    String getUser();
    char[] getPassword();

    boolean generateClassesForArrays();
    String getTypeVersion();
    boolean isGenerateAllElements();
    boolean isSupportWrappedStyleOperation();

    Project getProject();
    File getSavedWsdlFile();
    Module getSelectedModule();

    boolean useExtensions();
  }

  ExternalProcessHandler getGenerateJavaFromWsdlHandler(GenerateJavaFromWsdlOptions options) throws InvokeExternalCodeUtil.ExternalCodeException;

  @NonNls
  String WS_DOCUMENT_STYLE = "DOCUMENT";
  
  @NonNls
  String WS_RPC_STYLE = "RPC";

  @NonNls
  String WS_WRAPPED_STYLE = "WRAPPED";

  @NonNls
  String WS_USE_LITERAL = "LITERAL";

  @NonNls
  String WS_USE_ENCODED = "ENCODED";
}
