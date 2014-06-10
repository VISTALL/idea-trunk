/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.refactoring.GroovyNamesUtil;

import java.util.Arrays;
import java.util.List;

public class GrailsUtils {
  @NonNls public static final String GRAILS_INTEGRATION_TESTS = "test/integration/";
  @NonNls public static final String GRAILS_UNIT_TESTS = "test/unit/";
  @NonNls public static final String TAGLIB_DIRECTORY = "taglib";
  @NonNls public static final String MOCK_TAGLIB_DIRECTORY = "mockTagLib";
  @NonNls public static final String GRAILS_APP_DIRECTORY = "grails-app";
  @NonNls public static final String SCRIPTS_DIRECTORY = "scripts";
  @NonNls public static final String VIEWS_DIRECTORY = "views";
  @NonNls public static final String SERVICES_DIRECTORY = "services";

  @NonNls public static final String SOURCE_ROOT = "src";
  @NonNls public static final String JAVA_SOURCE_ROOT = "java";

  @NonNls public static final String webAppDir = "web-app";
  @NonNls public static final String metaInfDir = "META-INF";
  @NonNls public static final String webInfDir = "WEB-INF";
  @NonNls public static final String jsDir = "js";
  @NonNls public static final String cssDir = "css";
  @NonNls public static final String imagecDir = "images";

  @NonNls public static final String OUR_ANSWER_TO_LORD_CURZON = "y\n";

  @NonNls public static final String GRAILS_USER_LIBRARY = "Grails User Library";

  @NonNls public static final String CONTROLLER_SUFFIX = "Controller";
  @NonNls public static final String TAGLIB_SUFFIX = "TagLib";

  @NonNls public static final String TESTS_SUFFIX = "Tests";
  @NonNls public static final String DOMAIN_DIRECTORY = "domain";
  @NonNls public static final String CONTROLLER_DIRECTORY = "controllers";
  @NonNls public static final String GROOVY_EXTENSION = ".groovy";
  @NonNls public static final String TEST_DIR = "test";
  @NonNls public static final String INTEGRATION_DIR = "integration";

  // Grails run configuration
  @NonNls public static String GRAILS_RUN_DEFAULT_HOST = "localhost";
  @NonNls public static String GRAILS_RUN_DEFAULT_PORT = "8080";

  private GrailsUtils() {
  }


  @Nullable
  public static VirtualFile findParent(VirtualFile virtualFile, String parentFileName) {
    VirtualFile parent = virtualFile.getParent();

    while (parent != null && parent.isDirectory() && !parentFileName.equals(parent.getNameWithoutExtension())) {
      parent = parent.getParent();
    }

    return parent;
  }

  public static boolean isControllerClassFile(VirtualFile file, Project project) {
    if (file == null) return false;
    // Stub for test needs
    if (ApplicationManager.getApplication().isUnitTestMode()) return true;

    final Module module = ModuleUtil.findModuleForFile(file, project);
    VirtualFile directory = findControllersDirectory(module);
    return directory != null && VfsUtil.isAncestor(directory, file, true);
  }

  public static boolean isUnderWebAppDirectory(PsiDirectory directory) {
    VirtualFile virtualFile = directory.getVirtualFile();

    String[] reservedDirs = new String[]{metaInfDir, webInfDir, jsDir, cssDir, imagecDir};
    List<String> reservedList = Arrays.asList(reservedDirs);

    if (virtualFile.getNameWithoutExtension().equals(webAppDir) && virtualFile.isDirectory()) {
      return true;
    }
    VirtualFile parent = findParent(virtualFile, webAppDir);
    if (parent == null || reservedList.contains(virtualFile.getNameWithoutExtension())) return false;

    for (String reservedDir : reservedDirs) {
      if (findParent(virtualFile, reservedDir) != null) {
        return false;
      }
    }

    return true;
  }

  public static boolean isUnderGrailsViewsDirectory(PsiDirectory directory) {
    VirtualFile virtualFile = directory.getVirtualFile();
    VirtualFile parent;
    if (virtualFile.getNameWithoutExtension().equals(VIEWS_DIRECTORY) && virtualFile.isDirectory()) {
      parent = virtualFile;
    }
    else {
      parent = findParent(virtualFile, VIEWS_DIRECTORY);
    }

    if (parent != null) {
      VirtualFile grandParent = parent.getParent();
      if (grandParent != null && grandParent.getNameWithoutExtension().equals(GRAILS_APP_DIRECTORY)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public static VirtualFile findGrailsAppRoot(@Nullable Module module) {
    if (module == null) return null;
    final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
    if (contentRoots.length == 0) return null;

    for (VirtualFile contentRoot : contentRoots) {
      if (contentRoot.findChild(GRAILS_APP_DIRECTORY) != null) return contentRoot;
    }
    return null;
  }

  public static ProcessBuilder createGrailsCommand(@NotNull Module module, String... cmdLine) {
    return GrailsFramework.INSTANCE.createCommand(module, false, cmdLine);
  }

  public static boolean isGrailsPluginModule(@Nullable Module module) {
    return extractGrailsPluginName(module) != null;
  }

  @Nullable
  public static String extractGrailsPluginName(@Nullable Module module) {
    final VirtualFile root = findGrailsAppRoot(module);
    if (root == null) return null;

    for (VirtualFile child : root.getChildren()) {
      final String name = child.getName();
      if (name.endsWith("GrailsPlugin.groovy")) {
        return GroovyNamesUtil.camelToSnake(StringUtil.trimEnd(name, "GrailsPlugin.groovy"));
      }
    }
    return null;
  }


  @Nullable
  public static String getDomainClassPackageName(VirtualFile vFile, Module module) {
    return getDomainOrControllerClassPackageName(vFile, module, DOMAIN_DIRECTORY);
  }

  @Nullable
  public static String getControllerClassPackageName(VirtualFile vDir, Module module) {
    return getDomainOrControllerClassPackageName(vDir, module, CONTROLLER_DIRECTORY);
  }

  public static boolean isDomainClass(VirtualFile virtualFile, Module module) {
    if (virtualFile == null) return false;
    VirtualFile dir = virtualFile.getParent();
    return dir != null && getDomainClassPackageName(virtualFile, module) != null;
  }

  public static boolean isControllerClass(VirtualFile virtualFile, Module module) {
    if (virtualFile == null) return false;
    String name = virtualFile.getName();
    if (!name.endsWith(CONTROLLER_SUFFIX + GROOVY_EXTENSION)) return false;
    VirtualFile dir = virtualFile.getParent();
    return dir != null && getControllerClassPackageName(dir, module) != null;
  }

  public static boolean isDomainClassTest(VirtualFile virtualFile, Module module) {
    if (virtualFile == null) return false;
    String name = virtualFile.getName();
    if (!name.endsWith(TESTS_SUFFIX + GROOVY_EXTENSION) || name.endsWith(CONTROLLER_SUFFIX + TESTS_SUFFIX + GROOVY_EXTENSION)) return false;
    return isTestFile(virtualFile, module);
  }

  public static boolean isControllerClassTest(VirtualFile virtualFile, Module module) {
    if (virtualFile == null) return false;
    String name = virtualFile.getName();
    return name.endsWith(CONTROLLER_SUFFIX + TESTS_SUFFIX + GROOVY_EXTENSION) && isTestFile(virtualFile, module);
  }

  private static boolean isTestFile(VirtualFile virtualFile, Module module) {
    VirtualFile moduleDir = findGrailsAppRoot(module);
    if (moduleDir == null) return false;
    VirtualFile testDir = moduleDir.findChild(TEST_DIR);
    if (testDir == null) return false;
    return VfsUtil.isAncestor(testDir, virtualFile, true);
  }

  @Nullable
  private static String getDomainOrControllerClassPackageName(VirtualFile vDir, Module module, String dirName) {
    VirtualFile grailsAppDir = findModuleGrailsAppDir(module);
    if (grailsAppDir == null) return null;
    VirtualFile domainDir = grailsAppDir.findFileByRelativePath(dirName);
    if (domainDir == null) return null;
    return VfsUtil.getRelativePath(vDir, domainDir, '.');
  }

  /**
   * @param file   Some file of Grails category, for instance, Foo.groovy as domain class, FooController.groovy as controller etc.
   * @param module module
   * @return category name
   */
  @Nullable
  public static String getCategoryName(PsiFile file, @NotNull Module module) {
    if (file == null) return null;
    if (file instanceof GroovyFile) {
      final GroovyFile groovyFile = (GroovyFile)file;
      final String packageName = groovyFile.getPackageName();
      final String prefix = packageName.length() > 0 ? packageName + "." : "";

      VirtualFile virtualFile = file.getVirtualFile();
      if (virtualFile == null) return null;
      String fileName = virtualFile.getName();
      if (isControllerClass(virtualFile, module)) {
        return prefix + StringUtil.capitalize(StringUtil.trimEnd(fileName, CONTROLLER_SUFFIX + GROOVY_EXTENSION));
      }
      if (isDomainClass(virtualFile, module)) {
        return prefix + StringUtil.capitalize(StringUtil.trimEnd(fileName, GROOVY_EXTENSION));
      }
      if (isDomainClassTest(virtualFile, module)) {
        return prefix + StringUtil.capitalize(StringUtil.trimEnd(fileName, TESTS_SUFFIX + GROOVY_EXTENSION));
      }
      if (isControllerClassTest(virtualFile, module)) {
        return prefix + StringUtil.capitalize(StringUtil.trimEnd(fileName, CONTROLLER_SUFFIX + TESTS_SUFFIX + GROOVY_EXTENSION));
      }
    } else if (file instanceof GspFile) {
      VirtualFile virtualFile = file.getVirtualFile();
      if (virtualFile == null) return null;
      VirtualFile parent = virtualFile.getParent();
      if (parent == null) return null;
      VirtualFile views = parent.getParent();
      if (views != null && views.isDirectory() && VIEWS_DIRECTORY.equals(views.getName())) {
        return StringUtil.capitalize(parent.getName());
      }
    }
    return null;
  }

  @Nullable
  public static VirtualFile findControllerClassFile(@NotNull String categoryName, @NotNull Module module) {
    final String name = categoryName.replace(".", "/");
    VirtualFile directory = findControllersDirectory(module);
    if (directory == null) return null;
    return directory.findFileByRelativePath(name + CONTROLLER_SUFFIX + GROOVY_EXTENSION);
  }

  @Nullable
  public static VirtualFile findDomainClassFile(@NotNull String categoryName, @NotNull Module module) {
    final String name = categoryName.replace(".", "/");     // "/" should be here because of findFileByRelativePath use separating on "/"  char
    VirtualFile directory = findDomainClassDirectory(module);
    if (directory == null) return null;
    return directory.findFileByRelativePath(name + GROOVY_EXTENSION);
  }


  @Nullable
  public static VirtualFile findControllersDirectory(Module module) {
    VirtualFile appDir = findModuleGrailsAppDir(module);
    if (appDir == null) return null;
    return appDir.findChild(CONTROLLER_DIRECTORY);
  }

  @Nullable
  public static VirtualFile findDomainClassDirectory(Module module) {
    VirtualFile appDir = findModuleGrailsAppDir(module);
    if (appDir == null) return null;
    return appDir.findChild(DOMAIN_DIRECTORY);
  }

  @Nullable
  public static VirtualFile findServicesDirectory(Module module) {
    VirtualFile appDir = findModuleGrailsAppDir(module);
    if (appDir == null) return null;
    return appDir.findChild(SERVICES_DIRECTORY);
  }

  @Nullable
  public static VirtualFile findTagLibDirectory(Module module) {
    VirtualFile appDir = findModuleGrailsAppDir(module);
    if (appDir == null) return null;
    return appDir.findChild(TAGLIB_DIRECTORY);
  }

  @Nullable
  public static VirtualFile findViewsDirectory(Module module) {
    VirtualFile appDir = findModuleGrailsAppDir(module);
    if (appDir == null) return null;
    return appDir.findChild(VIEWS_DIRECTORY);
  }

  @Nullable
  public static VirtualFile findModuleGrailsAppDir(Module module) {
    VirtualFile root = findGrailsAppRoot(module);
    if (root == null) return null;
    return root.findChild(GRAILS_APP_DIRECTORY);
  }

  @Nullable
  public static VirtualFile findScriptDirectory(Module module) {
    VirtualFile root = findGrailsAppRoot(module);
    if (root == null) return null;
    return root.findChild(SCRIPTS_DIRECTORY);
  }

  @Nullable
  public static VirtualFile findSourceDirectory(Module module) {
    VirtualFile root = findGrailsAppRoot(module);
    if (root == null) return null;
    return root.findChild(SOURCE_ROOT);
  }

  @Nullable
  public static VirtualFile findJavaSourceDirectory(Module module) {
    final VirtualFile sourceRoot = findSourceDirectory(module);
    if (sourceRoot == null) return null;
    return sourceRoot.findChild(JAVA_SOURCE_ROOT);
  }

  @Nullable
  public static PsiDirectory getIntegrationTestsDirectory(Module module) {
    VirtualFile appRoot = findGrailsAppRoot(module);
    if (appRoot == null) {
      return null;
    }

    VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(appRoot.getUrl() + "/" + GRAILS_INTEGRATION_TESTS);
    if (virtualFile != null) {
      return PsiManager.getInstance(module.getProject()).findDirectory(virtualFile);
    }
    return null;
  }

  @Nullable
  public static PsiDirectory getUnitTestsDirectory(@Nullable Module module) {
    VirtualFile appRoot = findGrailsAppRoot(module);
    if (appRoot == null) {
      return null;
    }

    assert module != null;
    VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(appRoot.getUrl() + "/" + GRAILS_UNIT_TESTS);
    if (virtualFile != null) {
      return PsiManager.getInstance(module.getProject()).findDirectory(virtualFile);
    }
    return null;
  }

  public static boolean isUnderGrailstestFolders(final Module module, final PsiClass clazz) {
    final PsiDirectory dir = clazz.getContainingFile().getContainingDirectory();
    final PsiDirectory integration = getIntegrationTestsDirectory(module);
    final PsiDirectory unit = getUnitTestsDirectory(module);
    if (dir != null &&
        (unit != null && PsiTreeUtil.findCommonParent(dir, unit) == unit ||
         integration != null && PsiTreeUtil.findCommonParent(dir, integration) == integration)) {
      return true;
    }
    return false;
  }

  public static boolean hasGrailsSupport(@Nullable final Module module) {
    return module != null && GrailsFramework.INSTANCE.hasSupport(module);

  }

  public static boolean isControllerClass(GrTypeDefinition grTypeDefinition, Module module) {
    return isControllerClass(grTypeDefinition.getContainingFile().getVirtualFile(), module);
  }
}
