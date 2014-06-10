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

package com.advancedtools.webservices.actions.create;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.actions.EnableWebServicesSupportUtils;
import com.advancedtools.webservices.actions.GenerateJavaFromWsdlAction;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.BaseWSGenerateAction;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public class CreateWebServiceClientAction extends CreateWSActionBase {
  public CreateWebServiceClientAction() {
    super(
      "create.webservice.client.action.text",
      "create.webservice.client.action.description",
      IconLoader.findIcon("/javaee/WebServiceClient.png")
    );
  }

  protected String getKindName() {
    return WSBundle.message("webserviceclient.create.action.name");
  }

  static final @NonNls String marker = "// Please, do not remove this line from file template, here invocation of web service will be inserted";

  protected String buildText(String packageQName, String className) {
    return getDefaultClientCode(packageQName, className);
  }

  public static String getDefaultClientCode(String packageQName, String className) {
    return EnableWebServicesSupportUtils.getDefaultClientCode(packageQName, className);
  }

  protected void createAdditionalFiles(String className, String packageQName, PsiDirectory psiDirectory, final Editor editor, VirtualFile vfile) throws Exception {
    final VirtualFile file = psiDirectory.getVirtualFile();
    final Project project = psiDirectory.getProject();
    GenerateJavaFromWsdlAction.runAction(
      project,
      null,
      ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(file),
      new Runnable() {
        public void run() {
          runTemplate(editor, WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(JWSDPWSEngine.JWSDP_PLATFORM));
        }
      }
    );
  }

  public static void runTemplate(final @NotNull Editor editor,@NotNull WSEngine wsEngine) {
    LibUtils.doFileSystemRefresh();

    final String actionName;

    if (wsEngine instanceof AxisWSEngine) actionName = "WebServicesPlugin.GenerateAxisWSCall";
    else if (wsEngine instanceof RestWSEngine) actionName = "WebServicesPlugin.GenerateRestWSCall";
    else actionName = "WebServicesPlugin.GenerateJWSDPWSCall";

    final AnAction action = ActionManager.getInstance().getAction(actionName);
    final int i = editor.getDocument().getText().indexOf(marker);
    assert i != -1: "Web Services Client template does not have insertion point " + marker;
    editor.getCaretModel().moveToOffset(i);
    
    CommandProcessor.getInstance().runUndoTransparentAction(
      new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              editor.getDocument().replaceString(i, i + marker.length(), "");
            }
          });
        }
      }
    );
    
    ((BaseWSGenerateAction)action).run(editor, editor.getProject());
  }

  protected void modifyText(Editor editor) {
    final AnAction action = ActionManager.getInstance().getAction("WebServicesPlugin.GenerateJWSDPWSCall");
    ((BaseWSGenerateAction)action).run(editor, editor.getProject());
  }
}
