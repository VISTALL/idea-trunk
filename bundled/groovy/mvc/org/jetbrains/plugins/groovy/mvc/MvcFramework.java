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

package org.jetbrains.plugins.groovy.mvc;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.CantRunException;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeView;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathsList;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * @author peter
 */
public abstract class MvcFramework {
  public static final Key<Boolean> CREATE_APP_STRUCTURE = Key.create("CREATE_MVC_APP_STRUCTURE");
  @NonNls public static final String GROOVY_STARTER_CONF = "/conf/groovy-starter.conf";
  @NonNls public static final String XMX_JVM_PARAMETER = "-Xmx";

  public boolean hasSupport(@NotNull Module module) {
    return getSdkRoot(module) != null && findAppRoot(module) != null;
  }

  public abstract boolean isCommonPluginsModule(@NotNull Module module);

  public void syncSdkAndPluginsModule(@NotNull Module module) {
    final Module pluginsModule = findCommonPluginsModule(module);
    if (pluginsModule != null) {
      MvcModuleStructureUtil.syncAuxModuleSdk(module, pluginsModule);
    }
  }


  public abstract void upgradeFramework(@NotNull Module module);

  public void createApplicationIfNeeded(@NotNull final Module module) {
    if (findAppRoot(module) == null && module.getUserData(CREATE_APP_STRUCTURE) == Boolean.TRUE) {
      module.putUserData(CREATE_APP_STRUCTURE, null);
      final int result = Messages.showDialog(module.getProject(),
                                             "Create default " + getDisplayName() + " directory structure in module '" + module.getName() + "'?",
                                             "Create " + getDisplayName() + " application", new String[]{"Run 'create-&app'", "Run 'create-&plugin'", "&Cancel"}, 0, getIcon());
      if (result < 0 || result > 1) {
        return;
      }

      final ProcessBuilder pb = createCommand(module, true, result == 0 ? "create-app" : "create-plugin", module.getName());

      MvcConsole.getInstance(module.getProject()).executeProcess(module, pb, new Runnable() {
        public void run() {
          if (module.isDisposed()) return;

          VirtualFile root = findAppRoot(module);
          if (root == null) return;

          PsiDirectory psiDir = PsiManager.getInstance(module.getProject()).findDirectory(root);
          IdeView ide = (IdeView)DataManager.getInstance().getDataContext().getData(DataConstantsEx.IDE_VIEW);
          if (ide != null) ide.selectElement(psiDir);

          //also here comes fileCreated(application.properties) which manages roots and run configuration
        }
      }, true);
    }

  }

  public void updateProjectStructure(@NotNull final Module module) {
    final VirtualFile root = findAppRoot(module);
    if (root == null) {
      return;
    }

    MvcModuleStructureUtil.updateModuleStructure(module, createProjectStructure(module, false), root);

    MvcModuleStructureUtil.updateAuxiliaryPluginsModuleRoots(module, this);
  }

  public abstract void ensureRunConfigurationExists(@NotNull Module module);

  @Nullable
  public abstract VirtualFile findAppRoot(@Nullable Module module);

  @Nullable
  public abstract VirtualFile getSdkRoot(@Nullable Module module);

  protected List<VirtualFile> getImplicitClasspathRoots(@NotNull Module module) {
    final List<VirtualFile> toExclude = new ArrayList<VirtualFile>();

    ContainerUtil.addIfNotNull(getSdkRoot(module), toExclude);
    final VirtualFile appRoot = findAppRoot(module);
    if (appRoot != null) {
      ContainerUtil.addIfNotNull(appRoot.findChild("lib"), toExclude);
    }
    return toExclude;
  }

  private PathsList removeFrameworkStuff(Module module, List<VirtualFile> rootFiles) {
    final List<VirtualFile> toExclude = getImplicitClasspathRoots(module);

    PathsList scriptClassPath = new PathsList();
    eachRoot:
    for (VirtualFile file : rootFiles) {
      for (final VirtualFile excluded : toExclude) {
        if (VfsUtil.isAncestor(excluded, file, false)) {
          continue eachRoot;
        }
      }
      scriptClassPath.add(file);
    }
    return scriptClassPath;
  }

  private static List<VirtualFile> removeSdkRoots(Module module, LinkedHashSet<VirtualFile> allRoots) {
    final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
    if (sdk != null) {
        allRoots.removeAll(Arrays.asList(sdk.getRootProvider().getFiles(OrderRootType.CLASSES)));
      }

    PathsList tempClassPath = new PathsList();
    for (final VirtualFile file : allRoots) {
      tempClassPath.add(file);
    }

    return tempClassPath.getVirtualFiles();
  }

  public PathsList getApplicationClassPath(Module module, boolean forTests) {
    final VirtualFile[] files = ModuleRootManager.getInstance(module).getFiles(OrderRootType.CLASSES_AND_OUTPUT);
    final List<VirtualFile> classPath = removeSdkRoots(module, new LinkedHashSet<VirtualFile>(Arrays.asList(files)));

    retainOnlyJarsAndDirectories(classPath);

    removeModuleOutput(module, classPath);

    final Module pluginsModule = findCommonPluginsModule(module);
    if (pluginsModule != null) {
      removeModuleOutput(pluginsModule, classPath);
    }

    return removeFrameworkStuff(module, classPath);
  }

  private static void retainOnlyJarsAndDirectories(List<VirtualFile> woSdk) {
    for (Iterator<VirtualFile> iterator = woSdk.iterator(); iterator.hasNext();) {
      VirtualFile file = iterator.next();
      if (JarFileSystem.getInstance().getVirtualFileForJar(file) == null && !file.isDirectory()) {
        iterator.remove();
      }
    }
  }

  private static void removeModuleOutput(Module module, List<VirtualFile> from) {
    final CompilerModuleExtension extension = ModuleRootManager.getInstance(module).getModuleExtension(CompilerModuleExtension.class);
    from.remove(extension.getCompilerOutputPath());
    from.remove(extension.getCompilerOutputPathForTests());
  }


  public void fillJavaParameters(@NotNull Module module, JavaParameters params, boolean forCreation, boolean forTests) {
    fillJavaParameters(module, params, forCreation, forTests, true);
  }

  public abstract void fillJavaParameters(@NotNull Module module, JavaParameters params, boolean forCreation, boolean forTests,
                                          boolean classpathFromDependencies);

  protected static void ensureRunConfigurationExists(Module module, ConfigurationType configurationType, String name) {
    final RunManagerEx runManager = RunManagerEx.getInstanceEx(module.getProject());
    for (final RunConfiguration runConfiguration : runManager.getConfigurations(configurationType)) {
      if (runConfiguration instanceof MvcRunConfiguration && ((MvcRunConfiguration)runConfiguration).getModule() == module) {
        return;
      }
    }

    final ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];
    final RunnerAndConfigurationSettingsImpl runSettings = (RunnerAndConfigurationSettingsImpl)runManager.createRunConfiguration(name,
                                                                                                                                 factory);
    final MvcRunConfiguration configuration = (MvcRunConfiguration)runSettings.getConfiguration();
    configuration.setModule(module);
    runManager.addConfiguration(runSettings, false);
    runManager.setActiveConfiguration(runSettings);

    final CompileStepBeforeRun.MakeBeforeRunTask runTask = runManager.getBeforeRunTask(configuration, CompileStepBeforeRun.ID);
    if (runTask != null) {
      runTask.setEnabled(false);
    }
  }

  public abstract String getFrameworkName();
  public String getDisplayName() {
    return getFrameworkName();
  }
  public abstract Icon getIcon();

  public abstract String getSdkHomePropertyName();

  @NotNull
  public ProcessBuilder createCommand(@NotNull Module module, final boolean forCreation, String... cmdLine) {
    final JavaParameters params = new JavaParameters();
    fillJavaParameters(module, params, forCreation, false);
    params.getProgramParametersList().add(StringUtil.join(cmdLine, " "));

    try {
      final ProcessBuilder builder = new ProcessBuilder(CommandLineBuilder.createFromJavaParameters(params).getCommands());
      final VirtualFile griffonHome = getSdkRoot(module);
      if (griffonHome != null) {
        builder.environment().put(getSdkHomePropertyName(), FileUtil.toSystemDependentName(griffonHome.getPath()));
      }

      final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
      if (sdk != null && sdk.getSdkType() instanceof JavaSdkType) {
        String path = StringUtil.trimEnd(sdk.getHomePath(), File.separator);
        if (StringUtil.isNotEmpty(path)) {
          builder.environment().put("JAVA_HOME", FileUtil.toSystemDependentName(path));
        }
      }


      final VirtualFile root = findAppRoot(module);
      final File ioRoot = root != null ? VfsUtil.virtualToIoFile(root) : new File(module.getModuleFilePath()).getParentFile();
      builder.directory(forCreation ? ioRoot.getParentFile() : ioRoot);

      return builder;
    }
    catch (CantRunException e) {
      throw new RuntimeException(e);
    }
  }

  public List<VirtualFile> getPluginRoots(@NotNull Module module) {
    VirtualFile root = findAppRoot(module);
    ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
    if (root != null) {
      VirtualFile pluginsDir = root.findChild(MvcModuleStructureUtil.PLUGINS_DIRECTORY);
      if (pluginsDir != null) {
        for (VirtualFile file : pluginsDir.getChildren()) {
          if (file.isDirectory()) {
            result.add(file);
          }
        }
      }
    }
    final VirtualFile projectWorkDir = getProjectWorkDir(module);
    if (projectWorkDir != null) {
      VirtualFile pluginsDir = projectWorkDir.findChild(MvcModuleStructureUtil.PLUGINS_DIRECTORY);
      if (pluginsDir != null) {
        for (VirtualFile file : pluginsDir.getChildren()) {
          if (file.isDirectory()) {
            result.add(file);
          }
        }
      }
    }
    return result;
  }

  @Nullable
  public Module findCommonPluginsModule(@NotNull Module module) {
    return ModuleManager.getInstance(module.getProject()).findModuleByName(getCommonPluginsModuleName(module));
  }

  @Nullable
  public abstract String getSdkWorkDir(@NotNull Module module);

  @Nullable
  public VirtualFile getProjectWorkDir(@NotNull Module module) {
    final String path = MvcWatchedRootProvider.getProjectWorkDirPath(this, module);
    return path == null ? null : LocalFileSystem.getInstance().findFileByPath(path);
  }

  public Map<String, String> getInstalledCommonPluginVersions(Module module) {
      Map<String, String> pluginNames = new THashMap<String, String>();
      final PropertiesFile properties = MvcModuleStructureUtil.findApplicationProperties(module, this);
      if (properties != null) {
        for (final Property property : properties.getProperties()) {
          String propName = property.getName();
          if (propName == null) continue;

          propName = propName.trim();
          if (propName.startsWith("plugins.")) {
            pluginNames.put(propName.substring("plugins.".length()), property.getValue());
          }
        }
      }

      return pluginNames;
  }

  public abstract String getCommonPluginsModuleName(Module module);

  public abstract boolean isSDKLibrary(Library library);

  public abstract MvcProjectStructure createProjectStructure(@NotNull Module module, boolean auxModule);

  protected static String getSdkWorkDirParent(String framework) {
    String grailsWorkDir = System.getProperty(framework + ".work.dir");
    if (StringUtil.isNotEmpty(grailsWorkDir)) {
      grailsWorkDir = FileUtil.toSystemIndependentName(grailsWorkDir);
      if (!grailsWorkDir.endsWith("/")) {
        grailsWorkDir += "/";
      }
      return grailsWorkDir;
    }

    return FileUtil.toSystemIndependentName(SystemProperties.getUserHome()) + "/." + framework + "/";
  }
}
