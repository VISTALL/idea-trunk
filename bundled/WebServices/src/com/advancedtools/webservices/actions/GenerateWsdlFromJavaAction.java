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

package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.utils.*;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.wsengine.WSEngine;
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
import com.intellij.psi.PsiMethod;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @by maxim
 */
public class GenerateWsdlFromJavaAction extends BaseWSFromFileAction {
  @Override
  public boolean isAcceptableFile(VirtualFile file) {
    return super.isAcceptableFile(file);
  }

  public void actionPerformed(AnActionEvent anActionEvent) {
    DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    final PsiClass classFromDataContext = DeployUtils.getCurrentClassFromDataContext(dataContext);

    invokeDialog(project, classFromDataContext, null);
  }

  private void invokeDialog(final Project project, PsiClass classFromDataContext, @Nullable GenerateWsdlFromJavaDialog previousDialog) {
    final GenerateWsdlFromJavaDialog dialog = new GenerateWsdlFromJavaDialog(
      project,
      classFromDataContext,
      previousDialog
    );

    dialog.setOkAction(
      new Runnable() {
        public void run() {
          doAction(
            dialog,
            project
          );
        }
      }
    );

    dialog.show();
  }

  private void doAction(final GenerateWsdlFromJavaDialog dialog, final Project project) {
    LocalVcs.getInstance(project).addLabel("Generate Wsdl from Java", "");

    final WSEngine wsEngine = dialog.getCurrentWsEngine();
    final Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(
      dialog.getCurrentClass().getContainingFile().getVirtualFile()
    );
    final String[] classPathEntries = ArrayUtil.mergeArrays(
      LibUtils.getLibUrlsForToolRunning(wsEngine, module),
      InvokeExternalCodeUtil.buildClasspathStringsForModule(module),
      String.class
    );
    final PsiClass psiClass = dialog.getCurrentClass();


    WSEngine.GenerateWsdlFromJavaOptions options = new MyGenerateWsdlFromJavaOptions(psiClass, dialog, module, classPathEntries);

    wsEngine.generateWsdlFromJava(
      options,
      new Function<File, Void>() {
        public Void fun(File tempFile) {
          if (tempFile != null && tempFile.exists()) {
            LibUtils.doFileSystemRefresh();
            final File tempFile1 = tempFile;

            VirtualFile virtualFile = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
              public VirtualFile compute() {
                return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile1);
              }
            });
            FileEditorManager.getInstance(project).openTextEditor(
              EnvironmentFacade.getInstance().createOpenFileDescriptor(virtualFile, project), true
            );
          } else {
            Messages.showMessageDialog(project, "There is error in launching java 2 wsdl, please, send the exception to plugin author", "Internal Error", Messages.getErrorIcon());
          }
          return null;
        }
      },
      new Function<Exception, Void>() {
        public Void fun(Exception e) {
          Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
          return null;
        }
      },
      new Runnable() {
        public void run() {
          invokeDialog(
            dialog.getProject(),
            dialog.getCurrentClass(),
            dialog
          );
        }
      }
    );
  }

  private static class MyGenerateWsdlFromJavaOptions implements WSEngine.GenerateWsdlFromJavaOptions, Function<Void, Boolean> {
    private final PsiClass psiClass;
    private final GenerateWsdlFromJavaDialog dialog;
    private final Module module;
    private final String[] classPathEntries;

    public MyGenerateWsdlFromJavaOptions(PsiClass psiClass, GenerateWsdlFromJavaDialog dialog, Module module, String[] classPathEntries) {
      this.psiClass = psiClass;
      this.dialog = dialog;
      this.module = module;
      this.classPathEntries = classPathEntries;
    }

    public PsiClass getClassForOperation() {
      return psiClass;
    }

    public String getTypeMappingVersion() {
      return dialog.getTypeMappingVersion();
    }

    public String getSoapAction() {
      return dialog.getSoapAction();
    }

    public String getBindingStyle() {
      return dialog.getBindingStyle();
    }

    public String getUseOfItems() {
      return dialog.getUseItemsInBindings();
    }

    public String getGenerationType() {
      return dialog.getGenerationType();
    }

    public String getMethods() {
      final PsiMethod[] selectedMethods = dialog.getSelectedMethods();
      StringBuilder builder = new StringBuilder(selectedMethods.length * 8);

      for(PsiMethod m:selectedMethods) {
        if (builder.length() > 0) builder.append(',');
        builder.append(m.getName());
      }
      return builder.toString();
    }

    public Module getModule() {
      return module;
    }

    public String getWebServiceNamespace() {
      return dialog.getWebServiceNamespace().getText();
    }

    public String getWebServiceURL() {
      return dialog.getWebServiceURL().getText();
    }

    public String[] getClassPathEntries() {
      return classPathEntries;
    }

    public Function<Void, Boolean> isParametersStillValidPredicate() {
      return this;
    }

    public Runnable getSuccessRunnable(final Function<File, Void> successAction, final File file) {
      return new Runnable() {
        public void run() {
          successAction.fun(file);
        }
      };
    }

    public Boolean fun(Void s) {
      return getClassForOperation().isValid() ? Boolean.TRUE: Boolean.FALSE;
    }
  }
}
