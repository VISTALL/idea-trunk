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

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.config.AbstractConfigUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.util.GroovyConstantExpressionEvaluator;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;
import org.jetbrains.plugins.groovy.mvc.MvcModuleStructureUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peter
 */
public class GrailsModuleStructureUtil {
  @NonNls private static final String PLUGINS_MODULE_SUFFIX = "-grailsPlugins";
  @NonNls static final String CUSTOM_PLUGINS_MODULE_INFIX = "-grailsPlugin-";
  @NonNls static final String GRAILS_VERSION_KEY = "app.grails.version";
  @NonNls static final String UPGRADE_COMMAND = "upgrade";
  @NonNls public static final String BUILD_CONFIG_FILE = "BuildConfig.groovy";

  private GrailsModuleStructureUtil() {
  }

  public static Map<String, Module> getCustomPluginModules(Module module) {
    final Map<String, Module> customPluginModules = new THashMap<String, Module>();

    final Module commonPluginsModule = GrailsFramework.INSTANCE.findCommonPluginsModule(module);
    for (Module dependent : ModuleRootManager.getInstance(module).getDependencies()) {
      if (dependent == commonPluginsModule) {
        continue;
      }

      final String pluginName = GrailsUtils.extractGrailsPluginName(dependent);
      if (pluginName != null) {
        customPluginModules.put(pluginName, dependent);
      }
    }

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(module.getProject()).getFileIndex();
    final Map<String, VirtualFile> locations = getCustomPluginLocations(module);
    for (final String pluginName : locations.keySet()) {
      final VirtualFile root = locations.get(pluginName);
      if (root != null) {
        final Module candidate = fileIndex.getModuleForFile(root);
        if (candidate == commonPluginsModule) {
          continue;
        }

        customPluginModules.put(pluginName, candidate);
      }
    }

    return customPluginModules;
  }

  static String getCommonPluginsModuleName(Module module) {
    return module.getName() + PLUGINS_MODULE_SUFFIX;
  }

  static Map<String, VirtualFile> getCustomPluginLocations(Module module) {
    final Map<String, VirtualFile> customLocatedPlugins = new HashMap<String, VirtualFile>();
    final VirtualFile root = GrailsUtils.findGrailsAppRoot(module);
    if (root != null) {
      final VirtualFile vFile = root.findFileByRelativePath("grails-app/conf/" + BUILD_CONFIG_FILE);
      if (vFile != null) {
        final PsiFile file = PsiManager.getInstance(module.getProject()).findFile(vFile);
        if (file instanceof GroovyFile) {
          file.getManager().getSearchHelper().processElementsWithWord(new TextOccurenceProcessor() {
            public boolean execute(PsiElement element, int offsetInElement) {
              if (element instanceof GrAssignmentExpression) {
                final GrAssignmentExpression expression = (GrAssignmentExpression)element;
                final GrExpression value = expression.getLValue();
                if (value instanceof GrReferenceExpression) {
                  final GrReferenceExpression referenceExpression = (GrReferenceExpression)value;
                  final PsiElement qualifier = referenceExpression.getQualifier();
                  if (qualifier != null && qualifier.getText().replaceAll(" ", "").equals("grails.plugin.location")) {
                    final Object location = GroovyConstantExpressionEvaluator.evaluate(expression.getRValue());
                    final String pluginName = referenceExpression.getReferenceName();
                    customLocatedPlugins.put(pluginName, findPluginRoot(location, root));
                  }
                }
              }
              return true;
            }
          }, new LocalSearchScope(file), "location", UsageSearchContext.IN_CODE, true);
        }
      }
    }
    return customLocatedPlugins;
  }

  @Nullable
  private static VirtualFile findPluginRoot(@Nullable Object location, @NotNull VirtualFile root) {
    if (location instanceof String) {
      String path = (String)location;
      VirtualFile pluginRoot = root.findFileByRelativePath(path);
      return pluginRoot == null ? root.getFileSystem().findFileByPath(path) : pluginRoot;
    }
    return null;
  }

  public static boolean isCommonPluginsModule(@NotNull Module module) {
    return module.getName().endsWith(PLUGINS_MODULE_SUFFIX);
  }

  public static boolean isCustomPluginModule(Module module) {
    return module.getName().contains(CUSTOM_PLUGINS_MODULE_INFIX);
  }

  private static final Key<String> LAST_GRAILS_VERSION = Key.create("LAST_GRAILS_VERSION");
  static void upgradeGrails(final Module module) {
    String libVersion = GrailsConfigUtils.getInstance().getGrailsVersion(module);
    if (libVersion == null || AbstractConfigUtils.UNDEFINED_VERSION.equals(libVersion)) {
      return;
    }

    if (Comparing.equal(module.getUserData(LAST_GRAILS_VERSION), libVersion)) {
      return; //we've already asked
    }

    PropertiesFile file = MvcModuleStructureUtil.findApplicationProperties(module, GrailsFramework.INSTANCE);
    if (file == null) {
      return;
    }

    final Property property = file.findPropertyByKey(GRAILS_VERSION_KEY);
    if (property == null) {
      return;
    }

    final String appVersion = property.getValue();
    if (!libVersion.equals(appVersion)) {
      module.putUserData(LAST_GRAILS_VERSION, libVersion);
      int result = Messages.showOkCancelDialog(GrailsBundle.message("grails.malformed.version", appVersion, libVersion),
                                               GrailsBundle.message("grails.upgrade.app"), GrailsIcons.GRAILS_MODULE_ICON);
      if (result == 0) {
        ProcessBuilder pb = GrailsUtils.createGrailsCommand(module, UPGRADE_COMMAND);
        MvcConsole.getInstance(module.getProject()).executeProcess(module, pb, null, true, GrailsUtils.OUR_ANSWER_TO_LORD_CURZON + GrailsUtils.OUR_ANSWER_TO_LORD_CURZON);
      }
    }
  }

  static boolean isIdeaGeneratedPluginModule(Module pluginModule) {
    return pluginModule.getName().endsWith(CUSTOM_PLUGINS_MODULE_INFIX + GrailsUtils.extractGrailsPluginName(pluginModule));
  }
}
