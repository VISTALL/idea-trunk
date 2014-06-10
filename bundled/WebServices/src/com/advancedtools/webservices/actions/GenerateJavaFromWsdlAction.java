package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSLibrarySynchronizer;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.utils.ExternalProcessHandler;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.utils.facet.WebServicesClientLibraries;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @by maxim
 */
public class GenerateJavaFromWsdlAction extends BaseWSFromFileAction {
  static final @NonNls String WSDL_EXTENSION = "wsdl";
  static final @NonNls String WADL_EXTENSION = "wadl";

  @NonNls
  static final String GENERATE_JAVA_CODE_FROM_WSDL = "Generate Java Code From Wsdl or Wadl";

  public void actionPerformed(AnActionEvent event) {
    final Project project = (Project) event.getDataContext().getData(DataConstants.PROJECT);
    runAction(project, null, null, null);
  }

  public static void runAction(final Project project, @Nullable GenerateJavaFromWsdlDialog previousDialog,
                               @Nullable Module module, final Runnable onSuccess) {
    final GenerateJavaFromWsdlDialog dialog = new GenerateJavaFromWsdlDialog(
      project,
      previousDialog,
      module,
      onSuccess
    );

    dialog.setOkAction(new Runnable() {
      public void run() {
        doOkAction(project, dialog);
      }
    });

    dialog.show();
  }

  private static void doOkAction(final Project project, final GenerateJavaFromWsdlDialog dialog) {
    final String platform = dialog.getWebServicePlatform();
    if (WebServicesClientLibraries.isSupported(platform)) {
      final String[] jars = WSLibrarySynchronizer.getMissedJars(platform);
      if (jars != null && jars.length > 0) {
        @NonNls String msg = "Some necessary jar files are missed: ";
        for (String jar : jars) {
          msg += "\n    " + jar;
        }
        msg += "\n\nDownload jars from JetBrains site?";
        final int exitcode = Messages.showYesNoDialog(project, msg, "Information", Messages.getInformationIcon());
        if (exitcode == DialogWrapper.OK_EXIT_CODE) {
          if (!WSLibrarySynchronizer.downloadMissedJars(platform, project)) return;
        } else {
          return;
        }
      }
    }
    LocalVcs.getInstance(project).addLabel(GENERATE_JAVA_CODE_FROM_WSDL, "");

    String packagePrefix = (String) dialog.packagePrefix.getSelectedItem();
    if (packagePrefix != null) packagePrefix = packagePrefix.trim();
    WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();

    if (packagePrefix != null && packagePrefix.length() == 0) packagePrefix = null;
    if (packagePrefix != null) instance.addLastPackagePrefix(packagePrefix);

    final String wsdlUrl = ((String) dialog.wsdlUrl.getComboBox().getSelectedItem()).trim();
    instance.addLastWsdlUrl(wsdlUrl);

    final String packagePrefix1 = packagePrefix;

    final Function<Exception, Void> onException = new Function<Exception, Void>() {
      public Void fun(Exception e) {
        Messages.showErrorDialog(project, e.getMessage(), "Error");
        return null;
      }
    };

    try {
      final String outputPath = (String) dialog.outputPathes.getSelectedItem();
      final WSEngine currentWsEngine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(dialog.getWebServicePlatform());
      final Module currentModule = LibUtils.findModuleByOutputPath(project, outputPath);
      final String bindingType = dialog.getBindingType();

      final File savedWsdl;
      if (currentWsEngine instanceof RestWSEngine) {
        savedWsdl = new File( wsdlUrl.startsWith("file:/") ? wsdlUrl.substring(6) : wsdlUrl );
      } else {
        savedWsdl = LibUtils.saveSourceGeneratedFile(wsdlUrl, outputPath, packagePrefix);
      }

      final boolean serverSideGeneration = dialog.outputMode.getSelectedItem().equals(GenerateJavaFromWsdlDialog.SERVER_OUTPUT_MODE);

      final ExternalProcessHandler wsdlHandler = currentWsEngine.getGenerateJavaFromWsdlHandler(
        new WSEngine.GenerateJavaFromWsdlOptions() {
          public ExternalEngine.LibraryDescriptorContext getToolRunningContext() {
            return new ExternalEngine.LibraryDescriptorContext() {
              public boolean isForRunningGeneratedCode() {
                return false;
              }

              public String getBindingType() {
                return bindingType;
              }

              public Module getTargetModule() {
                return currentModule;
              }
            };
          }

          public String getPackagePrefix() {
            return packagePrefix1;
          }

          public String getWsdlUrl() {
            return wsdlUrl;
          }

          public String getOutputPath() {
            return outputPath;
          }

          public String getBindingType() {
            return bindingType;
          }

          public boolean isServersideSkeletonGeneration() {
            return serverSideGeneration;
          }

          public boolean isToGenerateTestCase() {
            return dialog.generateTestCase.isSelected();
          }

          public String getUser() {
            return dialog.user.getText();
          }

          public char[] getPassword() {
            return dialog.password.getPassword();
          }

          public boolean generateClassesForArrays() {
            return dialog.generateClassesForSchemaArrays.isSelected();
          }

          public String getTypeVersion() {
            return dialog.typeMappingVersion.getSelectedItem().toString();
          }

          public boolean isGenerateAllElements() {
            return dialog.generateUnreferencedElements.isSelected();
          }

          public boolean isSupportWrappedStyleOperation() {
            return dialog.wrappedDocumentSupport.isSelected();
          }

          public Project getProject() {
            return project;
          }

          public File getSavedWsdlFile() {
            return savedWsdl;
          }

          public Module getSelectedModule() {
            return currentModule;
          }

          public boolean generateNecessaryMappingCode() {
            return dialog.addLibs.isSelected();
          }

          public boolean useExtensions() {
            return dialog.useExtensions();
          }
        }
      );

      InvokeExternalCodeUtil.invokeExternalProcess2(
        wsdlHandler,
        project,
        new Runnable() {
          public void run() {
            if (dialog.addLibs.isSelected()) {
              EnableWebServicesSupportUtils.setupWebServicesInfrastructureForModule(
                new EnableWebServicesSupportUtils.EnableWebServicesSupportModel() {
                  @NotNull
                  public Module getModule() {
                    return currentModule;
                  }

                  public WSEngine getWsEngine() {
                    return currentWsEngine;
                  }

                  public boolean isServerSideSupport() {
                    return serverSideGeneration;
                  }

                  @Nullable
                  public String getBindingType() {
                    return bindingType;
                  }
                },
                project,
                false
              );
            }
            LibUtils.setupLibsForGeneratedCode(currentModule, currentWsEngine, "Wsdl2Java");
            LibUtils.doFileSystemRefresh();
            InvokeExternalCodeUtil.navigateToPackage(currentModule, packagePrefix1, outputPath);
            final Runnable runnable = dialog.getOnSuccess();
            if (runnable != null) runnable.run();
          }
        },
        onException,
        new Function<Void, Boolean>() {
          public Boolean fun(Void s) {
            return Boolean.TRUE;
          }
        },
        new Runnable() {
          public void run() {
            runAction(project, dialog, null, null);
          }
        }
      );
    } catch(Exception e) {
      onException.fun(e);
    }
  }

  static boolean isAcceptableFileForGenerateJavaFromWsdl(VirtualFile virtualFile) {
    final String extension = virtualFile.getExtension();
    return WSDL_EXTENSION.equals(extension) || WADL_EXTENSION.equals(extension);
  }

  public boolean isAcceptableFile(VirtualFile file) {
    return file.isDirectory() || isAcceptableFileForGenerateJavaFromWsdl(file);
  }
}
