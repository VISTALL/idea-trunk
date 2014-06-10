package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.utils.FileUtils;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Function;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @by maxim
 */
public class GenerateInstanceDocumentFromSchemaAction extends BaseWSFromFileAction {
  public void actionPerformed(AnActionEvent event) {
    DataContext dataContext = event.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    doShowDialog(project, null);
  }

  private void doShowDialog(final Project project, @Nullable GenerateInstanceDocumentFromSchemaDialog previousDialog) {
    final GenerateInstanceDocumentFromSchemaDialog dialog = new GenerateInstanceDocumentFromSchemaDialog(
      project, previousDialog
    );

    dialog.setOkAction(new Runnable() {
      public void run() {
        doAction(project, dialog);
      }
    });

    dialog.show();
  }

  private void doAction(final Project project, final GenerateInstanceDocumentFromSchemaDialog dialog) {
    FileDocumentManager.getInstance().saveAllDocuments();
    LocalVcs.getInstance(project).addLabel(WSBundle.message("generate.instance.document.from.schema.lvcslabel"), "");
    @NonNls List<String> parameters = new LinkedList<String>();

    final VirtualFile relativeFile = dialog.findVirtualFileFromUrl();
    VirtualFile baseDirForCreatedInstanceDocument = relativeFile.getParent();
    Module moduleForFile = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(relativeFile);

    if (!dialog.enableRestrictionCheck()) {
      parameters.add("-nopvr");
    }

    if (!dialog.enableUniquenessCheck()) {
      parameters.add("-noupa");
    }

    final String[] result = new String[1];

    final Function<Exception, Void> actionAtFailure = new Function<Exception, Void>() {
      public Void fun(Exception s) {
        Messages.showErrorDialog(project, result[0] == null ? s.getLocalizedMessage():result[0], WSBundle.message("instance.to.schema.generator.error"));
        return null;
      }
    };

    String pathToUse;

    try {
      final File tempDir = FileUtils.createTempDir("xsd2inst");

      if (relativeFile.getFileSystem() instanceof JarFileSystem) {
        baseDirForCreatedInstanceDocument = JarFileSystem.getInstance().getVirtualFileForJar(relativeFile).getParent();
      }

      pathToUse = tempDir.getPath() + File.separatorChar + Xsd2InstanceUtils.processAndSaveAllSchemas(
        (XmlFile) PsiManager.getInstance(project).findFile(relativeFile),
        new THashMap<String, String>(),
        new Xsd2InstanceUtils.SchemaReferenceProcessor() {
          public void processSchema(String schemaFileName, String schemaContent) {
            try {
              final String fullFileName = tempDir.getPath() + File.separatorChar + schemaFileName;
              FileUtils.saveStreamContentAsFile(
                fullFileName,
                new StringBufferInputStream(schemaContent)
              );
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        }
      );
    } catch (IOException e) {
      actionAtFailure.fun(e);
      return;
    }

    parameters.add(pathToUse);
    WebServicesPluginSettings.getInstance().addLastXmlBeansUrl(relativeFile.getPath());

    parameters.add("-name");
    parameters.add(dialog.getElementName());

    final InvokeExternalCodeUtil.JavaExternalProcessHandler handler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      WSBundle.message("schema.generator.process.title"),
      "org.apache.xmlbeans.impl.xsd2inst.SchemaInstanceGenerator",
      LibUtils.getLibUrlsForToolRunning(
        WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(XmlBeansMappingEngine.XML_BEANS_2_ENGINE),
        moduleForFile
      ),
      parameters.toArray(new String[parameters.size()]),
      moduleForFile,
      true
    );

    handler.setOutputConsumer(new InvokeExternalCodeUtil.OutputConsumer() {
      public boolean handle(String output, String errOutput) throws InvokeExternalCodeUtil.ExternalCodeException {
        result[0] = output.trim();
        if (!result[0].startsWith("<")) throw new InvokeExternalCodeUtil.ExternalCodeException(result[0]);
        return true;
      }
    });

    final VirtualFile baseDirForCreatedInstanceDocument1 = baseDirForCreatedInstanceDocument;
    InvokeExternalCodeUtil.invokeExternalProcess2(
      handler,
      project,
      new Runnable() {
        public void run() {
          try {
            if (result[0] != null && result[0].startsWith("<")) {

              String xmlFileName = baseDirForCreatedInstanceDocument1.getPath() + File.separatorChar + dialog.getOutputFileName();
              FileOutputStream fileOutputStream = null;

              try {
                fileOutputStream = new FileOutputStream(xmlFileName);
                fileOutputStream.write(result[0].getBytes());
                fileOutputStream.close();
                fileOutputStream = null;
              } finally {
                if (fileOutputStream != null) {
                  try {
                    fileOutputStream.close();
                  } catch(IOException ex) {}
                }
              }

              LibUtils.doFileSystemRefresh();
              final File xmlFile = new File(xmlFileName);
              VirtualFile virtualFile = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
                public VirtualFile compute() {
                  return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(xmlFile);
                }
              });
              FileEditorManager.getInstance(project).openTextEditor(
                EnvironmentFacade.getInstance().createOpenFileDescriptor(virtualFile, project), true
              );
            } else {
              actionAtFailure.fun(new InvokeExternalCodeUtil.ExternalCodeException(result[0]));
            }
          } catch (IOException e) {
            actionAtFailure.fun(e);
          }
        }
      },
      actionAtFailure,
      new Function<Void, Boolean>() {
        public Boolean fun(Void s) {
          return dialog.areCurrentParametersStillValid();
        }
      },
      new Runnable() {
        public void run() {
          doShowDialog(project, dialog);
        }
      }
    );
  }

  static boolean isAcceptableFileForGenerateSchemaFromInstanceDocument(VirtualFile virtualFile) {
    return virtualFile != null && WebServicesPluginSettings.XSD_FILE_EXTENSION.equals(virtualFile.getExtension());
  }

  public boolean isAcceptableFile(VirtualFile file) {
    return isAcceptableFileForGenerateSchemaFromInstanceDocument(file);
  }
}
