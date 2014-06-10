/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package org.jetbrains.plugins.groovy.mvc;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.WatchedRootsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author peter
 */
public class MvcWatchedRootProvider implements WatchedRootsProvider {
  private final Project myProject;

  public MvcWatchedRootProvider(Project project) {
    myProject = project;
  }

  @Nullable
  public static String getProjectWorkDirPath(MvcFramework framework, @NotNull Module module) {
    final String grailsWorkDir = framework.getSdkWorkDir(module);
    if (grailsWorkDir == null) return null;

    final VirtualFile root = framework.findAppRoot(module);
    if (root == null) return null;

    return grailsWorkDir + "/projects/" + root.getName();
  }

  @NotNull
  public Set<String> getRootsToWatch() {
    final THashSet<String> result = new THashSet<String>();
    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      final MvcFramework framework = MvcModuleStructureSynchronizer.getFramework(module);
      if (framework != null) {
        final String path = getProjectWorkDirPath(framework, module);
        if (path != null) {
          result.add(path + "/plugins");
        }
      }
    }
    return result;
  }

}
