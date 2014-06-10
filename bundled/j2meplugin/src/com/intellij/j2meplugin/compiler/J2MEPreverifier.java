/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.j2meplugin.compiler;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.emulator.MobileSdk;
import com.intellij.j2meplugin.module.J2MEModuleProperties;
import com.intellij.j2meplugin.module.J2MEModuleType;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ActionRunner;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * User: anna
 * Date: Aug 24, 2004
 */
public class J2MEPreverifier implements ClassPostProcessingCompiler {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");
  private final HashMap<Module, File> myModulePreverifiedClasses = new HashMap<Module, File>();

  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext context) {
    myModulePreverifiedClasses.clear();
    return ApplicationManager.getApplication().runReadAction(new Computable<ProcessingItem[]>() {
      public ProcessingItem[] compute() {
        final Module[] affectedModules = context.getCompileScope().getAffectedModules();
        if (affectedModules == null || affectedModules.length == 0) {
          return ProcessingItem.EMPTY_ARRAY;
        }
        ArrayList<ProcessingItem> result = new ArrayList<ProcessingItem>();
        for (final Module module : affectedModules) {
          if (!module.getModuleType().equals(J2MEModuleType.getInstance())) continue;
          final Sdk jdk = ModuleRootManager.getInstance(module).getSdk();
          //check mobile jdk
          if (!MobileSdk.checkCorrectness(jdk, module)) {
            context.addMessage(CompilerMessageCategory.ERROR,
                               J2MEBundle.message("compiler.jdk.is.invalid.common", jdk != null ? jdk.getName() : " "), null, -1, -1);
            continue;
          }
          if (((Emulator)jdk.getSdkAdditionalData()).getEmulatorType().getPreverifyPath(jdk.getHomePath()) == null) {
            //not necessary to preverify
            continue;
          }
          try {
            final File temp = FileUtil.createTempDirectory("temp", "temp");
            temp.deleteOnExit();
            myModulePreverifiedClasses.put(module, temp);
            HashSet<Module> modulesToPreverify = new HashSet<Module>();
            MobileMakeUtil.getDependencies(module, modulesToPreverify);
            ArrayList<VirtualFile> dependantClasspath = new ArrayList<VirtualFile>();
            for (Module toPreverify : modulesToPreverify) {
              if (module.equals(toPreverify)) continue;
              final VirtualFile moduleOutputDirectory = context.getModuleOutputDirectory(toPreverify);
              dependantClasspath.add(moduleOutputDirectory);
              if (moduleOutputDirectory != null) {
                result.add(new MyProcessingItem(moduleOutputDirectory, toPreverify, temp, jdk, null));
              }
            }
            final VirtualFile moduleOutputDirectory = context.getModuleOutputDirectory(module);
            if (moduleOutputDirectory != null) {
              result.add(new MyProcessingItem(moduleOutputDirectory, module, temp, jdk, dependantClasspath));
            }
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }
        return result.toArray(new ProcessingItem[result.size()]);
      }
    });
  }

  public ProcessingItem[] process(final CompileContext context, final ProcessingItem[] items) {
    ProgressIndicator progressIndicator = context.getProgressIndicator();
    try {
      progressIndicator.pushState();
      progressIndicator.setText(J2MEBundle.message("compiler.preverifying.progress.title"));
      try {
        ActionRunner.runInsideWriteAction(new ActionRunner.InterruptibleRunnable() {
          public void run() throws Exception {
            LocalFileSystem.getInstance().refresh(false);
          }
        });
      }
      catch (Exception e) {
        LOG.error(e);
      }
      return ApplicationManager.getApplication().runReadAction(new Computable<ProcessingItem[]>() {
        public ProcessingItem[] compute() {
          List<ProcessingItem> processed = new ArrayList<ProcessingItem>();
          for (int i = 0; items != null && i < items.length; i++) {
            final MyProcessingItem item = ((MyProcessingItem)items[i]);
            final ArrayList<VirtualFile> dependantClasspath = item.getDependantClasspath();
            try {
              FileUtil.copyDir(new File(item.myFromClasses.getPath().replace('/', File.separatorChar)), item.getToClasses());
            }
            catch (IOException e) {
              LOG.error(e);
            }
            final Sdk jdk = item.getJdkToPreverify();
            try {
              GeneralCommandLine generalCommandLine = new GeneralCommandLine();
              final Emulator emulator = (Emulator)jdk.getSdkAdditionalData();
              final EmulatorType emulatorType = emulator.getEmulatorType();
              generalCommandLine.setExePath(emulatorType.getPreverifyPath(jdk.getHomePath()));
              final String[] preverifyOptions = emulator.getPreverifyOptions();
              if (preverifyOptions != null) {
                generalCommandLine.addParameters(preverifyOptions);
              }
              generalCommandLine.addParameter("-d");
              generalCommandLine.addParameter(item.getToClasses().getPath().replace(File.separatorChar, '/'));
              generalCommandLine.addParameter("-classpath");
              String[] urls = jdk.getRootProvider().getUrls(OrderRootType.CLASSES);
              final OrderEntry[] orderEntries = ModuleRootManager.getInstance(item.getModule()).getOrderEntries();
              for (OrderEntry orderEntry : orderEntries) {
                if (orderEntry instanceof LibraryOrderEntry) {
                  urls = ArrayUtil.mergeArrays(urls, orderEntry.getUrls(OrderRootType.CLASSES), String.class);
                }
              }
              String classpath = "";
              for (int k = 0; urls != null && k < urls.length; k++) {
                classpath += PathUtil.toPresentableUrl(urls[k]) + (k != urls.length - 1 ? File.pathSeparator : "");
              }
              for (int k = 0; dependantClasspath != null && k < dependantClasspath.size(); k++) {
                classpath += File.pathSeparator + PathUtil.getLocalPath(dependantClasspath.get(k));
              }

              generalCommandLine.addParameter(classpath);
              generalCommandLine.addParameter(item.getFile().getPath());

              generalCommandLine.setWorkDirectory(jdk.getHomePath());

              OSProcessHandler osProcessHandler = new OSProcessHandler(generalCommandLine.createProcess(),
                                                                       generalCommandLine.getCommandLineString());
              final ArrayList<String> errors = new ArrayList<String>();
              osProcessHandler.addProcessListener(new ProcessAdapter() {
                public void onTextAvailable(final ProcessEvent event, final Key outputType) {
                  errors.add(event.getText());
                }
              });
              osProcessHandler.startNotify();
              osProcessHandler.waitFor();
              printPreverifyErrors(errors, context);
            }
            catch (ExecutionException e) {
              context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
            }
            processed.add(items[i]);
          }
          return processed.toArray(new ProcessingItem[processed.size()]);
        }
      });
    }
    finally {
      progressIndicator.popState();
    }
  }

  private void printPreverifyErrors(final ArrayList<String> errors, final CompileContext context) {
    int textIndex = 0;
    int noErrors = -1;
    while (textIndex < errors.size() && noErrors == -1) {
      String text = errors.get(textIndex);
      if (StringUtil.containsIgnoreCase(text, J2MEBundle.message("compiler.preverify.error"))) {
        noErrors = textIndex;
      }
      textIndex++;
    }
    if (noErrors > -1) {
      for (int errorIdx = noErrors; errorIdx < errors.size(); errorIdx++) {
        context.addMessage(CompilerMessageCategory.WARNING, getDescription() + errors.get(errorIdx), null, -1, -1);
      }
    }
  }

  public File getModulePreverifiedClasses(Module module) {
    return myModulePreverifiedClasses.get(module);
  }

  private static class MyProcessingItem implements ProcessingItem {
    private EmptyValidityState myEmptyValidityState;
    private final VirtualFile myFromClasses;
    private final File myToClasses;
    private final Sdk myProjectJdk;
    private final Module myModule;
    private final ArrayList<VirtualFile> myDependantClasspath;

    public MyProcessingItem(VirtualFile fromClasses,
                            Module module,
                            File toClasses,
                            Sdk projectJdk,
                            ArrayList<VirtualFile> dependantClasspath) {
      myFromClasses = fromClasses;
      myToClasses = toClasses;
      myProjectJdk = projectJdk;
      myModule = module;
      myDependantClasspath = dependantClasspath;
      setValidityState();
    }

    public ArrayList<VirtualFile> getDependantClasspath() {
      return myDependantClasspath;
    }

    public File getToClasses() {
      return myToClasses;
    }

    public Sdk getJdkToPreverify() {
      return myProjectJdk;
    }

    @NotNull
    public VirtualFile getFile() {
      return myFromClasses;
    }

    public Module getModule() {
      return myModule;
    }


    public EmptyValidityState getValidityState() {
      return myEmptyValidityState;
    }

    public void setValidityState() {
      myEmptyValidityState = new EmptyValidityState();
    }


  }


  @NotNull
  public String getDescription() {
    return J2MEBundle.message("compiler.preverifier");
  }

  public boolean validateConfiguration(final CompileScope scope) {
    return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      public Boolean compute() {
        final Module[] affectedModules = scope.getAffectedModules();
        if (affectedModules == null || affectedModules.length == 0) {
          return Boolean.TRUE;
        }
        for (final Module module : affectedModules) {
          if (!module.getModuleType().equals(J2MEModuleType.getInstance())) continue;
          Sdk projectJdk = ModuleRootManager.getInstance(module).getSdk();
          if (!MobileSdk.checkCorrectness(projectJdk, module)) {
            Messages.showErrorDialog(
              J2MEBundle.message("compiler.jdk.is.invalid", projectJdk != null ? projectJdk.getName() : "", module.getName()),
              J2MEBundle.message("compiler.unable.to.compile", module.getName()));
            return Boolean.FALSE;
          }
          final MobileApplicationType mobileApplicationType = J2MEModuleProperties.getInstance(module).getMobileApplicationType();
          final MobileModuleSettings settings = MobileModuleSettings.getInstance(module);
          LOG.assertTrue(settings != null);
          if (settings.getSettings().get(mobileApplicationType.getJarUrlSettingName()) == null) {
            Messages.showErrorDialog(J2MEBundle.message("compiler.jar.file.not.specified"),
                                     J2MEBundle.message("compiler.unable.to.compile", module.getName()));
            return Boolean.FALSE;
          }
          else if (settings.getMobileDescriptionPath() == null) {
            Messages.showErrorDialog(
              J2MEBundle.message("compiler.descriptor.file.not.specified", StringUtil.capitalize(mobileApplicationType.getExtension())),
              J2MEBundle.message("compiler.unable.to.compile", module.getName()));
            return Boolean.FALSE;
          }
        }
        return Boolean.TRUE;
      }
    }).booleanValue();
  }

  public ValidityState createValidityState(DataInput in) throws IOException {
    return new EmptyValidityState();
  }

}
