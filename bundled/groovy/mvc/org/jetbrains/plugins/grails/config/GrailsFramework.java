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

package org.jetbrains.plugins.grails.config;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.PathsList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.groovy.grails.tests.GrailsTestListener;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.runner.GrailsRunConfigurationType;
import org.jetbrains.plugins.grails.util.GrailsFacetProvider;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;
import org.jetbrains.plugins.groovy.mvc.MvcModuleStructureUtil;
import org.jetbrains.plugins.groovy.mvc.MvcProjectStructure;
import org.jetbrains.plugins.groovy.util.GroovyUtils;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author peter
 */
public class GrailsFramework extends MvcFramework {
  @NonNls private static final String GRAILS_STARTER_CLASS = "org.codehaus.groovy.grails.cli.support.GrailsStarter";
  @NonNls private static final String GRAILS_SCRIPT_RUNNER = "org.codehaus.groovy.grails.cli.GrailsScriptRunner";
  @NonNls private static final String GRAILS_TEST_RUNNER = "org.jetbrains.groovy.grails.tests.GrailsIdeaTestRunner";
  public static final GrailsFramework INSTANCE = new GrailsFramework();
  @NonNls public static final String GRAILS_BOOTSTRAP_JAR = "grails-bootstrap.*jar";

  public boolean isCommonPluginsModule(@NotNull Module module) {
    return GrailsModuleStructureUtil.isCommonPluginsModule(module);
  }

  public void syncSdkAndPluginsModule(@NotNull Module module) {
    super.syncSdkAndPluginsModule(module);
    for (Module auxModule : GrailsModuleStructureUtil.getCustomPluginModules(module).values()) {
      if (auxModule == null) {
        continue;
      }
      MvcModuleStructureUtil.syncAuxModuleSdk(module, auxModule);
    }
  }

  public void upgradeFramework(@NotNull Module module) {
    if (findAppRoot(module) != null && !GrailsModuleStructureUtil.isCustomPluginModule(module)) {
      GrailsModuleStructureUtil.upgradeGrails(module);
    }
  }

  public void updateProjectStructure(@NotNull final Module module) {
    final VirtualFile root = findAppRoot(module);
    if (root == null) {
      return;
    }

    super.updateProjectStructure(module);

    final Map<String, Module> customPluginModules = GrailsModuleStructureUtil.getCustomPluginModules(module);
    final Map<String, VirtualFile> locations = GrailsModuleStructureUtil.getCustomPluginLocations(module);
    for (final String pluginName : customPluginModules.keySet()) {
      @Nullable Module pluginModule = customPluginModules.get(pluginName);
      final VirtualFile appRoot = locations.get(pluginName);
      if (appRoot == null ) {
        if (pluginModule != null) {
          handleCustomPluginReferenceRemoval(module, pluginModule);
        }
      } else {
        updateCustomPluginModule(module, pluginName, appRoot, pluginModule);
      }
    }
  }

  private void updateCustomPluginModule(Module module, String pluginName, VirtualFile appRoot, @Nullable Module pluginModule) {
    if (pluginModule != null) {
      MvcModuleStructureUtil.ensureDependency(module, pluginModule);
    } else {
      final String pluginModuleName = module.getName() + GrailsModuleStructureUtil.CUSTOM_PLUGINS_MODULE_INFIX + pluginName;
      pluginModule = MvcModuleStructureUtil.createAuxiliaryModule(module, pluginModuleName, true, INSTANCE);
    }
    MvcModuleStructureUtil.updateAuxModuleStructure(module, pluginModule, Arrays.asList(appRoot), this);
  }

  private static void handleCustomPluginReferenceRemoval(Module module, @NotNull Module pluginModule) {
    if (GrailsModuleStructureUtil.isIdeaGeneratedPluginModule(pluginModule)) {
      MvcModuleStructureUtil.removeAuxiliaryModule(module, pluginModule);
    } else {
      MvcModuleStructureUtil.removeDependency(module, pluginModule);
    }
  }

  public void ensureRunConfigurationExists(@NotNull Module module) {
    final GrailsRunConfigurationType configurationType = GrailsRunConfigurationType.getInstance();

    final VirtualFile root = findAppRoot(module);
    if (root != null &&
        !GrailsModuleStructureUtil.isCustomPluginModule(module) &&
        !GrailsUtils.isGrailsPluginModule(module)) {
      ensureRunConfigurationExists(module, configurationType, "Grails:" + root.getName());
    }
  }

  @Override
  public VirtualFile findAppRoot(@Nullable Module module) {
    return GrailsUtils.findGrailsAppRoot(module);
  }

  @Override
  public VirtualFile getSdkRoot(@Nullable Module module) {
    final String path = GrailsConfigUtils.getInstance().getSDKInstallPath(module);
    return StringUtil.isEmpty(path) ? null : LocalFileSystem.getInstance().findFileByIoFile(new File(path));
  }

  @Override
  protected List<VirtualFile> getImplicitClasspathRoots(@NotNull Module module) {
    final List<VirtualFile> files = super.getImplicitClasspathRoots(module);
    final String grailsWorkDirPath = GrailsFramework.INSTANCE.getSdkWorkDir(module);
    if (grailsWorkDirPath != null) {
      ContainerUtil.addIfNotNull(LocalFileSystem.getInstance().findFileByIoFile(new File(grailsWorkDirPath)), files);
    }
    for (Module customPluginModule : GrailsModuleStructureUtil.getCustomPluginModules(module).values()) {
      if (customPluginModule == null) {
        continue;
      }

      final CompilerModuleExtension extension =
        ModuleRootManager.getInstance(customPluginModule).getModuleExtension(CompilerModuleExtension.class);
      ContainerUtil.addIfNotNull(extension.getCompilerOutputPath(), files);
      ContainerUtil.addIfNotNull(extension.getCompilerOutputPathForTests(), files);

      final VirtualFile appRoot = findAppRoot(customPluginModule);
      if (appRoot != null) {
        ContainerUtil.addIfNotNull(appRoot.findChild("lib"), files);
      }
    }
    return files;
  }

  @Override
  public PathsList getApplicationClassPath(Module module, boolean forTests) {
    final PathsList path = super.getApplicationClassPath(module, forTests);
    if (forTests) {
      path.add(PathUtil.getJarPathForClass(GrailsTestListener.class));
    }
    return path;
  }

  @Override
  public void fillJavaParameters(@NotNull Module module, JavaParameters params, boolean forCreation, boolean forTests,
                                 boolean classpathFromDependencies) {
    Sdk sdk = ModuleRootManager.getInstance(module).getSdk();

    params.setJdk(sdk);
    final String grailsHome = GrailsConfigUtils.getInstance().getSDKInstallPath(module);

    configureGrailsRunnerClassPath(params, grailsHome);

    /////////////////////////////////////////////////////////////

    params.setMainClass(GRAILS_STARTER_CLASS);

    VirtualFile root = GrailsUtils.findGrailsAppRoot(module);
    File rootFile = root == null ? new File(module.getModuleFilePath()).getParentFile() : VfsUtil.virtualToIoFile(root);
    if (forCreation) {
      rootFile = rootFile.getParentFile();
    }
    String workDir = rootFile.getAbsolutePath();

    if (!params.getVMParametersList().getParametersString().contains(XMX_JVM_PARAMETER)) {
      params.getVMParametersList().add("-Xmx256M");
    }

    params.getVMParametersList().add("-Dgrails.home=" + grailsHome);
    params.getVMParametersList().add("-Dbase.dir=" + workDir);

    assert sdk != null;
    params.getVMParametersList().add("-Dtools.jar=" + ((JavaSdkType)sdk.getSdkType()).getToolsPath(sdk));

    String confpath = grailsHome + GROOVY_STARTER_CONF;
    params.getVMParametersList().add("-Dgroovy.starter.conf=" + confpath);

    params.getProgramParametersList().add("--main");
    params.getProgramParametersList().add(GRAILS_SCRIPT_RUNNER);
    params.getProgramParametersList().add("--conf");
    params.getProgramParametersList().add(confpath);
    if (classpathFromDependencies) {
      final String path = GrailsFramework.INSTANCE.getApplicationClassPath(module, forTests).getPathsString();
      if (StringUtil.isNotEmpty(path)) {
        params.getProgramParametersList().add("--classpath");
        params.getProgramParametersList().add(path);
      }
    }

    if (forTests && GrailsConfigUtils.isAtLeastGrails1_2(module)) {
      params.getVMParametersList().add("-Dgrails.test.runner=" + GRAILS_TEST_RUNNER);
    }

    params.setWorkingDirectory(workDir);
  }

  @Override
  public String getFrameworkName() {
    return "Grails";
  }

  @Override
  public Icon getIcon() {
    return GrailsIcons.GRAILS_ICON;
  }

  @Override
  public String getSdkHomePropertyName() {
    return "GRAILS_HOME";
  }

  private static void configureGrailsRunnerClassPath(JavaParameters params, String grailsInstallPath) {
    File[] files = GroovyUtils.getFilesInDirectoryByPattern(grailsInstallPath + "/lib", "groovy-all.*jar");
    if (files.length > 0) {
      params.getClassPath().add(files[0].getAbsolutePath());
    }

    files = GroovyUtils.getFilesInDirectoryByPattern(grailsInstallPath + "/dist/", "grails-cli.*jar");
    if (files.length > 0) {
      params.getClassPath().add(files[0].getAbsolutePath());
    }

    //in Grails 1.1 there's no CLI, while there's bootstrap
    files = GroovyUtils.getFilesInDirectoryByPattern(grailsInstallPath + "/dist/", GRAILS_BOOTSTRAP_JAR);
    if (files.length > 0) {
      params.getClassPath().add(files[0].getAbsolutePath());
    }
  }

  @Override
  public Map<String, String> getInstalledCommonPluginVersions(Module module) {
    final Map<String, String> map = super.getInstalledCommonPluginVersions(module);
    map.keySet().removeAll(GrailsModuleStructureUtil.getCustomPluginLocations(module).keySet());
    return map;
  }

  @Override
  public String getCommonPluginsModuleName(Module module) {
    return GrailsModuleStructureUtil.getCommonPluginsModuleName(module);
  }

  @Override
  public boolean isSDKLibrary(Library library) {
    return GrailsConfigUtils.getInstance().isSDKLibrary(library);
  }

  @Override
  public MvcProjectStructure createProjectStructure(@NotNull Module module, boolean auxModule) {
    return new GrailsProjectStructure(module, auxModule);
  }

  static class GrailsProjectStructure extends MvcProjectStructure {

    GrailsProjectStructure(Module module, boolean auxModule) {
      super(module, auxModule, getUserHomeGrails());
    }

    @NotNull
    public String getDefaultLibraryName() {
      return GrailsUtils.GRAILS_USER_LIBRARY;
    }

    public String[] getSourceFolders() {
      return new String[]{"src/java", "src/groovy", "grails-app/controllers", "grails-app/domain", "grails-app/services", "grails-app/taglib"};
    }

    public String[] getTestFolders() {
      return new String[]{"test/unit", "test/integration"};
    }

    public String[] getInvalidSourceFolders() {
      return new String[]{"src", "grails-app/conf/hibernate", "grails-app/conf", "scripts"};
    }

    @Override
    public String[] getExcludedFolders() {
      return new String[]{"web-app/plugins"};
    }

    public void setupFacets(VirtualFile root) {
      final ModifiableFacetModel facetModel = FacetManager.getInstance(myModule).createModifiableModel();
      try {
        for (GrailsFacetProvider provider : GrailsFacetProvider.EP_NAME.getExtensions()) {
          provider.addFacets(facetModel, myModule, root);
        }
      }
      finally {
        facetModel.commit();
      }
    }
  }

  @Nullable
  public String getSdkWorkDir(@NotNull Module module) {
    final String version = GrailsConfigUtils.getInstance().getGrailsVersion(module);
    if (version == null) return null;

    return getUserHomeGrails() + version;
  }

  public static String getUserHomeGrails() {
    return getSdkWorkDirParent("grails");
  }

}