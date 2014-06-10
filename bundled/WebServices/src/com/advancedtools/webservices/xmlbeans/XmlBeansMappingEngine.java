package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author maxim
 */
public class XmlBeansMappingEngine implements ExternalEngine {
  private static final String XML_BEANS_LIBRARY_NAME = "XmlBeans";
  private static final String XML_BEANS_RT_LIBRARY_NAME = "XmlBeans Runtime";
  public static final String XML_BEANS_2_ENGINE = "XmlBeans 2";
  static final Logger LOG = Logger.getInstance("webservicesplugin.xmlbeans.java");

  public String getName() {
    return XML_BEANS_2_ENGINE;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    final String commonXmlBeansPrefix = LibUtils.accessingLibraryJarsFromPluginBundledLibs(getBasePath()) ? "":"lib" + File.separator;
    final String[] commonJars = new String[]{
      commonXmlBeansPrefix + "xbean.jar",
      commonXmlBeansPrefix + "jsr173_1.0_api.jar",
      commonXmlBeansPrefix + "resolver.jar"
    };

    if (context.isForRunningGeneratedCode()) {
      return new LibraryDescriptor[] {
        new LibraryInfo(XML_BEANS_RT_LIBRARY_NAME, ArrayUtil.append(commonJars,commonXmlBeansPrefix + "xmlpublic.jar"))
      };
    } else {
      return new LibraryDescriptor[] {
        new LibraryInfo(
          XML_BEANS_LIBRARY_NAME,
          commonJars
        )
      };
    }
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getXmlBeansPath();
  }

  public static void doXmlBeanGen(String url, String completeFileName, String[] classPath, final Module module,
                                  final boolean addToLibs, final @Nullable Runnable onSuccess, @Nullable Runnable toRestart) {
    List<String> parameters = new LinkedList<String>();

    int beginIndex = completeFileName.lastIndexOf(File.separatorChar);
    if (beginIndex == -1 ) beginIndex = completeFileName.lastIndexOf('/');

    String outputPath;
    final String outputFileName = completeFileName.substring(beginIndex + 1);

    if (beginIndex != -1) {
      outputPath = completeFileName.substring(0, beginIndex);
      if (outputPath.startsWith(LibUtils.FILE_URL_PREFIX)) outputPath = outputPath.substring(LibUtils.FILE_URL_PREFIX.length());
    } else {
      outputPath = url.substring(0, url.lastIndexOf('/'));
    }

    final Project project = module.getProject();

    completeFileName = outputPath + File.separator + outputFileName;
    VirtualFile outputFile = EnvironmentFacade.getInstance().findRelativeFile(completeFileName, null);
    if (outputFile != null) {
      EnvironmentFacade.getInstance().prepareFileForWrite(PsiManager.getInstance(project).findFile(outputFile));
    }

    parameters.add("-out");
    parameters.add(completeFileName);

    parameters.add("-quiet");

    parameters.add(url);
    WebServicesPluginSettings.getInstance().addLastXmlBeansUrl(url);
    parameters.add("-dl"); // allow schema download

    final InvokeExternalCodeUtil.JavaExternalProcessHandler externalProcessHandler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "Schema Compiler",
      "org.apache.xmlbeans.impl.tool.SchemaCompiler",
      classPath,
      parameters.toArray(new String[parameters.size()]),
      module,
      true
    );

    Function<Exception, Void> actionAtFailure = new Function<Exception, Void>() {
      public Void fun(Exception e) {
        Messages.showErrorDialog(project, e.getMessage(), "Xml Beans compiler error");
        LOG.debug(e);
        return null;
      }
    };

    if (toRestart != null) {
      final String outputPath1 = outputPath;
      
      InvokeExternalCodeUtil.invokeExternalProcess2(
        externalProcessHandler,
        project,
        new Runnable() {
          public void run() {
            addToLibs(module, addToLibs, outputPath1, outputFileName);
            if (onSuccess != null) onSuccess.run();
          }
        },
        actionAtFailure,
        new Function<Void, Boolean>() {
          public Boolean fun(Void aVoid) {
            return Boolean.TRUE;
          }
        },
        toRestart
      );
    } else {
      try {
        InvokeExternalCodeUtil.invokeExternalProcess(
          externalProcessHandler,
          project
        );
      } catch (InvokeExternalCodeUtil.ExternalCodeException e) {
        actionAtFailure.fun(e);
        return;
      }

      addToLibs(module, addToLibs, outputPath, outputFileName);
    }
  }

  private static void addToLibs(Module module, boolean addToLibs, String outputPath, String outputFileName) {
    if (addToLibs) {
      final LibraryDescriptor[] generatedLibInfos = new LibraryDescriptor[]{ new LibraryInfo(null, outputFileName)};
      LibUtils.setupLibraries(
        module,
        generatedLibInfos ,
        outputPath.length() > 0 ? outputPath + File.separatorChar:"",
        true
      );

      EnvironmentFacade.getInstance().setupLibsForDeployment(module, generatedLibInfos);
    }
  }
}
