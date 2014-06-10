/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.advancedtools.webservices.jaxb;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.utils.DeployUtils;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.util.Function;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @by maxim
 */
public class GenerateJAXBSchemasFromJavaAction extends BaseWSFromFileAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    final PsiClass classFromDataContext = DeployUtils.getCurrentClassFromDataContext(dataContext);

    doAction(project, classFromDataContext, null);
  }

  private void doAction(final Project project, PsiClass classFromDataContext, GenerateJAXBSchemasFromJavaDialog previousDialog) {
    final GenerateJAXBSchemasFromJavaDialog dialog = new GenerateJAXBSchemasFromJavaDialog(
      project,
      classFromDataContext,
      previousDialog
    );

    dialog.setOkAction(
      new Runnable() {
        public void run() {
          doAction(project, dialog);
        }
      }
    );

    dialog.show();
  }

  private void doAction(final Project project, final GenerateJAXBSchemasFromJavaDialog dialog) {
    LocalVcs.getInstance(project).addLabel(WSBundle.message("generate.jaxb.schemas.from.java.lvcs.title"), "");
    final List<String> parameters = new LinkedList<String>();

    final PsiFile file = dialog.getCurrentClass().getContainingFile();
    Module moduleForFile = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(
      file.getVirtualFile()
    );

    parameters.add("-cp");
    parameters.add(InvokeExternalCodeUtil.buildClasspathForModule(moduleForFile));
    parameters.add("-d");
    final String outputPath = file.getContainingDirectory().getVirtualFile().getPath();
    parameters.add(outputPath);

    final PsiClass currentClass = dialog.getCurrentClass();

    if (dialog.toIncludeTypesOfMethods()) {
      DeployUtils.processClassMethods(dialog.getSelectedMethods(), new DeployUtils.DeploymentProcessor() {
        public void processMethod(PsiMethod method, String problem, List<String> nonelementaryTypes) {
          parameters.addAll(nonelementaryTypes);
        }
      });
    }
    parameters.add(currentClass.getQualifiedName());


    ExternalEngine engine = WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(JaxbMappingEngine.JAXB_2_ENGINE);

    final Function<Exception, Void> atFailure = new Function<Exception, Void>() {
      public Void fun(Exception e) {
        Messages.showErrorDialog(project, e.getMessage(), "Schema gen error");
        e.printStackTrace();
        return null;
      }
    };

    final String basePath = engine.getBasePath();

    final InvokeExternalCodeUtil.JavaExternalProcessHandler processHandler = new InvokeExternalCodeUtil.JavaExternalProcessHandler(
      "JXC",
      basePath != null ?
        "com.sun.tools.jxc.SchemaGeneratorFacade" :
        "com.sun.tools.internal.jxc.SchemaGenerator",
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
          LibUtils.doFileSystemRefresh();

          final File tempFile = new File(outputPath + File.separator + "schema1.xsd");

          if (tempFile.exists()) {
            VirtualFile virtualFile = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
              public VirtualFile compute() {
                return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile);
              }
            });
            FileEditorManager.getInstance(project).openTextEditor(
              EnvironmentFacade.getInstance().createOpenFileDescriptor(virtualFile, project), true
            );
          } else {
            atFailure.fun( new InvokeExternalCodeUtil.ExternalCodeException("schema1.xsd is missing") );
          }
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
          doAction(project, null, dialog);
        }
      }
    );
  }
}
