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

package org.jetbrains.plugins.groovy.griffon;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;
import org.jetbrains.plugins.groovy.mvc.MvcProjectStructure;

import javax.swing.*;
import java.io.File;

/**
 * @author peter
 */
public class GriffonFramework extends MvcFramework {
  public static final GriffonFramework INSTANCE = new GriffonFramework();
  public static final Icon GRIFFON_ICON = IconLoader.findIcon("/images/griffon/griffon.png");
  @NonNls private static final String GRIFFON_COMMON_PLUGINS = "-griffonPlugins";

  @Override
  public boolean isCommonPluginsModule(@NotNull Module module) {
    return module.getName().endsWith(GRIFFON_COMMON_PLUGINS);
  }

  @Override
  public void upgradeFramework(@NotNull Module module) {
  }

  @Override
  public void ensureRunConfigurationExists(@NotNull Module module) {
    final VirtualFile root = findAppRoot(module);
    if (root != null) {
      ensureRunConfigurationExists(module, GriffonRunConfigurationType.getInstance(), "Griffon:" + root.getName());
    }
  }

  @Override
  public VirtualFile findAppRoot(@Nullable Module module) {
    for (ContentEntry entry : ModuleRootManager.getInstance(module).getContentEntries()) {
      final VirtualFile file = entry.getFile();
      if (file != null) {
        final VirtualFile child = file.findChild("griffon-app");
        if (child != null) {
          return file;
        }
      }
    }

    return null;
  }

  @Override
  public VirtualFile getSdkRoot(@Nullable Module module) {
    for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (entry instanceof LibraryOrderEntry) {
        for (VirtualFile file : entry.getFiles(OrderRootType.CLASSES)) {
          if (GriffonLibraryManager.isGriffonCoreJar(file)) {
            final VirtualFile localFile = JarFileSystem.getInstance().getVirtualFileForJar(file);
            if (localFile != null) {
              final VirtualFile parent = localFile.getParent();
              if (parent != null) {
                return parent.getParent();
              }
            }
            return null;
          }
        }
      }
    }
    return null;
  }

  @Override
  public void fillJavaParameters(@NotNull Module module, final JavaParameters params, boolean forCreation, boolean forTests,
                                 boolean classpathFromDependencies) {
    Sdk sdk = ModuleRootManager.getInstance(module).getSdk();

    params.setJdk(sdk);
    final VirtualFile sdkRoot = getSdkRoot(module);
    if (sdkRoot == null) {
      return;
    }

    final VirtualFile lib = sdkRoot.findChild("lib");
    if (lib != null) {
      for (final VirtualFile child : lib.getChildren()) {
        final String name = child.getName();
        if (name.startsWith("groovy-all-") && name.endsWith(".jar")) {
          params.getClassPath().add(child);
        }
      }
    }
    final VirtualFile dist = sdkRoot.findChild("dist");
    if (dist != null) {
      for (final VirtualFile child : dist.getChildren()) {
        final String name = child.getName();
        if (name.startsWith("griffon-cli-") && name.endsWith(".jar")) {
          params.getClassPath().add(child);
        }
      }
    }


    /////////////////////////////////////////////////////////////

    params.setMainClass("org.codehaus.griffon.cli.support.GriffonStarter");

    VirtualFile root = findAppRoot(module);
    File rootFile = root == null ? new File(module.getModuleFilePath()).getParentFile() : VfsUtil.virtualToIoFile(root);
    if (forCreation) {
      rootFile = rootFile.getParentFile();
    }
    String workDir = rootFile.getAbsolutePath();

    if (!params.getVMParametersList().getParametersString().contains(XMX_JVM_PARAMETER)) {
      params.getVMParametersList().add("-Xmx256M");
    }

    final String griffonHomePath = FileUtil.toSystemDependentName(sdkRoot.getPath());
    params.getVMParametersList().add("-Dgriffon.home=" + griffonHomePath);
    params.getVMParametersList().add("-Dbase.dir=" + workDir);

    assert sdk != null;
    params.getVMParametersList().add("-Dtools.jar=" + ((JavaSdkType)sdk.getSdkType()).getToolsPath(sdk));

    final String confpath = griffonHomePath + GROOVY_STARTER_CONF;
    params.getVMParametersList().add("-Dgroovy.starter.conf=" + confpath);

    params.getProgramParametersList().add("--main");
    params.getProgramParametersList().add("org.codehaus.griffon.cli.GriffonScriptRunner");
    params.getProgramParametersList().add("--conf");
    params.getProgramParametersList().add(confpath);
    if (!forCreation && classpathFromDependencies) {
      final String path = getApplicationClassPath(module, forTests).getPathsString();
      if (StringUtil.isNotEmpty(path)) {
        params.getProgramParametersList().add("--classpath");
        params.getProgramParametersList().add(path);
      }
    }

    params.setWorkingDirectory(workDir);
  }

  @Override
  public String getFrameworkName() {
    return "Griffon";
  }

  @Override
  public Icon getIcon() {
    return GRIFFON_ICON;
  }

  @Override
  public String getSdkHomePropertyName() {
    return "GRIFFON_HOME";
  }

  @Override
  public Module findCommonPluginsModule(@NotNull Module module) {
    final String name = module.getName() + GRIFFON_COMMON_PLUGINS;
    for (Module candidate : ModuleManager.getInstance(module.getProject()).getModules()) {
      if (name.equals(candidate.getName())) {
        return candidate;
      }
    }
    return null;
  }

  @Nullable
  public String getSdkWorkDir(@NotNull Module module) {
    final String version = GriffonLibraryManager.getGriffonVersion(module);
    if (version == null) return null;

    return getUserHomeGriffon() + version;
  }

  @Override
  public String getCommonPluginsModuleName(Module module) {
    return module.getName() + GRIFFON_COMMON_PLUGINS;
  }

  @Override
  public boolean isSDKLibrary(Library library) {
    return GriffonLibraryManager.isGriffonSdk(library.getFiles(OrderRootType.CLASSES));
  }

  @Override
  public MvcProjectStructure createProjectStructure(@NotNull Module module, boolean auxModule) {
    return new GriffonProjectStructure(module, auxModule);
  }

  public static String getUserHomeGriffon() {
    return getSdkWorkDirParent("griffon");
  }

  private static class GriffonProjectStructure extends MvcProjectStructure {
    public GriffonProjectStructure(Module module, final boolean auxModule) {
      super(module, auxModule, GriffonFramework.getUserHomeGriffon());
    }

    @NotNull
    public String getDefaultLibraryName() {
      return "Griffon:lib";
    }

    public String[] getSourceFolders() {
      return new String[]{"griffon-app/controllers", "griffon-app/models", "griffon-app/views", "src/main"};
    }

    public String[] getTestFolders() {
      return new String[]{"test/unit", "test/integration"};
    }

    public String[] getInvalidSourceFolders() {
      return new String[]{"src"};
    }

    @Override
    public String[] getExcludedFolders() {
      return new String[]{"target"};
    }

    public void setupFacets(VirtualFile root) {
    }
  }
}
