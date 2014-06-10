package com.advancedtools.webservices.actions.create;

import com.advancedtools.webservices.actions.EnableWebServicesSupportUtils;
import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Maxim
 */
public class CreateWebServiceAction extends CreateWSActionBase {
  public CreateWebServiceAction() {
    super(
      "create.webservice.action.text",
      "create.webservice.action.description",
      IconLoader.findIcon("/javaee/WebService.png")
    );
  }     

  protected String getKindName() {
    return WSBundle.message("webservice.create.action.name");
  }

  protected String buildText(String packageQName, String className) {
    return EnableWebServicesSupportUtils.buildDefaultJEEWebServiceText(
      packageQName,
      className
    );
  }
  
  protected void createAdditionalFiles(String className, String packageQName, PsiDirectory psiDirectory, final Editor editor, VirtualFile vfile) throws Exception {
    final Module module = ProjectRootManager.getInstance(psiDirectory.getProject()).getFileIndex().getModuleForFile(psiDirectory.getVirtualFile());
    assert module != null;
    final WSEngine engine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(JWSDPWSEngine.JWSDP_PLATFORM);
    
    EnableWebServicesSupportUtils.setupWebServicesInfrastructureForModule(new EnableWebServicesSupportUtils.EnableWebServicesSupportModel() {
      @NotNull
      public Module getModule() {
        return module;
      }

      public WSEngine getWsEngine() {
        return engine;
      }

      public boolean isServerSideSupport() {
        return true;
      }

      @Nullable
      public String getBindingType() {
        return null;
      }
    }, module.getProject(), false);

    List<VirtualFile> filesToCompile = new ArrayList<VirtualFile>();
    filesToCompile.add(vfile);
    EnableWebServicesSupportUtils.compileAndRunDeployment(
      module, packageQName.length() > 0 ? packageQName + "." + className:className, filesToCompile, engine, null, new Function<Exception, Void>() {
      public Void fun(Exception e) {
        e.printStackTrace();
        return null;
      }
    },
      null, null);
  }
}
