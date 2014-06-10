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

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.roots.ui.configuration.actions.ModuleDeleteProvider;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.intellij.util.SmartList;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author peter
 */
public class MvcModuleStructureUtil {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.mvc.MvcModuleStructureUtil");
  @NonNls public static final String PLUGINS_DIRECTORY = "plugins";
  @NonNls public static final String APPLICATION_PROPERTIES = "application.properties";

  private MvcModuleStructureUtil() {
  }

  @NotNull
  public static ContentEntry findOrCreateContentEntry(ModifiableRootModel model, VirtualFile root) {
    for (ContentEntry entry : model.getContentEntries()) {
      if (entry.getFile() == root) {
        return entry;
      }
    }

    return model.addContentEntry(root);
  }

  @Nullable
  public static Consumer<ModifiableRootModel> addSourceRootsAndLibDirectory(@NotNull final VirtualFile root,
                                                      final Module module,
                                                      final VirtualFile contentRoot, final MvcProjectStructure structure) {
    Set<VirtualFile> sourceRoots = CollectionFactory.newTroveSet(ModuleRootManager.getInstance(module).getSourceRoots());

    root.refresh(false, true);

    final List<Consumer<ContentEntry>> actions = CollectionFactory.arrayList();

    for (final String src : structure.getSourceFolders()) {
      addSourceFolder(root, src, false, actions, sourceRoots);
    }
    for (final String src : structure.getTestFolders()) {
      addSourceFolder(root, src, true, actions, sourceRoots);
    }
    for (final String src : structure.getInvalidSourceFolders()) {
      removeSrcFolderFromRoots(root.findFileByRelativePath(src), actions, sourceRoots);
    }

    final Set<VirtualFile> excludeRoots = CollectionFactory.newTroveSet(ModuleRootManager.getInstance(module).getExcludeRoots());
    for (final String excluded : structure.getExcludedFolders()) {
      excludeDirectory(root, excluded, actions, excludeRoots);
    }

    final Consumer<ModifiableRootModel> modifyLib = addJarDirectory(root, module, structure.getDefaultLibraryName());

    if (actions.isEmpty() && modifyLib == null) {
      return null;
    }

    return new Consumer<ModifiableRootModel>() {
      public void consume(ModifiableRootModel model) {
        if (!actions.isEmpty()) {
          final ContentEntry contentEntry = findOrCreateContentEntry(model, contentRoot);
          for (final Consumer<ContentEntry> action : actions) {
            action.consume(contentEntry);
          }
        }
        if (modifyLib != null) {
          modifyLib.consume(model);
        }

        structure.setupFacets(root);
      }
    };
  }

  private static void excludeDirectory(VirtualFile root, String excluded, List<Consumer<ContentEntry>> actions, Set<VirtualFile> excludeRoots) {
    final VirtualFile src = root.findFileByRelativePath(excluded);
    if (src == null || excludeRoots.contains(src)) {
      return;
    }

    actions.add(new Consumer<ContentEntry>() {
      public void consume(ContentEntry contentEntry) {
        contentEntry.addExcludeFolder(src);
      }
    });
  }

  public static void removeSrcFolderFromRoots(final VirtualFile file, List<Consumer<ContentEntry>> actions, Collection<VirtualFile> sourceRoots) {
    if (sourceRoots.contains(file)) {
      actions.add(new Consumer<ContentEntry>() {
        public void consume(ContentEntry contentEntry) {
          SourceFolder[] folders = contentEntry.getSourceFolders();
          for (SourceFolder folder : folders) {
            if (folder.getFile() == file) {
              contentEntry.removeSourceFolder(folder);
            }
          }
        }
      });
    }
  }

  @Nullable
  public static Library findDefaultLibrary(Module module, String libName) {
    for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry) {
        final LibraryOrderEntry entry = (LibraryOrderEntry)orderEntry;
        if (libName.equals(entry.getLibraryName())) {
          return entry.getLibrary();
        }
      }
    }
    return null;
  }

  @Nullable
  public static Consumer<ModifiableRootModel> addJarDirectory(VirtualFile root, Module module, final String libName) {
    final VirtualFile libDir = root.findFileByRelativePath("lib");
    if (libDir == null || !libDir.isDirectory()) {
      return null;
    }

    final Library library = findDefaultLibrary(module, libName);
    if (library != null && library.isJarDirectory(libDir.getUrl())) {
      return null;
    }

    return new Consumer<ModifiableRootModel>() {
      public void consume(ModifiableRootModel model) {
        Library.ModifiableModel libModel = modifyDefaultLibrary(model, libName);
        libModel.addJarDirectory(libDir, false);
        libModel.commit();
      }
    };
  }

  public static Library.ModifiableModel modifyDefaultLibrary(ModifiableRootModel model, String libName) {
    LibraryTable libTable = model.getModuleLibraryTable();
    Library library = libTable.getLibraryByName(libName);
    if (library == null) {
      library = LibraryUtil.createLibrary(libTable, libName);
    }
    return library.getModifiableModel();
  }

  public static void addSourceFolder(@NotNull VirtualFile root, @NotNull String relativePath, final boolean isTest, List<Consumer<ContentEntry>> actions,
                                                final Collection<VirtualFile> sourceRoots) {
    final VirtualFile src = root.findFileByRelativePath(relativePath);
    if (src == null) {
      return;
    }

    if (sourceRoots.contains(src)) {
      return;
    }

    actions.add(new Consumer<ContentEntry>() {
      public void consume(ContentEntry contentEntry) {
        contentEntry.addSourceFolder(src, isTest, "");
      }
    });
  }

  public static void updateModuleStructure(Module module, MvcProjectStructure structure, @NotNull VirtualFile root) {
    List<Consumer<ModifiableRootModel>> actions = getUpdateProjectStructureActions(module, Arrays.asList(root), false, structure);

    if (!actions.isEmpty()) {
      final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
      for (final Consumer<ModifiableRootModel> action : actions) {
        action.consume(model);
      }
      model.commit();
    }
  }

  public static boolean checkValidity(VirtualFile pluginDir) {
    pluginDir.refresh(false, false);
    return pluginDir.isValid();
  }

  public static List<Consumer<ModifiableRootModel>> getUpdateProjectStructureActions(Module module, Collection<VirtualFile> appRoots,
                                                                                      final boolean auxModule, MvcProjectStructure structure) {
    for (Iterator<VirtualFile> iterator = appRoots.iterator(); iterator.hasNext();) {
      VirtualFile appRoot = iterator.next();
      if (!checkValidity(appRoot)) {
        iterator.remove();
      }
    }

    for (final VirtualFile file : ModuleRootManager.getInstance(module).getContentRoots()) {
      checkValidity(file);
    }

    List<Consumer<ModifiableRootModel>> actions = CollectionFactory.arrayList();
    removeInvalidSourceRoots(module, actions, auxModule, structure);
    cleanupDefaultLibrary(module, actions, structure.getDefaultLibraryName());

    for (VirtualFile file : appRoots) {
      ContainerUtil.addIfNotNull(addSourceRootsAndLibDirectory(file, module, file, structure), actions);
    }

    return actions;
  }

  public static void removeInvalidSourceRoots(Module module, List<Consumer<ModifiableRootModel>> actions, boolean auxModule, MvcProjectStructure structure) {


    final Set<SourceFolder> toRemove = CollectionFactory.newTroveSet();
    final Set<ContentEntry> toRemoveContent = CollectionFactory.newTroveSet();
    for (ContentEntry entry : ModuleRootManager.getInstance(module).getContentEntries()) {
      final VirtualFile file = entry.getFile();
      if (file == null || !structure.isValidContentRoot(file)) {
        toRemoveContent.add(entry);
      }

      boolean removeAllSources = true;
      for (SourceFolder folder : entry.getSourceFolders()) {
        if (folder.getFile() == null) {
          toRemove.add(folder);
        } else {
          removeAllSources = false;
        }
      }
      if (auxModule && removeAllSources) {
        toRemoveContent.add(entry);
      }
    }

    if (!toRemove.isEmpty() || !toRemoveContent.isEmpty()) {
      actions.add(new Consumer<ModifiableRootModel>() {
        public void consume(ModifiableRootModel model) {
          for (final ContentEntry entry : toRemoveContent) {
            model.removeContentEntry(entry);
          }

          for (ContentEntry entry : model.getContentEntries()) {
            for (SourceFolder folder : entry.getSourceFolders()) {
              if (toRemove.remove(folder)) {
                entry.removeSourceFolder(folder);
              }
            }
          }
        }
      });
    }
  }

  public static void cleanupDefaultLibrary(Module module, List<Consumer<ModifiableRootModel>> actions, final String libName) {
    final Library library = findDefaultLibrary(module, libName);
    if (library == null) {
      return;
    }

    final VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
    final Set<String> urls = CollectionFactory.newTroveSet(library.getUrls(OrderRootType.CLASSES_AND_OUTPUT));
    for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();) {
      if (virtualFileManager.findFileByUrl(iterator.next()) != null) {
        iterator.remove();
      }
    }

    if (!urls.isEmpty()) {
      actions.add(new Consumer<ModifiableRootModel>() {
        public void consume(ModifiableRootModel model) {
          final Library.ModifiableModel modifiableModel = modifyDefaultLibrary(model, libName);
          for (String url : urls) {
            modifiableModel.removeRoot(url, OrderRootType.CLASSES_AND_OUTPUT);
          }
          modifiableModel.commit();
        }
      });
    }
  }

  public static boolean hasModulesWithSupport(Project project, final MvcFramework framework) {
    return !getAllModulesWithSupport(project, framework).isEmpty();
  }

  public static List<Module> getAllModulesWithSupport(Project project, MvcFramework framework) {
    List<Module> modules = new ArrayList<Module>();
    for (Module module : ModuleManager.getInstance(project).getModules()){
      if (framework.hasSupport(module) && !framework.isCommonPluginsModule(module)) {
        modules.add(module);
      }
    }
    return modules;
  }

  public static void syncAuxModuleSdk(@NotNull Module appModule, @NotNull Module pluginsModule) {
    final ModuleRootManager auxRootManager = ModuleRootManager.getInstance(pluginsModule);
    final ModuleRootManager appRootManager = ModuleRootManager.getInstance(appModule);
    if (!Comparing.equal(auxRootManager.getSdk(), appRootManager.getSdk())) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          final ModifiableRootModel model = auxRootManager.getModifiableModel();
          copySdk(appRootManager, model);
          model.commit();
        }
      });
    }
  }

  @Nullable
  public static PropertiesFile findApplicationProperties(@NotNull Module module, MvcFramework framework) {
    VirtualFile root = framework.findAppRoot(module);
    if (root == null) return null;

    PsiManager manager = PsiManager.getInstance(module.getProject());
    VirtualFile appChild = root.findChild(APPLICATION_PROPERTIES);
    if (appChild == null || !appChild.isValid()) return null;

    PsiFile psiFile = manager.findFile(appChild);
    if (psiFile instanceof PropertiesFile) {
      return (PropertiesFile)psiFile;
    }
    return null;
  }

  public static Set<VirtualFile> getAllVisiblePluginDirectories(Module module, MvcFramework framework) {
    final Map<String, String> pluginInfos = framework.getInstalledCommonPluginVersions(module);

    VirtualFile root = framework.findAppRoot(module);
    VirtualFile oldPluginsDir = root == null ? null : root.findChild(PLUGINS_DIRECTORY);
    final VirtualFile projectWorkDir = framework.getProjectWorkDir(module);
    VirtualFile projectPluginsDir = projectWorkDir == null ? null : projectWorkDir.findChild(PLUGINS_DIRECTORY);
    Set<VirtualFile> pluginDirs = new THashSet<VirtualFile>();
    for (final String pluginName : pluginInfos.keySet()) {
      final String version = pluginInfos.get(pluginName);
      final String dirName = version == null ? pluginName : pluginName + "-" + version;
      if (projectPluginsDir != null) {
        final VirtualFile candidate = projectPluginsDir.findChild(dirName);
        if (candidate != null && candidate.isDirectory()) {
          pluginDirs.add(candidate);
          continue;
        }
      }
      if (oldPluginsDir != null) {
        final VirtualFile candidate = oldPluginsDir.findChild(dirName);
        if (candidate != null && candidate.isDirectory()) {
          pluginDirs.add(candidate);
        }
      }
    }


    final String sdkWorkDir = framework.getSdkWorkDir(module);
    final VirtualFile globalPluginsDir = LocalFileSystem.getInstance().findFileByPath(sdkWorkDir + "/plugins");
    if (globalPluginsDir != null) {
      for (final VirtualFile globalPlugin : globalPluginsDir.getChildren()) {
        if (globalPlugin.isDirectory()) {
          pluginDirs.add(globalPlugin);
        }
      }
    }
    return pluginDirs;
  }

  public static void removeAuxiliaryModule(Module appModule, Module toRemove) {
    List<ModifiableRootModel> usingModels = new SmartList<ModifiableRootModel>();

    for (Module module : ModuleManager.getInstance(appModule.getProject()).getModules()) {
      if (module == toRemove) {
        continue;
      }

      for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
        if (entry instanceof ModuleOrderEntry && toRemove == ((ModuleOrderEntry)entry).getModule()) {
          usingModels.add(ModuleRootManager.getInstance(module).getModifiableModel());
          break;
        }
      }
    }

    final ModifiableModuleModel moduleModel = ModuleManager.getInstance(toRemove.getProject()).getModifiableModel();

    ModuleDeleteProvider.removeModule(toRemove, null, usingModels, moduleModel);

    moduleModel.commit();
    for (final ModifiableRootModel usingModel : usingModels) {
      usingModel.commit();
    }
  }

  @NotNull
  public static Module createAuxiliaryModule(@NotNull Module appModule, final String moduleName, boolean forCustomPlugin,
                                      final MvcFramework framework) {
    ModuleManager moduleManager = ModuleManager.getInstance(appModule.getProject());
    final ModifiableModuleModel moduleModel = moduleManager.getModifiableModel();
    final String moduleFilePath = new File(appModule.getModuleFilePath()).getParent() + "/" + moduleName + ".iml";
    final VirtualFile existing = LocalFileSystem.getInstance().findFileByPath(moduleFilePath);
    if (existing != null) {
      try {
        existing.delete("Grails/Griffon plugins maintenance");
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }

    moduleModel.newModule(moduleFilePath, StdModuleTypes.JAVA);
    moduleModel.commit();

    Module pluginsModule = moduleManager.findModuleByName(moduleName);
    assert pluginsModule != null;

    ModifiableRootModel newRootModel = ModuleRootManager.getInstance(pluginsModule).getModifiableModel();
    ModifiableRootModel appModel = ModuleRootManager.getInstance(appModule).getModifiableModel();

    copySdk(appModel, newRootModel);

    for (final OrderEntry orderEntry : appModel.getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry) {
        final Library library = ((LibraryOrderEntry)orderEntry).getLibrary();
        if (library != null && framework.isSDKLibrary(library) && library.getTable() != null) {
          newRootModel.addLibraryEntry(library);
        }
      }
    }

    if (forCustomPlugin) {
      final Module commonPluginsModule = framework.findCommonPluginsModule(appModule);
      if (commonPluginsModule != null) {
        newRootModel.addModuleOrderEntry(commonPluginsModule);
      }
    }

    newRootModel.commit();
    appModel.commit();

    ensureDependency(appModule, pluginsModule);

    return pluginsModule;
  }

  public static void ensureDependency(@NotNull Module from, @NotNull Module to) {
    if (!from.equals(to) && !hasDependency(from, to)) {
      final ModifiableRootModel fromModel = ModuleRootManager.getInstance(from).getModifiableModel();
      fromModel.addModuleOrderEntry(to);
      fromModel.commit();
    }
  }

  private static boolean hasDependency(Module from, Module to) {
    for (OrderEntry entry : ModuleRootManager.getInstance(from).getOrderEntries()) {
      if (entry instanceof ModuleOrderEntry) {
        final ModuleOrderEntry moduleOrderEntry = (ModuleOrderEntry)entry;
        if (to == moduleOrderEntry.getModule()) {
          return true;
        }
      }
    }
    return false;
  }

  public static void removeDependency(@NotNull Module from, @NotNull Module to) {
    if (!from.equals(to) && hasDependency(from, to)) {
      final ModifiableRootModel fromModel = ModuleRootManager.getInstance(from).getModifiableModel();
      for (OrderEntry entry : fromModel.getOrderEntries()) {
        if (entry instanceof ModuleOrderEntry) {
          final ModuleOrderEntry moduleOrderEntry = (ModuleOrderEntry)entry;
          if (to == moduleOrderEntry.getModule()) {
            fromModel.removeOrderEntry(moduleOrderEntry);
          }
        }
      }
      fromModel.commit();
    }
  }

  public static void copySdk(ModuleRootModel from, ModifiableRootModel to) {
    if (from.isSdkInherited()) {
      to.inheritSdk();
    } else {
      to.setSdk(from.getSdk());
    }
  }

  public static Consumer<ModifiableRootModel> removeStaleContentEntries(final Collection<VirtualFile> pluginDirs) {
    return new Consumer<ModifiableRootModel>() {
      public void consume(ModifiableRootModel modifiableRootModel) {
        for (final ContentEntry entry : modifiableRootModel.getContentEntries()) {
          if (!pluginDirs.contains(entry.getFile())) {
            modifiableRootModel.removeContentEntry(entry);
          }
        }
      }
    };
  }

  public static Consumer<ModifiableRootModel> updateFrameworkSdkInPluginsModule(final Module appModule, final MvcFramework framework) {
    return new Consumer<ModifiableRootModel>() {
      public void consume(ModifiableRootModel model) {
        Library sdkLib = null;
          for (final OrderEntry orderEntry : ModuleRootManager.getInstance(appModule).getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry) {
              final Library library = ((LibraryOrderEntry)orderEntry).getLibrary();
              if (library != null && framework.isSDKLibrary(library) && library.getTable() != null) {
                sdkLib = library;
              }
            }
          }

          if (sdkLib != null) {
            for (OrderEntry entry : model.getOrderEntries()) {
              if (entry instanceof LibraryOrderEntry &&
                  framework.isSDKLibrary(((LibraryOrderEntry)entry).getLibrary())) {
                model.removeOrderEntry(entry);
              }
            }
            model.addLibraryEntry(sdkLib);
          }
      }
    };
  }

  public static void updateAuxModuleStructure(Module appModule, Module auxModule, Collection<VirtualFile> pluginDirs, MvcFramework framework) {
    ensureDependency(appModule, auxModule);

    final MvcProjectStructure structure = framework.createProjectStructure(auxModule, true);
    final List<Consumer<ModifiableRootModel>> actions = getUpdateProjectStructureActions(auxModule, pluginDirs, true, structure);
    for (final ContentEntry root : ModuleRootManager.getInstance(auxModule).getContentEntries()) {
      if (!pluginDirs.contains(root.getFile())) {
        actions.add(removeStaleContentEntries(pluginDirs));
        break;
      }
    }

    if (framework.getSdkRoot(appModule) != framework.getSdkRoot(auxModule)) {
      actions.add(updateFrameworkSdkInPluginsModule(appModule, framework));
    }

    if (!actions.isEmpty()) {
      actions.add(exportDefaultLibrary(structure.getDefaultLibraryName()));
    }

    if (!actions.isEmpty()) {
      final ModifiableRootModel model = ModuleRootManager.getInstance(auxModule).getModifiableModel();
      for (final Consumer<ModifiableRootModel> pluginsUpdateAction : actions) {
        pluginsUpdateAction.consume(model);
      }
      model.commit();
    }
  }

  public static Consumer<ModifiableRootModel> exportDefaultLibrary(final String libraryName) {
    return new Consumer<ModifiableRootModel>() {
      public void consume(ModifiableRootModel modifiableRootModel) {
        for (final OrderEntry entry : modifiableRootModel.getOrderEntries()) {
          if (entry instanceof LibraryOrderEntry) {
            final LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry)entry;
            if (libraryName.equals(libraryOrderEntry.getLibraryName())) {
              libraryOrderEntry.setExported(true);
            }
          }
        }
      }
    };
  }

  public static void updateAuxiliaryPluginsModuleRoots(Module appModule, MvcFramework framework) {
    final Set<VirtualFile> pluginDirs = getAllVisiblePluginDirectories(appModule, framework);
    for (Iterator<VirtualFile> it = pluginDirs.iterator(); it.hasNext();) {
      VirtualFile pluginDir = it.next();
      if (!checkValidity(pluginDir)) {
        it.remove();
      }
    }

    Module pluginsModule = framework.findCommonPluginsModule(appModule);
    if (pluginDirs.isEmpty()) {
      if (pluginsModule != null) {
        removeAuxiliaryModule(appModule, pluginsModule);
      }
      return;
    }

    if (pluginsModule == null) {
      pluginsModule = createAuxiliaryModule(appModule, framework.getCommonPluginsModuleName(appModule), false, framework);
    }

    updateAuxModuleStructure(appModule, pluginsModule, pluginDirs, framework);
  }

}
