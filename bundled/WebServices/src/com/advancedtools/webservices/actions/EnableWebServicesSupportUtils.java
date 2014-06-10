package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.actions.create.CreateWebServiceClientAction;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.jbossws.JBossWSEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.*;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.xfire.XFireWSEngine;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Maxim
 */
public class EnableWebServicesSupportUtils {
  @NonNls
  public static final String SIMPLE_WS_NAME = "HelloWorld";
  
  @NonNls
  public static final String SIMPLE_WS_CLIENT_NAME = "HelloWorldClient";
  
  @NonNls
  public static final String SIMPLE_WS_PACKAGE = "example";

  public static void setupWebServicesInfrastructureForModule(final EnableWebServicesSupportModel model, Project project, boolean doLabel) {
    final Module currentModule = model.getModule();

    if (doLabel) LocalVcs.getInstance(project).addLabel(WSBundle.message("enable.web.services.support.lvcslabel"), "");
    final WSEngine wsEngine = model.getWsEngine();

    LibUtils.setupLibsForGeneratedCode(currentModule,wsEngine,model.getBindingType());
    if (EnvironmentFacade.getInstance().isWebModule(currentModule)) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          EnvironmentFacade.getInstance().setupLibsForDeployment(currentModule, LibUtils.getGeneratedCodeLibInfos(wsEngine, model.getBindingType(), currentModule));
        }
      });
    }

    if (model.isServerSideSupport()) {
      wsEngine.doAdditionalWSServerSetup(currentModule);

      final VirtualFile result = FileUtils.findWebXml(currentModule);

      String deploymentServletName = wsEngine.getDeploymentServletName();
      if (result != null && deploymentServletName != null) {
        // update web.xml as needed
        DeployUtils.updateWebXml(project, result, deploymentServletName);
      }
    }
    
    LibUtils.doFileSystemRefresh();
    SoapUI.installSoapUI(project);    
  }

  public static void enableWebServicesServerSupport(final Module module, final WSEngine wsEngine) {
    setupWebServicesInfrastructureForModule(new EnableWebServicesSupportModel() {
      @NotNull
      public Module getModule() {
        return module;
      }

      public WSEngine getWsEngine() {
        return wsEngine;
      }

      public boolean isServerSideSupport() {
        return true;
      }

      @Nullable
      public String getBindingType() {
        return null;
      }
    }, module.getProject(), false);

    createSimpleCodeForServer(module, wsEngine);
  }

  public static void createSimpleCodeForServer(final Module module, final WSEngine wsEngine) {
    createCodeForServer(module, wsEngine, SIMPLE_WS_PACKAGE, SIMPLE_WS_NAME, Function.ID, null, null);
  }

  public static void createCodeForServer(final Module module, final WSEngine wsEngine, @NonNls String packageName,
                                         @NonNls String serviceName, final @NotNull Function<Exception, Void> onException,
                                         Runnable restartAction, Function<Void, Boolean> toRestart) {
    final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
    final List<VirtualFile> createdSourceFiles = new ArrayList<VirtualFile>(2);

    if (sourceRoots.length > 0) {
      @NonNls String interfaceName = null;

      @NonNls String text = buildDefaultJEEWebServiceText(packageName, serviceName);

      final String javaFileExtension = ".java";
      if (!wsEngine.supportsJaxWs2()) {
        if (wsEngine instanceof XFireWSEngine || wsEngine instanceof AxisWSEngine) {
          text = getFromFileTemplate(WebServicesPluginSettings.POJO_WEBSERVICE_TEMPLATE_NAME, packageName, serviceName);
        } else if (wsEngine instanceof RestWSEngine) {
          text = getFromFileTemplateWithCustomParameters(WebServicesPluginSettings.RESTFUL_WEBSERVICE_TEMPLATE_NAME,
            WebServicesPluginSettings.PACKAGE_NAME_TEMPLATE_ARG, packageName,
            WebServicesPluginSettings.CLASS_NAME_TEMPLATE_ARG, serviceName);
        } else {
          interfaceName = "I" + serviceName;
          text = getFromFileTemplateWithCustomParameters(
            WebServicesPluginSettings.J2EE1_4_WEBSERVICE_TEMPLATE_NAME, 
            WebServicesPluginSettings.PACKAGE_NAME_TEMPLATE_ARG, packageName, 
            WebServicesPluginSettings.CLASS_NAME_TEMPLATE_ARG, serviceName,
            WebServicesPluginSettings.INTERFACE_NAME_TEMPLATE_ARG, interfaceName
          );

          @NonNls String interfaceText = getFromFileTemplateWithCustomParameters(
            WebServicesPluginSettings.J2EE1_4_WEBSERVICE_INTERFACE_TEMPLATE_NAME, 
            WebServicesPluginSettings.PACKAGE_NAME_TEMPLATE_ARG, packageName, 
            WebServicesPluginSettings.INTERFACE_NAME_TEMPLATE_ARG, interfaceName
          );
          final VirtualFile interfaceFile = DeployUtils.addFileToModuleFromTemplate(
            module,
            new String[]{packageName, interfaceName + javaFileExtension},
            new StringBufferInputStream(interfaceText),
            false,
            true
          );

          assert interfaceFile != null;
          createdSourceFiles.add(interfaceFile);
        }
      }

      final VirtualFile virtualFile = DeployUtils.addFileToModuleFromTemplate(
        module,
        new String[]{packageName, serviceName + javaFileExtension},
        new StringBufferInputStream(text),
        false,
        true
      );

      assert virtualFile != null;
      createdSourceFiles.add(virtualFile);

      boolean useInterfaceName = interfaceName != null && wsEngine instanceof JBossWSEngine;
      compileAndRunDeployment(module, packageName + "."+((useInterfaceName)?interfaceName:serviceName), createdSourceFiles,
        wsEngine, new Processor<PsiClass>() {
        public boolean process(PsiClass psiClass) {
          psiClass.navigate(true);
          return true;
        }
      }, onException, restartAction, toRestart);
    }
  }

  public static void compileAndRunDeployment(final Module module, final String mainClassName, final List<VirtualFile> createdSourceFiles,
                                             final WSEngine wsEngine, final Processor<PsiClass> onSuccess,
                                             final @NotNull Function<Exception, Void> onException,final Runnable restart,
                                             final Function<Void, Boolean> canRestart) {
    final PsiManager psiManager = PsiManager.getInstance(module.getProject());
    final PsiClass clazz =
      EnvironmentFacade.getInstance().findClass(mainClassName, psiManager.getProject(), GlobalSearchScope.moduleScope(module));

    CompilerManager.getInstance(module.getProject()).compile(
        createdSourceFiles.toArray(new VirtualFile[createdSourceFiles.size()]),
      new CompileStatusNotification() {
        public void finished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
          if (!aborted && errors == 0) {
            try {
//              final Runnable restart1 = new Runnable() {
//                public void run() {
//                  compileAndRunDeployment(module, mainClassName, createdSourceFiles, wsEngine, onSuccess);
//                }
//              };
              continueDeployment(wsEngine, module, clazz, onSuccess, onException, restart, canRestart);
            } catch (InvokeExternalCodeUtil.ExternalCodeException ex) {
              Messages.showErrorDialog(module.getProject(), ex.getMessage(), "Error");
            }
          }
        }
      },
      false
    );
  }

  public static @NonNls String buildDefaultJEEWebServiceText(String packageName, String serviceName) {
    return getFromFileTemplate(WebServicesPluginSettings.JAXWS_WEBSERVICE_TEMPLATE_NAME, packageName, serviceName);
  }

  private static String getFromFileTemplate(String templateName, String packageName, String serviceName) {
    return getFromFileTemplateWithCustomParameters(templateName, WebServicesPluginSettings.PACKAGE_NAME_TEMPLATE_ARG, packageName, 
      WebServicesPluginSettings.CLASS_NAME_TEMPLATE_ARG, serviceName);
  }
  
  private static String getFromFileTemplateWithCustomParameters(String templateName, String... additionalParameters) {
    Properties properties = FileTemplateManager.getInstance().getDefaultProperties();
    
    if (additionalParameters != null) {
      for(int i = 0; i < additionalParameters.length; ++i) {
        String paramName = additionalParameters[i];
        if (i + 1 < additionalParameters.length) {
          ++i;
          properties.put(paramName, additionalParameters[i]);
        }
      }
    }
    return WebServicesPluginSettings.getInstance().getTemplateText(templateName, properties);
  }

  public static String getDefaultClientCode(String packageQName, String className) {
    return getFromFileTemplate(WebServicesPluginSettings.DEFAULT_WEBSERVICE_CLIENT_TEMPLATE_NAME, packageQName, className);
  }

  private static void continueDeployment(final WSEngine wsEngine, final Module module, final PsiClass clazz, final Processor<PsiClass> onSuccess,
                                         final Function<Exception, Void> onException,
                                         final Runnable restart, final Function<Void, Boolean> canRestart
                                         ) throws InvokeExternalCodeUtil.ExternalCodeException {
    final Runnable continueDeployment = new Runnable() {
      public void run() {
        wsEngine.deployWebService(new WSEngine.DeployWebServiceOptions() {
          public String getWsName() {
            return clazz.getName();
          }

          public String getWsClassName() {
            return clazz.getQualifiedName();
          }

          public String getWsNamespace() {
            final String packageName = ((PsiJavaFile) clazz.getContainingFile()).getPackageName();
            if (packageName.length() == 0) return "empty";
            else return "http://" + DeployWebServiceDialog.buildNSNameFromPackageText(packageName, wsEngine);
          }

          public String getUseOfItems() {
            return WSEngine.WS_USE_LITERAL;
          }

          public String getBindingStyle() {
            return WSEngine.WS_DOCUMENT_STYLE;
          }

          public PsiClass getWsClass() {
            return clazz;
          }
        }, module, new Runnable() {
          public void run() {
            LibUtils.doFileSystemRefresh();
            if (onSuccess != null) onSuccess.process(clazz);
          }
        }, onException, restart, canRestart);
      }
    };
    wsEngine.undeployWebService(clazz.getName(), module, continueDeployment, onException, restart, canRestart);
  }
  
  public static void enableWebServiceSupportForClient(final Module module, final WSEngine wsEngine) {
    setupWebServicesInfrastructureForModule(new EnableWebServicesSupportModel() {
      @NotNull
      public Module getModule() {
        return module;
      }

      public WSEngine getWsEngine() {
        return wsEngine;
      }

      public boolean isServerSideSupport() {
        return false;
      }

      @Nullable
      public String getBindingType() {
        return null;
      }
    }, module.getProject(), false);

    createSimpleCodeForClient(module, wsEngine);
  }

  public static void createSimpleCodeForClient(Module module, WSEngine engine) {
    createCodeForClient(module, engine, SIMPLE_WS_PACKAGE, SIMPLE_WS_CLIENT_NAME);
  }

  public static void createCodeForClient(final Module module, final WSEngine wsEngine, final String packageNameToCreate, final String classNameToCreate) {
    GenerateJavaFromWsdlAction.runAction(module.getProject(), null, module, new Runnable() {
      public void run() {
        final VirtualFile interfaceFile = DeployUtils.addFileToModuleFromTemplate(
          module,
          new String[]{packageNameToCreate, classNameToCreate + ".java"},
          new StringBufferInputStream(CreateWebServiceClientAction.getDefaultClientCode(packageNameToCreate, classNameToCreate)),
          false,
          true
        );

        if (interfaceFile == null) return;
        final Editor editor = FileEditorManager.getInstance(module.getProject()).openTextEditor(new OpenFileDescriptor(module.getProject(), interfaceFile), true);
        if (editor == null) return;
        CreateWebServiceClientAction.runTemplate(editor, wsEngine);
      }
    });
  }

  public static void ensureAnnotationsAreAllowedInJdkIfNeeded(@NotNull WSEngine wsEngine,@NotNull Module module) {
    if (wsEngine instanceof JWSDPWSEngine) {
      final EnvironmentFacade environmentFacade = EnvironmentFacade.getInstance();

      if (!environmentFacade.getEffectiveLanguageLevel(module).hasEnumKeywordAndAutoboxing()) {
        Sdk projectJdk = environmentFacade.getProjectJdkFromModule(module);
        boolean moduleJdk = projectJdk != null;
        
        if (projectJdk == null) {
          projectJdk = ProjectRootManager.getInstance(module.getProject()).getProjectJdk();
        }

        if (projectJdk != null) {
          if (moduleJdk) environmentFacade.setModuleLanguageLevel(module, LanguageLevel.JDK_1_5);
          else environmentFacade.setLanguageLevel(module.getProject(),LanguageLevel.JDK_1_5);
        }
      }
    }
  }

  public interface EnableWebServicesSupportModel {
    @NotNull
    Module getModule();
    WSEngine getWsEngine();
    boolean isServerSideSupport();

    @Nullable
    String getBindingType();
  }
}
