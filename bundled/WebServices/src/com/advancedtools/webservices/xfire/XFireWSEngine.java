package com.advancedtools.webservices.xfire;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisUtil;
import com.advancedtools.webservices.utils.*;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.wsengine.WSEngineUtils;
import com.advancedtools.webservices.xmlbeans.XmlBeansMappingEngine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * @by maxim
 */
public class XFireWSEngine implements WSEngine {
  private static final String XFIRE_LIBRARY_NAME = "XFire 1.X";
  private static final String CXF_LIBRARY_NAME = "CXF";
  private static final String XFIRE_RT_LIBRARY_NAME = "XFire 1.X Runtime";
  private static final String CXF_RT_LIBRARY_NAME = "CXF Runtime";

  private static final String JAXB_2_0_MAPPING_TYPE = "JAXB 2.0";
  private static final String XML_BEANS_MAPPING_TYPE = "XmlBeans";
  private static final String XFIRE_PLATFORM = "XFire 1.X / CXF";
  private final @NonNls String[] pathComponents = new String[] {"META-INF","xfire", "services.xml"};
  private final @NonNls String[] cxfPathComponents = new String[] {"WEB-INF","cxf-servlet.xml"};
  private static final boolean CREATE_IN_CONTENT_ROOT = false;
  private static final boolean CXF_CREATE_IN_CONTENT_ROOT = true;

  public String getName() {
    return XFIRE_PLATFORM;
  }

  private static final @NonNls Set<String> ourExcludeJarNamesSet = new HashSet<String>();
  static {
    ourExcludeJarNamesSet.add("servlet-api-");
    ourExcludeJarNamesSet.add("jetty-");
    ourExcludeJarNamesSet.add("-servlet_2.5_spec");
  }

  public LibraryDescriptor[] getLibraryDescriptors(final LibraryDescriptorContext context) {
    final String xFirePath = WebServicesPluginSettings.getInstance().getXFirePath();

    File[] xfireLibFiles = buildListOfJarsInLibDirectory(context, xFirePath + "/lib");
    File xFireJar = XFireUtils.findXFireJar(xFirePath);
    final boolean isCxf = isCxf(xFireJar);
    String[] xfireLibPathes = new String[xfireLibFiles.length + (isCxf ? 0 : 1)];

    for(int i = 0; i < xfireLibFiles.length; ++i) {
      xfireLibPathes[i] = WSEngineUtils.stripPrefixPath(xfireLibFiles[i].getAbsolutePath(), xFirePath);
    }

    if (!isCxf) {
      xfireLibPathes[xfireLibFiles.length] = WSEngineUtils.stripPrefixPath(xFireJar.getAbsolutePath(), xFirePath);
    }


    String libName = context.isForRunningGeneratedCode() ? (isCxf ? CXF_RT_LIBRARY_NAME : XFIRE_RT_LIBRARY_NAME) : 
      (isCxf ? CXF_LIBRARY_NAME : XFIRE_LIBRARY_NAME);
    return new LibraryDescriptor[] { new LibraryInfo(libName, xfireLibPathes) };
  }

  private static File[] buildListOfJarsInLibDirectory(final LibraryDescriptorContext context, String s1) {
    File[] xfireLibFiles;
    final String bindingType = context.getBindingType();
    xfireLibFiles = new File(s1).listFiles(new FilenameFilter() {
      public boolean accept(File file, String filename) {
        final boolean isjar = filename.endsWith(".jar");

        if (isjar) {
          for(String s:ourExcludeJarNamesSet) if (filename.indexOf(s) >= 0) return false;

          if (filename.indexOf("jaxb") != -1) {
            if (context.isForRunningGeneratedCode() && filename.indexOf("xjc") != -1) {
              return false;
            }

            if (JAXB_2_0_MAPPING_TYPE.equals(bindingType)) {
              return filename.indexOf("-1.0") == -1;
            } else {
              return (context.isForRunningGeneratedCode() && bindingType == null) || filename.indexOf("xjc") != -1;
            }
          }
        }
        return isjar;
      }
    });
    return xfireLibFiles;
  }

  private static boolean isCxf(File xFireJar) {
    return xFireJar.getName().indexOf("cxf") >= 0;
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getXFirePath();
  }

  public boolean hasSeparateClientServerJavaCodeGenerationOption() {
    return isCxf();
  }

  public boolean allowsTestCaseGeneration() {
    return false;
  }

  @Nullable
  public String[] getSupportedMappingTypesForJavaFromWsdl() {
    if (isCxf()) {
      return new String[] {
        JAXB_2_0_MAPPING_TYPE
      };
    }
    return new String[] {
      JAXB_2_0_MAPPING_TYPE,
      XML_BEANS_MAPPING_TYPE
    };
  }

  public String getDeploymentServletName() {
    return isCxf() ? "cxf":"XFireServlet";
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
    final boolean cxf = isCxf();
    DeployUtils.addFileToModuleFromTemplate(
      currentModule,
      cxf ? cxfPathComponents:pathComponents,
      cxf ? "cxf-servlet.xml":"XFire.services.xml",
      cxf ? CXF_CREATE_IN_CONTENT_ROOT:CREATE_IN_CONTENT_ROOT
    );
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    return null;
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    return null;
  }

  public void generateWsdlFromJava(final GenerateWsdlFromJavaOptions options, final Function<File, Void> onSuccessAction,
                                   final Function<Exception, Void> onException, Runnable editAgain) {
    final PsiClass psiClass = options.getClassForOperation();

    final String generateWsdlDir = psiClass.getContainingFile().getContainingDirectory().getVirtualFile().getPath();
    final File tempFile = new File(
      generateWsdlDir + "/" + (psiClass.getName() + ".wsdl")
    );

    File tempDir;

    try {
      tempDir = FileUtils.createTempDir("cxf");
    } catch (IOException e) {
      onException.fun(e);
      return;
    }

    final InvokeExternalCodeUtil.JavaExternalProcessHandler externalProcessHandler = buildGenerateWsdlFromJavaHandler(
      options, tempFile, psiClass, tempDir, onException);
    if (externalProcessHandler == null) return;

    InvokeExternalCodeUtil.invokeExternalProcess2(
      externalProcessHandler,
      options.getModule().getProject(),
      new Runnable() {
        public void run() {
          try {
            final String webServiceName = psiClass.getName();
            FileUtils.copyWsdlWithReplacementOfSoapAddress(
              tempFile,
              tempFile,
              isCxf() ? "http://localhost:9090/hello":AxisUtil.getWebServiceUrlReference("",webServiceName).replaceFirst(":"+ WebServicesPluginSettings.getInstance().getHostPort(),""),
              options.getWebServiceURL()
            );
            options.getSuccessRunnable(onSuccessAction, tempFile).run();
          } catch (IOException ex) {
            onException.fun(ex);
          }
        }
      },
      onException,
      options.isParametersStillValidPredicate(),
      editAgain
    );
  }

  private InvokeExternalCodeUtil.JavaExternalProcessHandler buildGenerateWsdlFromJavaHandler(GenerateWsdlFromJavaOptions options,
    File tempFile, PsiClass psiClass, File marshalledCodeDir, Function<Exception,Void> onException) {

    String[] classpath = options.getClassPathEntries();
    @NonNls final List<String> parameters = new LinkedList<String>();
    @NonNls String className;
    String title;

    final boolean cxf = isCxf();

    if (cxf) {
      className = XFireUtils.getJavaToWsdlClassName(classpath);
      title =  "Generate CXF Wsdl";
//      parameters.add("-verbose");
      parameters.add("-o");
      parameters.add(tempFile.getPath());
      parameters.add("-t");
      parameters.add(options.getWebServiceNamespace());

      parameters.add("-cp");
      final String cp = InvokeExternalCodeUtil.buildClasspathForModule(options.getModule());
      final String canonicalPath;
      try {
        canonicalPath = marshalledCodeDir.getCanonicalPath();
      } catch (IOException ex) {
        onException.fun(ex);
        return null;
      }
      parameters.add(cp.concat(InvokeExternalCodeUtil.CLASS_PATH_SEPARATOR).concat(canonicalPath));

      classpath = ArrayUtil.append(classpath, canonicalPath);

      parameters.add("-s");
      parameters.add(canonicalPath);

      parameters.add("-classdir");
      parameters.add(canonicalPath);

      FileUtils.addClassAsCompilerResource(options.getModule().getProject());

      parameters.add(psiClass.getQualifiedName());
    } else {
      parameters.add(psiClass.getQualifiedName());
      parameters.add(psiClass.getName());
      parameters.add(options.getWebServiceNamespace());
      parameters.add(options.getWebServiceURL());

      parameters.add(tempFile.getPath());

      classpath = ArrayUtil.append(classpath, LibUtils.detectPluginPath() + "/lib/rt/WebServicesRT.jar");
      className = "com.advancedtools.webservices.rt.xfire.WsdlGenerator";
      title = "Generate XFire Wsdl";
    }

    final InvokeExternalCodeUtil.JavaExternalProcessHandler processHandler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      title,
      className,
      classpath,
      parameters.toArray(new String[parameters.size()]),
      options.getModule(),
      false
    );

    if (cxf) {
      processHandler.addCommandLineProperty("java.util.logging.config.file", getBasePath() + "/etc/logging.properties");
      processHandler.setOutputConsumer(createCxfOutputFilter());
    }
    return processHandler;
  }

  public static InvokeExternalCodeUtil.OutputConsumer createCxfOutputFilter() {
    return new InvokeExternalCodeUtil.OutputConsumer() {
      public boolean handle(String output, String errOutput) throws InvokeExternalCodeUtil.ExternalCodeException {
        if(errOutput.indexOf("Error") != -1) throw new InvokeExternalCodeUtil.ExternalCodeException(errOutput);
        if(output.length() > 0) throw new InvokeExternalCodeUtil.ExternalCodeException(output);
        return false;
      }
    };
  }

  public boolean isCxf() {
    final String path = getBasePath();
    if (path == null) return false;
    return isCxf(XFireUtils.findXFireJar(path));
  }

  public String[] getWebServicesOperations(String webServiceName, Module module) {
    return new String[0];
  }

  public void deployWebService(final DeployWebServiceOptions createOptions, final Module module, final Runnable onSuccessAction, final Function<Exception, Void> onExceptionAction, final Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    final boolean cxf = isCxf();

    final @NonNls String baseWsdlFileName = createOptions.getWsName() + ".wsdl";
    final @NonNls String toInsert;

    if (cxf) {
      toInsert = "<jaxws:endpoint\n" +
        "        id=\"" + createOptions.getWsName() +"\"\n" +
        "        implementor=\"" + createOptions.getWsClassName() + "\"\n" +
        "        wsdlLocation=\"/" + ("WEB-INF/" + baseWsdlFileName) + "\"\n" +
        "        address=\"/" + createOptions.getWsName() + "\">\n" +
        "                <jaxws:features>\n" +
        "                     <bean class=\"org.apache.cxf.feature.LoggingFeature\"/>\n" +
        "                </jaxws:features>\n" +
        "    </jaxws:endpoint>";
    } else {
      toInsert = "<service>\n" +
        "    <name>" + createOptions.getWsName() + "</name>\n" +
        "    <namespace>" + createOptions.getWsNamespace() + "</namespace>\n" +
        "    <serviceClass>" + createOptions.getWsClassName() + "</serviceClass>\n" +
        "    <implementationClass>" + createOptions.getWsClassName() + "</implementationClass>\n" +
        "  </service>";
    }

    final Runnable addToConfig = new Runnable() {
      public void run() {
        DeployUtils.addToConfigFile(
          cxf ? cxfPathComponents : pathComponents,
          cxf ? CXF_CREATE_IN_CONTENT_ROOT:CREATE_IN_CONTENT_ROOT,
          module,
          toInsert,
          XFireWSEngine.this
        );

        onSuccessAction.run();
      }
    };

    if (cxf) {
      final String outputFileName = DeployUtils.determineWhereToPlaceTheFileUnderWebInf(module, baseWsdlFileName);

      File marshalledCodeDir = new File(LibUtils.findOutputDir(module));

      final GenerateWsdlFromJavaOptions generateWsdlFromJavaOptions = new GenerateWsdlFromJavaOptions() {
        public PsiClass getClassForOperation() {
          return createOptions.getWsClass();
        }

        public String getTypeMappingVersion() {
          return null;
        }

        public String getSoapAction() {
          return null;
        }

        public String getBindingStyle() {
          return createOptions.getBindingStyle();
        }

        public String getUseOfItems() {
          return createOptions.getUseOfItems();
        }

        public String getGenerationType() {
          return null;
        }

        public String getMethods() {
          return null;
        }

        public Module getModule() {
          return module;
        }

        public String getWebServiceNamespace() {
          return createOptions.getWsNamespace();
        }

        public String getWebServiceURL() {
          return AxisUtil.getWebServiceUrlReference("", createOptions.getWsName());
        }

        public String[] getClassPathEntries() {
          return ArrayUtil.mergeArrays(
            LibUtils.getLibUrlsForToolRunning(XFireWSEngine.this, module),
            InvokeExternalCodeUtil.buildClasspathStringsForModule(module),
            String.class
          );
        }

        public Function<Void, Boolean> isParametersStillValidPredicate() {
          return null;
        }

        public Runnable getSuccessRunnable(Function<File, Void> successAction, File file) {
          return null;
        }
      };
      final InvokeExternalCodeUtil.JavaExternalProcessHandler externalProcessHandler = buildGenerateWsdlFromJavaHandler(
        generateWsdlFromJavaOptions, new File(outputFileName), createOptions.getWsClass(), marshalledCodeDir, onExceptionAction);
      if (externalProcessHandler != null) {
        InvokeExternalCodeUtil.invokeExternalProcess2(externalProcessHandler, module.getProject(),
          new Runnable() {
            public void run() {
              final File fileName = new File(outputFileName);
              try {
                FileUtils.copyWsdlWithReplacementOfSoapAddress(
                fileName,
                fileName,
                "http://localhost:9090/hello",
                generateWsdlFromJavaOptions.getWebServiceURL()
              );
              } catch (IOException e) {
                onExceptionAction.fun(e);
                return;
              }
              addToConfig.run();
            }
          }, onExceptionAction,
          new Function<Void, Boolean>() {
            public Boolean fun(Void aVoid) {
              return restartAction != null;
            }
          },
          restartAction
        );
        return;
      }
    }

    addToConfig.run();
  }

  public void undeployWebService(final String webServiceName, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    final boolean cxf = isCxf();

    DeployUtils.removeFromConfigFile(
      cxf ? cxfPathComponents : pathComponents,
      cxf ? CXF_CREATE_IN_CONTENT_ROOT:CREATE_IN_CONTENT_ROOT,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag xmlTag) {
          if (cxf) {
            return webServiceName.equals(xmlTag.getAttributeValue("id"));
          } else {
            final XmlTag nameTag = xmlTag.findFirstSubTag("name");
            return nameTag != null ? webServiceName.equals(nameTag.getValue().getTrimmedText()): false;
          }
        }
      }
    );

    onSuccessAction.run();
  }

  public String[] getAvailableWebServices(Module module) {
    final List<String> availableWS = new ArrayList<String>(1);
    final boolean cxf = isCxf();

    DeployUtils.processTagsInConfigFile(
      cxf ? cxfPathComponents:pathComponents,
      cxf ? CXF_CREATE_IN_CONTENT_ROOT:CREATE_IN_CONTENT_ROOT,
      module,
      new Processor<XmlTag>() {
        public boolean process(XmlTag xmlTag) {
          if (cxf) {
            final String name = xmlTag.getAttributeValue("id");
            if (name != null) availableWS.add(name);
          } else {
            final XmlTag nameTag = xmlTag.findFirstSubTag("name");
            if (nameTag != null) {
              availableWS.add(nameTag.getValue().getText());
            }
          }
          return true;
        }
      }
    );

    return availableWS.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public ExternalProcessHandler getGenerateJavaFromWsdlHandler(final GenerateJavaFromWsdlOptions options) {
    final boolean cxf = isCxf();

    if (cxf) {
      return getGenerateJavaFromWsdlHandlerForCxf(options);
    }
    
    final boolean xmlBeansMappingType = XML_BEANS_MAPPING_TYPE.equals(options.getBindingType());
    final @NonNls String typesJar = "types.jar";

    String[] libsUrlsForToolRunning = LibUtils.getLibsUrlsFromLibInfos(getLibraryDescriptors(options.getToolRunningContext()), getBasePath());

    if (xmlBeansMappingType) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {

        public void run() {
          LocalFileSystem.getInstance().refreshAndFindFileByIoFile(options.getSavedWsdlFile());
        }
      });

      XmlBeansMappingEngine.doXmlBeanGen(
        options.getSavedWsdlFile().getPath(),
        options.getOutputPath() + File.separatorChar + typesJar,
        libsUrlsForToolRunning,
        options.getSelectedModule(),
        true,
        null,
        null
      );
    }

    final String xFirePath = WebServicesPluginSettings.getInstance().getXFirePath();

    File xfireJar = XFireUtils.findXFireJar(xFirePath);

    final @NonNls StringBuilder antFileText = new StringBuilder();
    antFileText.append("<project default=\"dowsgen\">\n");
    antFileText.append("<target name=\"dowsgen\">\n");
    antFileText.append(" <path id=\"wsgen.path\">\n" +
      "    <pathelement path=\"" + InvokeExternalCodeUtil.toAntPath(xfireJar.getAbsolutePath()) + "\"/>\n" +
      "  </path>\n");
    antFileText.append(
      "  <taskdef name=\"wsgen\" classname=\"org.codehaus.xfire.gen.WsGenTask\" classpathref=\"wsgen.path\"/>\n"
    );

    String outputPath1 = InvokeExternalCodeUtil.toAntPath(options.getOutputPath());
    antFileText.append("  <wsgen outputDirectory=\"" + outputPath1 + "\"\n" +
      "    wsdl=\"" + options.getWsdlUrl() + "\" package=\"" + options.getPackagePrefix() + "\"" +
//      (xFirePath.indexOf("1.0") == -1 ? " overwrite=\"true\"":"") +   // TODO: overwrite does not supported with Jaxb2 + XFire 1.1
      (xmlBeansMappingType ? " binding=\"" + options.getBindingType().toLowerCase() + "\"":"") +
      "/>");

    antFileText.append("\n</target>\n");
    antFileText.append("</project>");

    if (xmlBeansMappingType) {
      libsUrlsForToolRunning = ArrayUtil.append(libsUrlsForToolRunning, options.getOutputPath() + "/" + typesJar);
    }

    for(int i= 0; i < libsUrlsForToolRunning.length; ++i) {
      libsUrlsForToolRunning[i] = InvokeExternalCodeUtil.toAntPath(libsUrlsForToolRunning[i]);
    }

    Set<String> libSet = new LinkedHashSet<String>();
    //libSet.addAll(Arrays.asList(moduleClassPath));
    libSet.addAll(Arrays.asList(libsUrlsForToolRunning));

    return new InvokeExternalCodeUtil.ANTExternalProcessHandler(
      "WSGen",
      antFileText.toString(),
      libSet.toArray(new String[libSet.size()]),
      options.getSelectedModule()
    );
  }

  private ExternalProcessHandler getGenerateJavaFromWsdlHandlerForCxf(GenerateJavaFromWsdlOptions options) {
    final @NonNls List<String> parameters = new ArrayList<String>();

    parameters.add("-d");
    parameters.add(options.getOutputPath());
    parameters.add("-p");
    parameters.add(options.getPackagePrefix());

    if (options.isServersideSkeletonGeneration()) {
      parameters.add("-server");
    } else {
      parameters.add("-client");
    }

//    parameters.add("-verbose");

    final File savedWsdlFile = options.getSavedWsdlFile();
    parameters.add(savedWsdlFile.getPath());

    final InvokeExternalCodeUtil.JavaExternalProcessHandler processHandler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "CXF Wsdl2Java",
      "org.apache.cxf.tools.wsdlto.WSDLToJava",
      LibUtils.getLibsUrlsFromLibInfos(getLibraryDescriptors(options.getToolRunningContext()), getBasePath()),
      parameters.toArray(new String[parameters.size()]),
      options.getSelectedModule(),
      false
    );

    processHandler.addCommandLineProperty("java.util.logging.config.file", getBasePath() + "/etc/logging.properties");

    return processHandler;
  }

  public boolean supportsJaxWs2() {
    return isCxf();
  }

  public boolean deploymentSupported() {
    return true;
  }
}
