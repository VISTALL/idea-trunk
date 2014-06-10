package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.utils.FileUtils;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.LibUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.Function;
import com.intellij.util.NullableFunction;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @by Konstantin Bulenkov
 */
public class GenerateWadlFromJavaAction extends BaseWSFromFileAction {
  @NonNls private static final String WADL_EXT = ".wadl";
  @NonNls private static final String GEN_WADL_FQN = "com.advancedtools.webservices.rest.GenerateWadlFromJava";


  @Override
  public void update(final AnActionEvent e) {
    Project project = DataKeys.PROJECT.getData(e.getDataContext());
    Module module = e.getData(DataKeys.MODULE);
    e.getPresentation().setEnabled(module != null && project != null);
  }

  public void actionPerformed(final AnActionEvent e) {
    final Module module = e.getData(DataKeys.MODULE);
    if (module == null) return;
    final GenerateWadlFromJavaDialog mainPanel = new GenerateWadlFromJavaDialog();
    mainPanel.setBaseURI(WebServicesPluginSettings.getInstance().getLastRestClientHost());
    VirtualFile file = FileUtils.findWebInf(module);

    if (file == null) {
      file = module.getModuleFile();
      file = (file == null) ? null : file.getParent();
    }
    if (file != null) {
      mainPanel.setWadlFilePath(file.getPath() + File.separator + module.getName() + WADL_EXT);
    }


    final DialogBuilder builder = new DialogBuilder(module.getProject());
    builder.setTitle(WSBundle.message("generate.wadl.from.java.dialog.title"));
    builder.setCenterPanel(mainPanel.getCentralPanel());
    if (builder.show() == DialogWrapper.OK_EXIT_CODE) {
      generateWadl(module, mainPanel);
    }
  }

  private static void generateWadl(final Module module, final GenerateWadlFromJavaDialog panel) {
    String path = ModuleRootManager.getInstance(module).getModuleExtension(CompilerModuleExtension.class).getCompilerOutputUrl();
    if (path == null) return;
    if (path.startsWith(LocalFileSystem.PROTOCOL_PREFIX)) {
      path = path.substring(LocalFileSystem.PROTOCOL_PREFIX.length());
    }
    VirtualFile[] roots = LibraryUtil.getLibraryRoots(new Module[]{module}, false, false);
    List<String> paths = new ArrayList<String>(roots.length + 1);
    paths.add(getWadlGenPath());
    for (VirtualFile root : roots) {
      paths.add(root.getPresentableUrl());
    }
    String[] libs = paths.toArray(new String[paths.size()]);

    InvokeExternalCodeUtil.JavaExternalProcessHandler handler =
        new InvokeExternalCodeUtil.JavaExternalProcessHandler(WSBundle.message("generate.wadl.from.java.dialog.title"), GEN_WADL_FQN,
                                                              libs,
                                                              new String[]{path, panel.getBaseURI(), panel.getWadlFilePath()}, module,
                                                              false);

    final Runnable run = new Runnable() {
      public void run() {
        final VirtualFile wadl =
            VirtualFileManager.getInstance().refreshAndFindFileByUrl(LocalFileSystem.PROTOCOL_PREFIX + panel.getWadlFilePath());
        final FileEditorManager editorManager = FileEditorManager.getInstance(module.getProject());
        if (wadl != null) {
          final PsiFile file = PsiManager.getInstance(module.getProject()).findFile(wadl);
          boolean opened = editorManager.isFileOpen(wadl);
          if (file != null && file.getChildren().length > 0) {
            if (opened) {
              wadl.refresh(false, false);
            }
            editorManager.openFile(wadl, true);

            CommandProcessor.getInstance().executeCommand(module.getProject(), new Runnable() {
              public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                  public void run() {
                    try {
                    CodeStyleManager.getInstance(module.getProject()).reformat(file.getChildren()[0]);
                    } catch (Exception e) {//
                    }
                  }
                });
              }
            }, WSBundle.message("generate.wadl.from.java.reformat.wadl.dialog.title"), null);
          }
        }
      }
    };
    InvokeExternalCodeUtil.runViaConsole(handler, module.getProject(), run, new NullableFunction<Exception, Void>() {
      public Void fun(final Exception e) {
        run.run();
        return null;
      }
    }, new Function<Void, Boolean>() {
      public Boolean fun(final Void aVoid) {
        return Boolean.FALSE;
      }
    }, null);
  }

  @NonNls
  private static String getWadlGenPath() {
    return LibUtils.getExtractedResourcesWebServicesDir()
           + File.separator
           + "wadlgen"
           + File.separator
           + "wadlgen.jar";
  }
}
