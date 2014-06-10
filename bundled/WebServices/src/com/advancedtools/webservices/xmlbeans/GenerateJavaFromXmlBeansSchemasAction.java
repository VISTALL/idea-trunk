package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @by maxim
 */
public class GenerateJavaFromXmlBeansSchemasAction extends BaseWSFromFileAction {
  static final @NonNls String JAR_FILE_EXTENSION = "jar";

  public void actionPerformed(AnActionEvent event) {
    DataContext dataContext = event.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    runAction(project, null);
  }

  private void runAction(final Project project, @Nullable GenerateJavaFromXmlBeansSchemasDialog previousDialog) {
    final GenerateJavaFromXmlBeansSchemasDialog dialog = new GenerateJavaFromXmlBeansSchemasDialog(
      project, previousDialog
    );

    dialog.setOkAction(new Runnable() {
      public void run() {
        doAction(project, dialog);
      }
    });

    dialog.show();
  }

  private void doAction(final Project project, final GenerateJavaFromXmlBeansSchemasDialog dialog) {
    ApplicationManager.getApplication().saveAll();
    LocalVcs.getInstance(project).addLabel(WSBundle.message("generate.java.code.from.xmlbeans.schemas.lvcs.action"), "");

    String url = (String) dialog.getUrl().getComboBox().getSelectedItem();

    final boolean addToLibs = dialog.getAddLibs().isSelected();
    final String completeFileName = dialog.getOutputFileName();
    final ExternalEngine xmlBeansEngine = WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(XmlBeansMappingEngine.XML_BEANS_2_ENGINE);

    VirtualFile schemaFile = EnvironmentFacade.getInstance().findRelativeFile(url, null);
    Module moduleForFile = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(schemaFile);
    if (moduleForFile == null) moduleForFile = ModuleManager.getInstance(project).getModules()[0];
    if (url.startsWith(LibUtils.FILE_URL_PREFIX)) url = url.substring(LibUtils.FILE_URL_PREFIX.length());

    final Module moduleForFile1 = moduleForFile;
    final String url1 = url;
    XmlBeansMappingEngine.doXmlBeanGen(
      url,
      completeFileName,
      LibUtils.getLibUrlsForToolRunning(
        xmlBeansEngine,
        moduleForFile
      ),
      moduleForFile,
      addToLibs,
      new Runnable() {
        public void run() {
          LibUtils.setupLibsForGeneratedCode(moduleForFile1, xmlBeansEngine, null);
          LibUtils.doFileSystemRefresh();
          final VirtualFile inputFile = EnvironmentFacade.getInstance().findRelativeFile(url1, null);

          if (inputFile != null) {
            final VirtualFile outputFile = EnvironmentFacade.getInstance().findRelativeFile(completeFileName, inputFile.getParent());

            if (outputFile != null) {
              new OpenFileDescriptor(project, outputFile).navigate(true);
            }
          }
        }
      },
      new Runnable() {
        public void run() {
          runAction(project, dialog);
        }
      }
    );
  }

  public boolean isAcceptableFile(VirtualFile file) {
    return isAcceptableFileForJavaFromXmlBeans(file);
  }

  static boolean isAcceptableFileForJavaFromXmlBeans(@NotNull VirtualFile virtualFile) {
    final String extension = virtualFile.getExtension();
    return WebServicesPluginSettings.XSD_FILE_EXTENSION.equals(extension) ||
      WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(extension) ||
      JAR_FILE_EXTENSION.equals(extension);
  }
}
