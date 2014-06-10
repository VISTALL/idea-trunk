package com.advancedtools.webservices.axis;

import com.advancedtools.webservices.utils.DeployUtils;
import com.advancedtools.webservices.utils.ExternalProcessHandler;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.containers.ArrayListSet;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.*;

/**
 * @author maxim
 */
public class AxisWSEngine implements WSEngine {
  private @NonNls final String[] pathComponents = new String[] {"WEB-INF", "server-config.wsdd"};
  private static final boolean CREATE_IN_CONTENT_ROOT = true;

  public static final String AXIS_PLATFORM = "Apache Axis";
  private static final @NonNls String SERVICE_TAG_NAME = "service";
  private @NonNls final Set<String> predefinedServices = new HashSet<String>(Arrays.asList("AdminService","Version", "SOAPMonitorService"));
  @NonNls private static final String WSDL2JAVA = "org.apache.axis.wsdl.WSDL2Java";

  public String getName() {
    return AXIS_PLATFORM;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    return new LibraryDescriptor[] {
      //new LibraryInfo( "Axis", "axis.jar" ),
      //new LibraryInfo( "Commons Discovery", "commons-discovery-0.2.jar" ),
      //new LibraryInfo( "Commons Logging", "commons-logging-1.0.4.jar" ),
      //new LibraryInfo( "JAX RPC", "jaxrpc.jar" ),
      //new LibraryInfo( "SAAJ", "saaj.jar" ),
      //new LibraryInfo( "Log4J", "log4j-1.2.8.jar" ),
      //new LibraryInfo( "WSDL4J", "wsdl4j-1.5.1.jar" )
    };
  }

  public String getBasePath() {
    return null;
    //return LibUtils.getExtractedResourcesWebServicesDir() + "/axis-1.4.0";
  }

  public boolean hasSeparateClientServerJavaCodeGenerationOption() {
    return true;
  }

  public boolean allowsTestCaseGeneration() {
    return true;
  }

  public String[] getSupportedMappingTypesForJavaFromWsdl() {
    return null;
  }

  public String getDeploymentServletName() {
    return "AxisServlet";
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
    DeployUtils.addFileToModuleFromTemplate(
      currentModule,
      pathComponents,
      "Axis.services.wsdd",
      CREATE_IN_CONTENT_ROOT
    );
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    return null;
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    return null;
  }

  public void generateWsdlFromJava(GenerateWsdlFromJavaOptions options, Function<File, Void> onSuccessAction, Function<Exception, Void> onException, Runnable editAgain) {
    final List<String> parameters = new LinkedList<String>();

    PsiClass psiClass = options.getClassForOperation();
    String wsdlFileName = psiClass.getName() + ".wsdl";
    final PsiFile file = psiClass.getContainingFile();

    final File tempFile = new File(
      file.getContainingDirectory().getVirtualFile().getPath() + "/" + wsdlFileName
    );

    parameters.add("-l" + options.getWebServiceURL());
    parameters.add("-n" + options.getWebServiceNamespace());

    parameters.add(
      "-o" + tempFile.getPath()
    );

    parameters.add("-A" + options.getSoapAction());
    parameters.add("-y" + options.getBindingStyle());
    parameters.add("-u" + options.getUseOfItems());
    parameters.add("-T" + options.getTypeMappingVersion());
    parameters.add("-w" + options.getGenerationType());
    String methods = options.getMethods();
    if (methods.length() > 0) parameters.add("-m" + methods);

    parameters.add(
      psiClass.getQualifiedName()
    );

    final String[] args = parameters.toArray(new String[parameters.size()]);

    final InvokeExternalCodeUtil.JavaExternalProcessHandler handler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "Java 2 WSDL",
      AxisUtil.ORG_APACHE_AXIS_WSDL_JAVA2_WSDL,
      LibUtils.getLibUrlsForToolRunning(this, options.getModule()),
      //options.getClassPathEntries(),
      args,
      options.getModule(),
      false
    );

    handler.setOutputConsumer(new InvokeExternalCodeUtil.OutputConsumer() {
      public boolean handle(String output, String errOutput) throws InvokeExternalCodeUtil.ExternalCodeException {
        if (errOutput.indexOf("error") != -1) throw new InvokeExternalCodeUtil.ExternalCodeException(errOutput);
        return true;
      }
    });

    InvokeExternalCodeUtil.invokeExternalProcess2(
      handler,
      options.getModule().getProject(),
      options.getSuccessRunnable(onSuccessAction, tempFile),
      onException,
      options.isParametersStillValidPredicate(),
      editAgain
    );
  }

  public String[] getWebServicesOperations(String webServiceName, Module module) {
    return new String[0];
  }

  public void deployWebService(DeployWebServiceOptions createOptions, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    PsiType wsPsiType = JavaPsiFacade.getInstance(module.getProject()).getElementFactory().createType(createOptions.getWsClass());
    PsiClass[] classes = DeployUtils.searchReferencedTypesForClass(wsPsiType);
    DeployUtils.addToConfigFile(
      pathComponents,
      CREATE_IN_CONTENT_ROOT,
      module,
      generateWSDD4Service(createOptions, module, classes),
      //"<service name=\"" + createOptions.getWsName() + "\" provider=\"java:RPC\" style=\"" +
      //  createOptions.getBindingStyle().toLowerCase() + "\" use=\"" + createOptions.getUseOfItems().toLowerCase() +  "\">\n" +
      //  "  <parameter name=\"className\" value=\"" + createOptions.getWsClassName() + "\"/>\n" +
      //  "  <parameter name=\"allowedMethods\" value=\"*\"/>\n" +
      //  "  <parameter name=\"scope\" value=\"Application\"/>\n" +
      //  "  <namespace>" + createOptions.getWsNamespace() + "</namespace>" +
      //  " </service>",
      this
    );

    onSuccessAction.run();
  }
  @NonNls
  static final String TYPE_MAPPING = "<typeMapping qname=\"ns:%1$s\" xmlns:ns=\"%2$s\"\n" +
                               "    languageSpecificType=\"java:%3$s\"\n" +
                               "    serializer=\"%4$s\"\n" +
                               "    deserializer=\"%5$s\"\n" +
                               "    encodingStyle=\"%6$s\"/>\n";
  @NonNls
  static final String ARRAY_MAPPING = "<arrayMapping\n" +
                                      "        xmlns:ns=\"%1$s\"\n" +
                                      "        qname=\"ns:%2$s\"\n" +
                                      "        type=\"java:%3$s\"\n" +
                                      "        innerType=\"ns2:%4$s\" xmlns:ns2=\"%5$s\"\n" +
                                      "        encodingStyle=\"%6$s\"/>\n";
  @NonNls
  static final String ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";
  @NonNls
  static final String SERVICE_DEF = "<service name=\"%1$s\" provider=\"java:RPC\" style=\"%2$s\" use=\"%3$s\">\n" +
                                    "   <parameter name=\"className\" value=\"%4$s\"/>\n" +
                                    "   <parameter name=\"allowedMethods\" value=\"*\"/>\n" +
                                    "   <parameter name=\"scope\" value=\"Application\"/>\n" +
                                    "   <namespace>%5$s</namespace>\n" +
                                    "   %6$s" +
                                    "</service>";
  static final @NonNls String DEFAULT_SERIALIZER = "org.apache.axis.encoding.ser.BeanSerializerFactory";
  static final @NonNls String DEFAULT_DESERIALIZER = "org.apache.axis.encoding.ser.BeanDeserializerFactory";

  public static String generateWSDD4Service(DeployWebServiceOptions createOptions, Module module, PsiClass[] references) {
    String namespace = createOptions.getWsNamespace();

    StringBuilder typesMapping = new StringBuilder();
    Set<PsiType> arrays = findRefferencesToArrays(createOptions.getWsClass());
    for (PsiClass clazz : references) {
      if (! createOptions.getWsClass().equals(clazz)) {
        typesMapping.append(String.format(TYPE_MAPPING,
                                  clazz.getName(),
                                  createOptions.getWsNamespace(),
                                  clazz.getQualifiedName(),
                                  DEFAULT_SERIALIZER,
                                  DEFAULT_DESERIALIZER,
                                  ENCODING_STYLE));
        PsiType type = JavaPsiFacade.getInstance(module.getProject()).getElementFactory().createType(clazz);

        if (arrays.contains(type)) {
          typesMapping.append(String.format(ARRAY_MAPPING,
                                            namespace,
                                            "ArrayOf" + clazz.getName(),
                                            clazz.getQualifiedName() + "[]",
                                            clazz.getName(),
                                            namespace,
                                            ENCODING_STYLE));
        }
      }
    }
    return String.format(SERVICE_DEF,
                         createOptions.getWsName(),
                         createOptions.getBindingStyle().toLowerCase(),
                         createOptions.getUseOfItems().toLowerCase(),
                         createOptions.getWsClassName(),
                         namespace,
                         typesMapping.toString());
  }

  static Set<PsiType> findRefferencesToArrays(PsiClass clazz) {
    final Set<PsiType> arraysTypes = new ArrayListSet<PsiType>();
    for (PsiMethod method : clazz.getMethods()) {
      PsiType returnType = method.getReturnType();
      if (returnType instanceof PsiArrayType) {
        arraysTypes.add(((PsiArrayType)returnType).getComponentType());
      }
      for (PsiParameter param : method.getParameterList().getParameters()) {
        if (param.getType() instanceof PsiArrayType) {
          arraysTypes.add(((PsiArrayType)param.getType()).getComponentType());
        }
      }
    }
    return arraysTypes;
  }

  public void undeployWebService(final String webServiceName, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    DeployUtils.removeFromConfigFile(
      pathComponents,
      CREATE_IN_CONTENT_ROOT,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag xmlTag) {
          if (SERVICE_TAG_NAME.equals(xmlTag.getLocalName())) {
            return webServiceName.equals(xmlTag.getAttributeValue("name"));
          }
          return false;
        }
      }
    );

    onSuccessAction.run();
  }

  public String[] getAvailableWebServices(Module module) {
    final List<String> wsnames = new ArrayList<String>(1);
    DeployUtils.processTagsInConfigFile(
      pathComponents,
      CREATE_IN_CONTENT_ROOT,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag xmlTag) {
          if (SERVICE_TAG_NAME.equals(xmlTag.getLocalName())) {
            final String name = xmlTag.getAttributeValue("name");
            if (name != null && !predefinedServices.contains(name)) wsnames.add(name);
          }
          return true;
        }
      }
    );
    
    return wsnames.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public ExternalProcessHandler getGenerateJavaFromWsdlHandler(GenerateJavaFromWsdlOptions options) {
    @NonNls final List<String> commandLineParameters = new LinkedList<String>();

    final String packagePrefix = options.getPackagePrefix();
    if (packagePrefix!=null) {
      commandLineParameters.add("-p".concat(packagePrefix));
    }

    final String user = options.getUser();
    if (user.length() > 0) {
      commandLineParameters.add("-U" + user);
      commandLineParameters.add("-P" + new String(options.getPassword()));
    }

    if (options.isToGenerateTestCase()) {
      commandLineParameters.add("-t");
    }

    if (options.generateClassesForArrays()) {
      commandLineParameters.add("-w");
    }

    final String typeVersion = options.getTypeVersion();
    commandLineParameters.add("-T" + typeVersion);

    // set output directory:
    final String output = options.getOutputPath();
    commandLineParameters.add("-o".concat(output));

    commandLineParameters.add(options.getWsdlUrl());
    if (options.isGenerateAllElements()) {
      commandLineParameters.add("-a");
    }

    if (!options.isSupportWrappedStyleOperation()) {
      commandLineParameters.add("-W");
    }

    if (options.isServersideSkeletonGeneration()) {
      commandLineParameters.add("-s");
    }

    return new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "WSDL 2 Java",
      WSDL2JAVA,
      LibUtils.getLibUrlsForToolRunning(this, options.getSelectedModule()),
      commandLineParameters.toArray(new String[commandLineParameters.size()]),
      null,
      false
    );
  }

  public boolean supportsJaxWs2() {
    return false;
  }

  public boolean deploymentSupported() {
    return true;
  }
}
