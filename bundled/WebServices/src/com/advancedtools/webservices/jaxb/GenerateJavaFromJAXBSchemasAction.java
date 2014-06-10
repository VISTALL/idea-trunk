package com.advancedtools.webservices.jaxb;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @by maxim
 */
public class GenerateJavaFromJAXBSchemasAction extends BaseWSFromFileAction {
  private static final Logger LOG = Logger.getInstance("webservicesplugin.jaxb.java");
  static final @NonNls String DTD_FILE_EXTENTION = "dtd";

  public void actionPerformed(AnActionEvent event) {
    DataContext dataContext = event.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    runAction(project, null);
  }

  private void runAction(final Project project, @Nullable GenerateJavaFromJAXBSchemasDialog previousDialog) {
    final GenerateJavaFromJAXBSchemasDialog dialog = new GenerateJavaFromJAXBSchemasDialog(
      project,
      previousDialog
    );

    dialog.setOkAction(new Runnable() {
      public void run() {
        doAction(project, dialog);
      }
    });

    dialog.show();
  }

  private void doAction(final Project project, final GenerateJavaFromJAXBSchemasDialog dialog) {
    ApplicationManager.getApplication().saveAll();
    LocalVcs.getInstance(project).addLabel(WSBundle.message("generate.java.code.from.jaxb.schemas.lvcs.action"), "");
    List<String> parameters = new LinkedList<String>();
    final String packagePrefix = (String) dialog.getPackagePrefix().getSelectedItem();
    final String outputPath = (String) dialog.getOutputPathes().getSelectedItem();

    final Module moduleForFile = LibUtils.findModuleByOutputPath(project, outputPath);

    if (packagePrefix.length() > 0) {
      WebServicesPluginSettings.getInstance().addLastPackagePrefix(packagePrefix);
      parameters.add("-p");
      parameters.add(packagePrefix);

      new File(outputPath + File.separator + packagePrefix.replace('.',File.separatorChar)).mkdirs();
    }

    parameters.add("-d");

    parameters.add(outputPath);

    final String moduleClassPath = InvokeExternalCodeUtil.buildClasspathForModule(moduleForFile);
    if (moduleClassPath.length() > 0) {
      parameters.add("-classpath");
      parameters.add(moduleClassPath);
    }

    if (dialog.toMarkGeneratedCodeWithAnnotations()) {
      parameters.add("-mark-generated");
    }

    if (dialog.toEnableSourceLocationSupport()) {
      parameters.add("-Xlocator");
    }

    if (dialog.toCreateSynchronizedMethods()) {
      parameters.add("-Xsync-methods");
    }

    if (dialog.toMakeGeneratedCodeReadOnly()) {
      parameters.add("-readOnly");
    }

    if (!dialog.toCreatePackageLevelAnnotations()) {
      parameters.add("-npa");
    }

    parameters.add("-quiet");
    final String url = (String) dialog.getUrl().getComboBox().getSelectedItem();
    final String[] urls = url.split(GenerateJavaFromJAXBSchemasDialog.SEPARATOR_CHAR);

    WebServicesPluginSettings.getInstance().addLastJAXBUrl(urls[urls.length - 1]);

    parameters.addAll(Arrays.asList(urls));

    //we assume that all files have the same extension as the latest file has
    if (url.endsWith(WebServicesPluginSettings.WSDL_FILE_EXTENSION)) {
      parameters.add("-wsdl");
    } else if (url.endsWith(DTD_FILE_EXTENTION)) {
      parameters.add("-dtd");
    }

    final Function<Exception, Void> atFailure = new Function<Exception, Void>() {
      public Void fun(Exception e) {
        Messages.showErrorDialog(project, e.getMessage(), "XJC generation error");
        LOG.debug(e);
        return null;
      }
    };

    final ExternalEngine engine = WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(JaxbMappingEngine.JAXB_2_ENGINE);

    final String basePath = engine.getBasePath();
    final InvokeExternalCodeUtil.JavaExternalProcessHandler processHandler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "XJC",
      basePath != null ? "com.sun.tools.xjc.XJCFacade" : "com.sun.tools.internal.xjc.Driver",
      LibUtils.getLibUrlsForToolRunning(engine, moduleForFile),
      parameters.toArray(new String[parameters.size()]),
      moduleForFile,
      true
    );

    InvokeExternalCodeUtil.addEndorsedJarDirectory(processHandler, engine, moduleForFile);

    InvokeExternalCodeUtil.invokeExternalProcess2(
      processHandler,
      project,
      new Runnable() {
        public void run() {
          if (dialog.getAddLibs().isSelected()) {
            LibUtils.setupLibsForGeneratedCode(moduleForFile, engine, null);
          }

          try {
            for (String schema : urls) LibUtils.saveSourceGeneratedFile(schema, outputPath, packagePrefix);
          } catch (IOException e) {
            atFailure.fun(e);
            return;
          }
          LibUtils.doFileSystemRefresh();

          InvokeExternalCodeUtil.navigateToPackage(moduleForFile, packagePrefix, outputPath);
        }
      },
      atFailure,
      new Function<Void, Boolean>() {
        public Boolean fun(Void aVoid) {
          return Boolean.TRUE;
        }
      },
      new Runnable() {
        public void run() {
          runAction(project, dialog);
        }
      }
    );
  }

  static boolean isAcceptableFileForGenerateJAXBJavaFromSchema(VirtualFile virtualFile) {
    final String extension = virtualFile.getExtension();
    return WebServicesPluginSettings.XSD_FILE_EXTENSION.equals(extension) ||
      DTD_FILE_EXTENTION.equals(extension) ||
      WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(extension);
  }

  public boolean isAcceptableFile(VirtualFile file) {
    return isAcceptableFileForGenerateJAXBJavaFromSchema(file);
  }
}
