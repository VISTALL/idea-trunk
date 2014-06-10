/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.mvc;

import com.intellij.ProjectTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.config.GrailsModuleStructureUtil;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.griffon.GriffonFramework;

import java.util.*;

/**
 * @author peter
 */
public class MvcModuleStructureSynchronizer extends AbstractProjectComponent {
  private static final MvcFramework[] ourFrameworks = {GrailsFramework.INSTANCE, GriffonFramework.INSTANCE};
  private final Set<Pair<Object, SyncAction>> myActions = new LinkedHashSet<Pair<Object, SyncAction>>();

  public MvcModuleStructureSynchronizer(Project project) {
    super(project);
  }

  public void initComponent() {
    final MessageBusConnection connection = myProject.getMessageBus().connect();
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(ModuleRootEvent event) {
        queue(SyncAction.SyncSdkWithPluginsModule, myProject);
        queue(SyncAction.UpgradeGrails, myProject);
        queue(SyncAction.CreateAppStructureIfNeeeded, myProject);
      }
    });

    connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
      public void moduleAdded(Project project, Module module) {
        queue(SyncAction.UpdateProjectStructure, module);
        queue(SyncAction.CreateAppStructureIfNeeeded, module);
      }

      public void beforeModuleRemoved(Project project, Module module) {
      }

      public void moduleRemoved(Project project, Module module) {
      }

      public void modulesRenamed(Project project, List<Module> modules) {
      }
    });

    connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(new VirtualFileAdapter() {
      public void fileCreated(final VirtualFileEvent event) {
        final VirtualFile file = event.getFile();
        final String fileName = event.getFileName();
        if (MvcModuleStructureUtil.APPLICATION_PROPERTIES.equals(fileName) ||
            GrailsUtils.GRAILS_APP_DIRECTORY.equals(fileName) ||
            GrailsModuleStructureUtil.BUILD_CONFIG_FILE.equals(fileName)) {
          queue(SyncAction.UpdateProjectStructure, file);
          queue(SyncAction.EnsureRunConfigurationExists, file);
        }
        else if (isPluginsOrLibDirectory(file) || isPluginsOrLibDirectory(event.getParent())) {
          queue(SyncAction.UpdateProjectStructure, file);
        }
      }

      @Override
      public void fileDeleted(VirtualFileEvent event) {
        final VirtualFile file = event.getFile();
        if (isPluginsOrLibDirectory(file) || isPluginsOrLibDirectory(event.getParent())) {
          queue(SyncAction.UpdateProjectStructure, file);
        }
      }

      @Override
      public void contentsChanged(VirtualFileEvent event) {
        final String fileName = event.getFileName();
        if (MvcModuleStructureUtil.APPLICATION_PROPERTIES.equals(fileName) || GrailsModuleStructureUtil.BUILD_CONFIG_FILE.equals(fileName)) {
          queue(SyncAction.UpdateProjectStructure, event.getFile());
        }
      }
    }));

  }

  private static boolean isPluginsOrLibDirectory(@Nullable final VirtualFile file) {
    return file != null && (MvcModuleStructureUtil.PLUGINS_DIRECTORY.equals(file.getName()) || "lib".equals(file.getName()));
  }

  public void projectOpened() {
    queue(SyncAction.UpdateProjectStructure, myProject);
    queue(SyncAction.EnsureRunConfigurationExists, myProject);
    queue(SyncAction.UpgradeGrails, myProject);
    queue(SyncAction.CreateAppStructureIfNeeeded, myProject);
  }

  private void queue(SyncAction action, Object on) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    synchronized (myActions) {
      if (myActions.isEmpty()) {
        StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
          public void run() {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                runActions();
              }
            }, ModalityState.NON_MODAL);
          }
        });
      }

      myActions.add(new Pair<Object, SyncAction>(on, action));
    }
  }

  @NotNull
  private List<Module> determineModuleBySyncActionObject(Object o) {
    if (o instanceof Module) {
      return Arrays.asList((Module)o);
    }
    if (o instanceof Project) {
      return Arrays.asList(ModuleManager.getInstance((Project)o).getModules());
    }
    if (o instanceof VirtualFile) {
      final VirtualFile file = (VirtualFile)o;
      if (file.isValid()) {
        final Module module = ModuleUtil.findModuleForFile(file, myProject);
        if (module == null) {
          return Collections.emptyList();
        }

        if (GrailsModuleStructureUtil.isCommonPluginsModule(module)) {
          return Collections.emptyList();
        }

        return Arrays.asList(module);
      }
    }
    return Collections.emptyList();
  }

  @Nullable
  public static MvcFramework getFramework(@Nullable Module module) {
    if (module == null) {
      return null;
    }

    for (final MvcFramework framework : ourFrameworks) {
      if (framework.getSdkRoot(module) != null) {
        return framework;
      }
    }
    return null;
  }

  private void runActions() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    if (myProject.isDisposed()) {
      return;
    }

    final Set<Trinity<Module, SyncAction, MvcFramework>> rawActions = new LinkedHashSet<Trinity<Module, SyncAction, MvcFramework>>();
    //get module by object and kill duplicates
    synchronized (myActions) {
      for (final Pair<Object, SyncAction> pair : myActions) {
        for (Module module : determineModuleBySyncActionObject(pair.first)) {
          if (!module.isDisposed()) {
            final MvcFramework framework = getFramework(module);
            if (framework != null && !framework.isCommonPluginsModule(module)) {
              rawActions.add(Trinity.create(module, pair.second, framework));
            }
          }
        }
      }
    }

    for (final Trinity<Module, SyncAction, MvcFramework> rawAction : rawActions) {
      final Module module = rawAction.first;
      if (module.isDisposed()) {
        continue;
      }

      final MvcFramework synchronizer = rawAction.third;
      switch (rawAction.second) {
        case CreateAppStructureIfNeeeded:
          synchronizer.createApplicationIfNeeded(module);
          break;
        case UpdateProjectStructure:
          if (synchronizer.findAppRoot(module) != null) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              public void run() {
                synchronizer.updateProjectStructure(module);
              }
            });
          }
          break;
        case UpgradeGrails:
          synchronizer.upgradeFramework(module);
          break;
        case EnsureRunConfigurationExists:
          synchronizer.ensureRunConfigurationExists(module);
          break;
        case SyncSdkWithPluginsModule:
          synchronizer.syncSdkAndPluginsModule(module);
      }
    }
    // if there were any actions added during performSyncAction, clear them too
    // all needed actions are already added to buffer and have thus been performed
    // otherwise you may get repetitive 'run create-app?' questions 
    synchronized (myActions) {
      myActions.clear();
    }
  }

  enum SyncAction {
    SyncSdkWithPluginsModule,
    UpgradeGrails,
    CreateAppStructureIfNeeeded,
    UpdateProjectStructure,
    EnsureRunConfigurationExists

  }

}
