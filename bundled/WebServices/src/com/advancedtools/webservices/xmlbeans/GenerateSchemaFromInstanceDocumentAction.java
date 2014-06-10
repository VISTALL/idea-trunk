package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @by maxim
 */
public class GenerateSchemaFromInstanceDocumentAction extends BaseWSFromFileAction {
  private static final Logger LOG = Logger.getInstance("webservicesplugin.xmlbeans.schema.instance");
  private static final String INSTANCE_2_SCHEMA_GENERATOR_ERROR = WSBundle.message("instance.2.schema.generator.error");

  public void actionPerformed(AnActionEvent event) {
    DataContext dataContext = event.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    showDialog(project, null);
  }

  private void showDialog(final Project project, @Nullable GenerateSchemaFromInstanceDocumentDialog previousDialog) {
    final GenerateSchemaFromInstanceDocumentDialog dialog = new GenerateSchemaFromInstanceDocumentDialog(
      project, previousDialog
    );

    dialog.setOkAction(new Runnable() {
      public void run() {
        doAction(project, dialog);
      }
    });

    dialog.show();
  }

  private void doAction(final Project project, final GenerateSchemaFromInstanceDocumentDialog dialog) {
    FileDocumentManager.getInstance().saveAllDocuments();
    LocalVcs.getInstance(project).addLabel(WSBundle.message("generate.schema.from.instance.xml.document.lvcslabel"), "");
    @NonNls List<String> parameters = new LinkedList<String>();

    final String url = (String) dialog.getUrl().getComboBox().getSelectedItem();
    final StringTokenizer tokenizer = new StringTokenizer(url, dialog.SEPARATOR_CHAR);
    String currentUrl = tokenizer.nextToken();

    VirtualFile relativeFile = EnvironmentFacade.getInstance().findRelativeFile(currentUrl, null);
    Module moduleForFile = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(relativeFile);

    parameters.add("-design");

    final String designType = dialog.getDesignType();
    if (GenerateSchemaFromInstanceDocumentDialog.LOCAL_ELEMENTS_GLOBAL_COMPLEX_TYPES.equals(designType)) {
      parameters.add("vb");
    } else if (GenerateSchemaFromInstanceDocumentDialog.GLOBAL_ELEMENTS_LOCAL_TYPES.equals(designType)) {
      parameters.add("ss");
    } else {
      parameters.add("rd");
    }

    parameters.add("-simple-content-types");
    if (GenerateSchemaFromInstanceDocumentDialog.SMART_TYPE.equals(dialog.getSimpleContentType())) {
      parameters.add("smart");
    } else {
      parameters.add("string");
    }

    parameters.add("-enumerations");
    String enumerationsLimit = dialog.getEnumerationsLimit();
    int enumLimit = Integer.parseInt(enumerationsLimit);

    if (enumLimit == 0) {
      parameters.add("never");
    } else {
      parameters.add(enumerationsLimit);
    }

    parameters.add("-outDir");
    parameters.add(relativeFile.getParent().getPath());

    final File expectedSchemaFile = new File(relativeFile.getParent().getPath() + File.separator + relativeFile.getName() + "0.xsd");
    expectedSchemaFile.delete();
    parameters.add("-outPrefix");
    parameters.add(relativeFile.getName());

    while(currentUrl != null) {
      if (currentUrl.startsWith(LibUtils.FILE_URL_PREFIX)) currentUrl = currentUrl.substring(LibUtils.FILE_URL_PREFIX.length());
      parameters.add(currentUrl);
      currentUrl = tokenizer.hasMoreTokens() ? tokenizer.nextToken():null;
    }

    final InvokeExternalCodeUtil.JavaExternalProcessHandler handler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      WSBundle.message("instance.2.xsd.dialog.title"),
      "org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd",
      LibUtils.getLibUrlsForToolRunning(
        WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(XmlBeansMappingEngine.XML_BEANS_2_ENGINE),
        moduleForFile
      ),
      parameters.toArray(new String[parameters.size()]),
      moduleForFile,
      true
    );

    final String[] result = new String[1];
    handler.setOutputConsumer(new InvokeExternalCodeUtil.OutputConsumer() {
      public boolean handle(String output, String errOutput) {
        result[0] = output.trim();
        return true;
      }
    });

    final Function<Exception, Void> actionAtFailure = new Function<Exception, Void>() { // on exception

      public Void fun(Exception e) {
        Messages.showErrorDialog(project, e.getMessage(), INSTANCE_2_SCHEMA_GENERATOR_ERROR);
        LOG.debug(e);
        return null;
      }
    };
    InvokeExternalCodeUtil.invokeExternalProcess2(
      handler,
      project,
      new Runnable() {
        public void run() {
          if (expectedSchemaFile.exists()) {
            String pathname = dialog.getTargetSchemaName();
            final File dest = new File(expectedSchemaFile.getParentFile(),pathname);
            if (dest.exists()) dest.delete();
            expectedSchemaFile.renameTo(dest);

            LibUtils.doFileSystemRefresh();
            VirtualFile virtualFile = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
              public VirtualFile compute() {
                return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dest);
              }
            });
            FileEditorManager.getInstance(project).openTextEditor(
              EnvironmentFacade.getInstance().createOpenFileDescriptor(virtualFile, project), true
            );
          } else {
            actionAtFailure.fun(new InvokeExternalCodeUtil.ExternalCodeException(result[0]));
          }
        }
      },
      actionAtFailure,
      new Function<Void, Boolean>() {
        public Boolean fun(Void s) {
          return dialog.checkParametersAreStillValid();
        }
      },
      new Runnable() {
        public void run() {
          showDialog(project, dialog);
        }
      }
    );
  }

  static boolean isAcceptableFileForGenerateSchemaFromInstanceDocument(VirtualFile virtualFile) {
    return virtualFile != null && WebServicesPluginSettings.XML_FILE_EXTENSION.equals(virtualFile.getExtension());
  }

  public boolean isAcceptableFile(VirtualFile file) {
    return isAcceptableFileForGenerateSchemaFromInstanceDocument(file);
  }
}
