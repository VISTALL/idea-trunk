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

package org.jetbrains.plugins.grails.runner;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.config.GrailsConfigUtils;
import org.jetbrains.plugins.grails.tests.runner.GrailsTestRunConfigurationProducer;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition;

import javax.swing.*;

public class GrailsRunConfigurationType implements LocatableConfigurationType {
  @NonNls private static final String GRAILS_TESTS = "Grails Tests";
  @NonNls private static final String TESTS_SUFFIX = "Tests";
  private final GrailsConfigurationFactory myConfigurationFactory;
  @NonNls private static final String GRAILS_APPLICATION = "Grails Application";

  public GrailsRunConfigurationType() {
    myConfigurationFactory = new GrailsConfigurationFactory(this, GRAILS_APPLICATION, "run-app");
  }

  public String getDisplayName() {
    return GRAILS_APPLICATION;
  }

  public String getConfigurationTypeDescription() {
    return GRAILS_APPLICATION;
  }

  public Icon getIcon() {
    return GrailsIcons.GRAILS_ICON;
  }

  @NonNls
  @NotNull
  public String getId() {
    return "GrailsRunConfigurationType";
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myConfigurationFactory};
  }

  public static GrailsRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(GrailsRunConfigurationType.class);
  }

    public RunnerAndConfigurationSettings createConfigurationByLocation(Location location) {
    final PsiElement element = location.getPsiElement();
    final PsiClass clazz = getClassByElement(element);
    if (clazz != null) {
      final PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
      return createConfigurationByClass(clazz, method);
    }
    if (element instanceof PsiDirectory && GrailsTestRunConfigurationProducer.isTestDirectory(((PsiDirectory)element))) {
      return createConfigurationByDir(((PsiDirectory)element));
    }
    return null;
  }

  @NotNull
  private RunnerAndConfigurationSettings createConfigurationByDir(PsiDirectory dir) {
    final Project project = dir.getProject();
    RunnerAndConfigurationSettings settings = RunManagerEx.getInstanceEx(project).createConfiguration("", myConfigurationFactory);
    final GrailsRunConfiguration configuration = (GrailsRunConfiguration)settings.getConfiguration();
    final VirtualFile vfile = dir.getVirtualFile();
    final Module module = ModuleUtil.findModuleForFile(vfile, project);
    configuration.setName("Grails tests" + (module == null ? "" : ":" + module.getName()));
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final VirtualFile srcRoot = fileIndex.getSourceRootForFile(vfile);
    if (srcRoot != null) {
      configuration.cmdLine = "test-app";
      configuration.setName("Grails tests:" + srcRoot.getName());
      if (srcRoot.getName().equals("integration")) {
        configuration.cmdLine += " -integration";
      } else if (srcRoot.getName().equals("unit")) {
        configuration.cmdLine += " -unit";
      }

      final String pkg = fileIndex.getPackageNameByDirectory(vfile);
      if (StringUtil.isNotEmpty(pkg)) {
        configuration.cmdLine += " " + pkg + ".*";
        configuration.setName("Grails tests:" + pkg);
      }
    }

    return settings;
  }

  @Nullable
  private static PsiClass getClassByElement(PsiElement element) {
    final PsiFile file = element.getContainingFile();
    if (file instanceof GroovyFile) {
      GroovyFile groovyFile = (GroovyFile)file;
      if (!(groovyFile.isScript()) && groovyFile.getTypeDefinitions().length == 1) {
        final GrTypeDefinition clazz = groovyFile.getTypeDefinitions()[0];
        final Project project = clazz.getProject();
        final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        final PsiClass taskClass = facade.findClass("groovy.util.GroovyTestCase", GlobalSearchScope.allScope(project));
        final String name = clazz.getName();
        if (taskClass != null &&
            clazz instanceof GrClassDefinition &&
            clazz.isInheritor(taskClass, true) &&
            GrailsTestRunConfigurationProducer.isUnderTestSources(clazz) &&
            name!=null &&
            name.endsWith(TESTS_SUFFIX)) {
          return clazz;
        }
      }
    }

    return null;
  }

  private RunnerAndConfigurationSettings createConfigurationByClass(final PsiClass aClass, @Nullable PsiMethod method) {
    final Project project = aClass.getProject();
    RunnerAndConfigurationSettings settings = RunManagerEx.getInstanceEx(project).createConfiguration("", myConfigurationFactory);
    final GrailsRunConfiguration configuration = (GrailsRunConfiguration)settings.getConfiguration();
    configuration.cmdLine = "test-app";
    final VirtualFile srcRoot =
      ProjectRootManager.getInstance(aClass.getProject()).getFileIndex().getSourceRootForFile(aClass.getContainingFile().getVirtualFile());
    if (srcRoot != null && "unit".equals(srcRoot.getName())) {
      configuration.cmdLine += " -unit";
    } else if (srcRoot != null && "integration".equals(srcRoot.getName())) {
      configuration.cmdLine += " -integration";
    }

    final Module module = JavaExecutionUtil.findModule(aClass);
    configuration.setModule(module);

    final String qname = aClass.getQualifiedName();
    assert qname != null;
    String testName = StringUtil.trimEnd(qname, TESTS_SUFFIX);
    String confName = StringUtil.trimEnd(aClass.getName(), TESTS_SUFFIX);
    if (method != null) {
      if (GrailsConfigUtils.isAtLeastGrails1_1(module)) {
        if (GrailsTestRunConfigurationProducer.isGrailsTestMethod(method)) {
          final String appendix = "." + method.getName();
          testName += appendix;
          confName += appendix;
        }
      }
    }

    configuration.cmdLine += " " + testName;
    configuration.setName(confName);
    return settings;
  }


  public boolean isConfigurationByLocation(final RunConfiguration configuration, final Location location) {
    return false;
  }

}
