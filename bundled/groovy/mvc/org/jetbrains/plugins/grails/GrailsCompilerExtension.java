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

package org.jetbrains.plugins.grails;

import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.PathsList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.groovy.grails.GrailsInjectingPatcher;
import org.jetbrains.plugins.grails.config.GrailsConfigUtils;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.config.GrailsModuleStructureUtil;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.compiler.GroovyCompilerExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public class GrailsCompilerExtension extends GroovyCompilerExtension {

  @Override
  public void enhanceCompilationClassPath(@NotNull ModuleChunk chunk, @NotNull PathsList classPath) {
    for (final Module module : chunk.getModules()) {
      if (shouldInjectGrails(module)) {
        final VirtualFile grailsHome = GrailsFramework.INSTANCE.getSdkRoot(module);
        if (grailsHome != null) {
          classPath.add(PathUtil.getJarPathForClass(GrailsInjectingPatcher.class));
          return;
        }
      }
    }
  }

  private static boolean shouldInjectGrails(Module module) {
    if (GrailsConfigUtils.isAtLeastGrails1_2(module)) {
      return false;
    }

    return GrailsUtils.hasGrailsSupport(module) ||
           GrailsModuleStructureUtil.isCommonPluginsModule(module) ||
           GrailsModuleStructureUtil.isCustomPluginModule(module);
  }

  @Override
  @NotNull
  public List<String> getCompilationUnitPatchers(@NotNull ModuleChunk chunk) {
    for (final Module module : chunk.getModules()) {
      if (shouldInjectGrails(module) && GrailsFramework.INSTANCE.getSdkRoot(module) != null) {
        return Arrays.asList(GrailsInjectingPatcher.class.getName());
      }
    }
    return Collections.emptyList();
  }
}
