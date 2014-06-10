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

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.*;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @by Maxim.Mossienko
 */
public class DeployWebServiceAction extends BaseWSFromFileAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    final DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    final PsiClass classFromDataContext = DeployUtils.getCurrentClassFromDataContext(dataContext);

    runAction(project, classFromDataContext, null);
  }

  private void runAction(final Project project, PsiClass classFromDataContext, @Nullable DeployWebServiceDialog previousDialog) {
    final DeployWebServiceDialog dialog = new DeployWebServiceDialog(
      project,
      classFromDataContext,
      previousDialog
    );

    dialog.show();

    dialog.setOkAction(new Runnable() {
      public void run() {
        runDeployment();
      }

      private void runDeployment() {
        LocalVcs.getInstance(project).addLabel(WSBundle.message("expose.class.as.web.service.lvcs.label"), "");
        final WSEngine currentEngine = dialog.getCurrentEngine();

        final Function<Exception, Void> onException = new Function<Exception, Void>() {
          public Void fun(Exception e) {
            Messages.showErrorDialog(project, e.getMessage(), "Error");
            return null;
          }
        };

        final Runnable restartAction = new Runnable() {
          public void run() {
            runAction(project, null, dialog);
          }
        };

        final Runnable onSuccessAfterServiceDeployment = new Runnable() {
          public void run() {
            continueDeployment(currentEngine, onException);
          }
        };

        final Runnable deploymentAction = new Runnable() {
          public void run() {
            currentEngine.deployWebService(new WSEngine.DeployWebServiceOptions() {
              public String getWsName() {
                return dialog.getWSName();
              }

              public String getWsClassName() {
                return dialog.getWSClassName();
              }

              public String getWsNamespace() {
                return dialog.getWsNamespace();
              }

              public String getBindingStyle() {
                return dialog.getBindingStyle();
              }

              public PsiClass getWsClass() {
                return dialog.getCurrentClass();
              }

              public String getUseOfItems() {
                return dialog.getUseOfItems();
              }
            }, dialog.getSelectedModule(), onSuccessAfterServiceDeployment, onException, restartAction, null);
          }
        };

        currentEngine.undeployWebService(dialog.getWSName(), dialog.getSelectedModule(), new Runnable() {
          public void run() {
            deploymentAction.run();
          }
        }, onException, restartAction, null);
      }

      private void continueDeployment(WSEngine currentEngine, Function<Exception, Void> onException) {
        if (dialog.isToAddLibs()) {
          EnableWebServicesSupportUtils.setupWebServicesInfrastructureForModule(new EnableWebServicesSupportUtils.EnableWebServicesSupportModel() {
            @NotNull
            public Module getModule() {
              return dialog.getSelectedModule();
            }

            @NotNull
            public WSEngine getWsEngine() {
              return dialog.getCurrentEngine();
            }

            public boolean isServerSideSupport() {
              return true;
            }

            @Nullable
            public String getBindingType() {
              return null;
            }
          }, project, false);
        }

        final Module module = dialog.getSelectedModule();
        final String[] classPathEntries = ArrayUtil.mergeArrays(
          LibUtils.getLibUrlsForToolRunning(dialog.getCurrentEngine(), module),
          InvokeExternalCodeUtil.buildClasspathStringsForModule(module),
          String.class
        );

        final String basePluginRTLib = LibUtils.detectPluginPath() + "/lib/rt/";
        final String servletName = currentEngine.getDeploymentServletName();
        final String[] servletClassResult = new String[1];

        if (servletName != null) {
          try {
            String s = DeployUtils.loadWSWebXml(servletName);
            PsiFile psiFile = EnvironmentFacade.getInstance().createFileFromText("s.xml", s, project);

            psiFile.acceptChildren(new XmlRecursiveElementVisitor() {
              public void visitXmlTag(XmlTag xmlTag) {
                if (xmlTag.getName().equals("servlet-class") && servletClassResult[0] == null) {
                  servletClassResult[0] = LibUtils.getStringValue(xmlTag);
                }
                if (servletClassResult[0] == null) super.visitXmlTag(xmlTag);
              }
            });
          } catch(Exception e) {
            onException.fun(e);
          }
        }

        if (servletClassResult[0] != null && false)
          someTestMethod(module, classPathEntries, basePluginRTLib, servletClassResult);

        LibUtils.doFileSystemRefresh();
      }
    });
  }

  private void someTestMethod(final Module module, final String[] classPathEntries, final String basePluginRTLib, final String[] servletClassResult) {
    Runnable runnable = new Runnable() {
      public void run() {
        try {
          final String [] params = new String[4];
          params[0] = "8081";
          params[1] = "";
          params[2] = servletClassResult[0];
          params[3] = WebServicesPluginSettings.getInstance().getWebServicesUrlPathPrefix();

          InvokeExternalCodeUtil.doInvoke(
            new InvokeExternalCodeUtil.JavaExternalProcessHandler(
              "Generate XFire Wsdl",
              "com.advancedtools.webservices.rt.embedded.WebServerHandler",
              ArrayUtil.mergeArrays(
                classPathEntries,
                new String[] {
                  basePluginRTLib + "WebServicesRT.jar",
                  //basePluginRTLib + "jetty-6.1.1/jetty-6.1.1.jar",
                  //basePluginRTLib + "jetty-6.1.1/jetty-util-6.1.1.jar",
                  //basePluginRTLib + "jetty-6.1.1/servlet-api-2.5-6.1.1.jar",
                },
                String.class
              ),
              servletClassResult,
              module,
              false
            )
          );
        } catch (InvokeExternalCodeUtil.ExternalCodeException e) {
          e.printStackTrace();
        }
      }
    };

    EnvironmentFacade.getInstance().executeOnPooledThread(runnable);
  }
}
