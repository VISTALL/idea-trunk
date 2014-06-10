package com.advancedtools.webservices.axis2;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.references.Axis2ServicesXmlReferenceProvider;
import com.advancedtools.webservices.utils.*;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.wsengine.WSEngineUtils;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.Processor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 30.07.2006
 * Time: 2:31:44
 * To change this template use File | Settings | File Templates.
 */
public class Axis2WSEngine implements WSEngine {
  private static final String AXIS2_LIBRARY_NAME = "Axis2";
  private static final String ADB_MAPPING_TYPE = "ADB";
  private static final String JAXME_MAPPING_TYPE = "JaxMe";
  private static final String JIBX_MAPPING_TYPE = "JibX";
  private static final String XMLBEANS_MAPPING_TYPE = "XmlBeans";
  public static final String AXIS2_PLATFORM = "Apache Axis 2";
  private final String[] pathComponents = new String[] {"META-INF","services.xml"};
  private final String[] pathComponents2 = new String[] {"WEB-INF","conf", "axis2.xml"};
  private static final boolean CREATE_IN_CONTENT_ROOT = false;

  public String getName() {
    return AXIS2_PLATFORM;
  }

  public LibraryDescriptor[] getLibraryDescriptors(final LibraryDescriptorContext context) {
    final String axis2BasePath = WebServicesPluginSettings.getInstance().getAxis2Path();
    File[] axis2LibFiles = new File(axis2BasePath + "/lib").listFiles(new FilenameFilter() {
      public boolean accept(File file, String filename) {
        final boolean jarFile = filename.endsWith(".jar");
        
        if (jarFile && filename.startsWith("servletapi") && context.isForRunningGeneratedCode()) {
          return false;
        }
        return jarFile;
      }
    });

    String[] axis2LibPathes = new String[axis2LibFiles.length];

    for(int i = 0; i < axis2LibFiles.length; ++i) {
      axis2LibPathes[i] = WSEngineUtils.stripPrefixPath(axis2LibFiles[i].getAbsolutePath(), axis2BasePath);
    }

    return new LibraryDescriptor[] { new LibraryInfo( AXIS2_LIBRARY_NAME, axis2LibPathes) };
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getAxis2Path();
  }

  public boolean hasSeparateClientServerJavaCodeGenerationOption() {
    return true;
  }

  public boolean allowsTestCaseGeneration() {
    return true;
  }

  public String[] getSupportedMappingTypesForJavaFromWsdl() {
    return new String[] { ADB_MAPPING_TYPE, JAXME_MAPPING_TYPE,
      //JIBX_MAPPING_TYPE, 
      XMLBEANS_MAPPING_TYPE };
  }

  public String getDeploymentServletName() {
    return "Axis2Servlet";
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
    DeployUtils.addFileToModuleFromTemplate(
      currentModule,
      pathComponents,
      "Axis2.services.xml",
      CREATE_IN_CONTENT_ROOT
    );

    DeployUtils.addFileToModuleFromTemplate(
      currentModule,
      pathComponents2,
      "Axis2.conf_axis2.xml",
      true
    );
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    return null;
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    return null;
  }

  public boolean supportsJaxWs2() {
    return false;
  }

  public boolean deploymentSupported() {
    return false;
  }

  public void generateWsdlFromJava(GenerateWsdlFromJavaOptions options, Function<File, Void> onSuccessAction, Function<Exception, Void> onException, Runnable editAgain) {
    final List<String> parametersList = new LinkedList<String>();
    PsiClass psiClass = options.getClassForOperation();
    String wsdlFileName = psiClass.getName() + ".wsdl";

    File tempFile = new File(
      psiClass.getContainingFile().getContainingDirectory().getVirtualFile().getPath() + "/" + wsdlFileName
    );

    parametersList.add("-o");
    parametersList.add(tempFile.getParentFile().getPath());
    parametersList.add("-st");
    parametersList.add(options.getBindingStyle().toLowerCase());

    parametersList.add("-tn");
    String webServiceNamespace = options.getWebServiceNamespace();
    parametersList.add(webServiceNamespace);
    parametersList.add("-stn");
    parametersList.add(webServiceNamespace);

    parametersList.add("-l");
    parametersList.add(options.getWebServiceURL());

    parametersList.add("-u");
    parametersList.add(options.getUseOfItems().toLowerCase());

    parametersList.add("-cn");
    parametersList.add(psiClass.getQualifiedName());

    InvokeExternalCodeUtil.invokeExternalProcess2(
      new InvokeExternalCodeUtil.JavaExternalProcessHandler(
        "Axis2 Java 2 WSDL",
        "org.apache.ws.java2wsdl.Java2WSDL",
        options.getClassPathEntries(),
        parametersList.toArray(new String[parametersList.size()]),
        options.getModule(),
        false
      ),
      options.getModule().getProject(),
      options.getSuccessRunnable(onSuccessAction, tempFile),
      onException,
      options.isParametersStillValidPredicate(),
      editAgain
    );
  }

  public String[] getWebServicesOperations(String webServiceName, Module module) {
    final List<String> result = new ArrayList<String>();

    DeployUtils.processTagsInConfigFile(
      pathComponents,
      CREATE_IN_CONTENT_ROOT,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag xmlTag) {
          if (xmlTag.getAttributeValue(Axis2ServicesXmlReferenceProvider.NAME_ATTR_NAME) != null &&
              Axis2ServicesXmlReferenceProvider.OPERATION_TAG_NAME.equals(xmlTag.getName())
             ) {
            result.add(xmlTag.getAttributeValue(Axis2ServicesXmlReferenceProvider.NAME_ATTR_NAME));
          }
          return true;
        }
      }
    );
    return result.toArray(new String[result.size()]);
  }

  public void deployWebService(DeployWebServiceOptions createOptions, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    DeployUtils.addToConfigFile(
      pathComponents,
      CREATE_IN_CONTENT_ROOT,
      module,
      "<service name=\"" + createOptions.getWsName() + "\">\n" +
        "    <parameter name=\"ServiceClass\" locked=\"false\">" + createOptions.getWsClassName() + "</parameter>\n" +
        "    <operation name=\"*\">\n" +
        "    <messageReceiver  class=\"org.apache.axis2.rpc.receivers.RPCMessageReceiver\" />\n" +
        "    </operation>\n" +
        "</service>",
      this
    );

    onSuccessAction.run();
  }

  public void undeployWebService(final String webServiceName, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    DeployUtils.removeFromConfigFile(
      pathComponents,
      CREATE_IN_CONTENT_ROOT,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag t) {
          return webServiceName.equals(t.getAttributeValue("name"));
        }
      }
    );

    onSuccessAction.run();
  }

  public String[] getAvailableWebServices(Module module) {
    final List<String> availableWS = new ArrayList<String>(1);

    DeployUtils.processTagsInConfigFile(
      pathComponents,
      CREATE_IN_CONTENT_ROOT,
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

  public ExternalProcessHandler getGenerateJavaFromWsdlHandler(GenerateJavaFromWsdlOptions options) {
    List<String> parameters = new ArrayList<String>(3);

    parameters.add("-uri");
    parameters.add(FileUtils.removeFileProtocolPrefixIfPresent(options.getWsdlUrl()));

    parameters.add("-o");
    String outputPathForAxis2 = options.getOutputPath();

    if(outputPathForAxis2.endsWith(File.separator + "src")) { // it adds src to package prefix :(
      outputPathForAxis2 = outputPathForAxis2.substring(0, outputPathForAxis2.length() - 4);
    }
    parameters.add(outputPathForAxis2 + File.separator);

    parameters.add("-p");
    parameters.add(options.getPackagePrefix());

    if (options.isToGenerateTestCase()) {
      parameters.add("-t");
    }

    parameters.add("-d");
    parameters.add(options.getBindingType().toLowerCase());

    if (options.isServersideSkeletonGeneration()) {
      parameters.add("-ss");
      parameters.add("-sd");
      //parameters.add("-ssi");
    }

    return new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "Axis 2 WSDL2Java",
      "org.apache.axis2.wsdl.WSDL2Java",
      LibUtils.getLibsUrlsFromLibInfos(
        getLibraryDescriptors( options.getToolRunningContext() ),
        getBasePath()
      ),
      parameters.toArray(new String[parameters.size()]),
      null,
      true
    );

  }
}
