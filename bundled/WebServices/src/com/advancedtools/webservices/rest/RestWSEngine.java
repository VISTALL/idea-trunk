package com.advancedtools.webservices.rest;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.jaxb.JaxbMappingEngine;
import com.advancedtools.webservices.utils.ExternalProcessHandler;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.wsengine.WSEngineUtils;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @by Maxim
 * @by Konstantin Bulenkov
 */
public class RestWSEngine implements WSEngine {
  @NonNls
  public static final String NAME = "RESTful Web Services";
  @NonNls
  static final String  WADL_HOME = "WADL_HOME";

  public static RestWSEngine getInstance() {
    return (RestWSEngine)WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(NAME);
  }

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
    return "REST_SWDP";
  }

  public void doAdditionalWSServerSetup(Module currentModule) {
  }

  public String checkNotAcceptableClassForGenerateWsdl(PsiClass clazz) {
    return null;
  }

  public String checkNotAcceptableClassForDeployment(PsiClass clazz) {
    return null;
  }

  public void generateWsdlFromJava(GenerateWsdlFromJavaOptions options, Function<File, Void> onSuccessAction, Function<Exception, Void> onException, Runnable editAgain) {
  }

  public void deployWebService(DeployWebServiceOptions createOptions, Module module, Runnable onSuccessAction, Function<Exception, Void> onExceptionAction, Runnable restartAction, Function<Void, Boolean> canRestartPredicate) {
    final String sourceOutputDir = LibUtils.findOutputDir(module);
    final StringBuilder textBuilder = new StringBuilder();
    String path = LibUtils.getLibUrlByName("jaxws-tools.jar", module);
    path = (path == null) ? "lib/jaxws-tools.jar" : path.replace(File.separatorChar,'/');    
    textBuilder.append(
      "<project default=\"\">" +
      "    <property name=\"file.reference.jaxws-tools.jar\"  value=\"" + path + "\"/>\n" +
      "    <property name=\"src.dir\" value=\"" + sourceOutputDir + "\">" +
      "    <path id=\"apt.classpath.id\">\n" +
      "    <pathelement location=\"${file.reference.jaxws-tools.jar}\"/>\n" +
      "    </path>" +
      "<taskdef name=\"apt\" classname=\"com.sun.tools.ws.ant.Apt\">\n" +
      "    <classpath refid=\"apt.classpath.id\"/>\n" +
      "</taskdef>\n" +
      "<target name=\"genresources\">\n" +
      "    <apt fork=\"true\" destdir=\"${build.classes.dir}\" \n" +
      "         sourcedestdir=\"gen-src\" sourcePath=\"${src.dir}\">\n" +
      "        <classpath>\n" +
      "            <path refid=\"apt.classpath.id\"/>\n" +
      "            <pathelement location=\"${build.dir}\"/>\n" +
      "        </classpath>" +
      "        <option key=\"restbeansdestdir\" value=\".\"/>\n" +
      "        <option key=\"restbeanpkg\" value=\"com.sun.ws.rest.samples.jaxws.resources\"/>\n" +
      "        <option key=\"noservlet\"/>\n" +
      "        <source dir=\"${src.dir}\">\n" +
      "            <include name=\"**/*.java\"/>\n" +
      "        </source>\n" +
      "    </apt>" +
      "</target>\n" +
      "</project>");

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
    final List<String> parameters = new ArrayList<String>(5);

    parameters.add("-o");
    parameters.add(InvokeExternalCodeUtil.toAntPath(options.getOutputPath()));
    parameters.add("-p");
    parameters.add(options.getPackagePrefix());
    
    try {
      parameters.add(new URL(options.getWsdlUrl().replaceAll(" ","%20")).toURI().toASCIIString());
    } catch (Exception e) {
      throw new InvokeExternalCodeUtil.ExternalCodeException(e.getMessage()); 
    }

    return new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "WADL2Java",
      "org.jvnet.ws.wadl2java.Main",
      LibUtils.getLibUrlsForToolRunning(this, options.getSelectedModule()),
      parameters.toArray(new String[parameters.size()]),
      options.getSelectedModule(),
      false
    );
  }

  public String getName() {
    return NAME;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    if (true) return new LibraryDescriptor[0];
    final String pathForSharedLibRelativeToBase = "shared" + File.separatorChar + "lib";
    final String pathForWadlLib = "wadl" + File.separator + "lib";

    if (!context.isForRunningGeneratedCode()) {
      File baseFile = new File(getBasePath());
      final String basePath = baseFile.getPath();
      File[] sharedFiles = new File(baseFile, pathForSharedLibRelativeToBase).listFiles();
      String[] libs = new String[sharedFiles.length + 1];
      libs[sharedFiles.length] = pathForWadlLib + File.separatorChar + "wadl2java.jar";

      for(int i = 0; i < sharedFiles.length; ++i) {
        libs[i] = WSEngineUtils.stripPrefixPath(sharedFiles[i].getAbsolutePath(), basePath);
      }

      return new LibraryDescriptor[] {
        new LibraryInfo("", libs)
      };
    } else {
      final LibraryDescriptor[] jaxbLibraries = JaxbMappingEngine.buildJaxbNeededJars(pathForSharedLibRelativeToBase + File.separatorChar, context);
      final String pathPrefix = "rest-impl/lib/";
      final String pathPrefix2 = pathForWadlLib + File.separatorChar;

      final LibraryDescriptor[] libraryDescriptors = new LibraryDescriptor[]{
        new LibraryInfo(
          "SUN_REST_LIB",
          new String[]{
            pathPrefix + "http.jar",
            pathPrefix + "jsr250-api.jar",
            pathPrefix + "localizer.jar",
            pathPrefix + "restbeans-impl.jar",
          }
        ),
        new LibraryInfo("WADL2Java", pathPrefix2 + "wadl2java.jar")
      };
      
      return ArrayUtil.mergeArrays(jaxbLibraries, libraryDescriptors, LibraryDescriptor.class);
    }
  }
    //String wadlHome = System.getenv(WADL_HOME);
    //if (wadlHome != null) {
    //  File lib = new File(wadlHome + File.separatorChar + "lib");
    //  if (lib.exists() && lib.isDirectory()) {
    //    File[] jars = lib.listFiles(new FileFilter() {
    //      public boolean accept(final File file) {
    //        return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
    //      }
    //    });
    //    String[] w2jJars = new String[jars.length];
    //    for (int i = 0; i < jars.length; i++) w2jJars[i] = File.separatorChar + "lib" + File.separatorChar + jars[i].getName();
    //    return new LibraryDescriptor[] {new LibraryInfo("Wadl2Java", w2jJars)};
    //  }
    //}
    //return new LibraryDescriptor[]{};

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getSunWebDevelopmentPackPath();
  }

  public boolean supportsJaxWs2() {
    return false;
  }

  public boolean deploymentSupported() {
    return false;
  }

  public String getWadl2JavaHome() {
    String home = System.getenv(WADL_HOME);
    while (home.endsWith(File.separator)) home = home.substring(0, home.length() - 2);
    return home;
  }

  protected String getLibraryName(final String selectedVersion) {
    return "Jersey";
  }

  //public String getDefaultClientCode(final String pckg, final String className) {
  //  StringBuilder clazz = new StringBuilder();
  //  clazz.append("package ").append(pckg).append(";\r\n");
  //  clazz.append("public class ").append(className).append(" {\r\n");
  //  clazz.append("\tpublic static void main(String[] args) {\r\n");
  //  clazz.append("\t\tSystem.out.println(\"Hello World\");\r\n");
  //  clazz.append("\t}\r\n");
  //  clazz.append("}");
  //  return clazz.toString();
  //}


}
