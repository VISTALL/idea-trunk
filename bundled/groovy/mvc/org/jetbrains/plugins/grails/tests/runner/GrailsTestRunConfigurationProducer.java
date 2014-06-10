package org.jetbrains.plugins.grails.tests.runner;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.Location;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.config.GrailsConfigUtils;
import org.jetbrains.plugins.grails.runner.GrailsRunConfiguration;
import org.jetbrains.plugins.grails.runner.GrailsRunConfigurationType;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;

/**
 * @author ilyas
 */
public class GrailsTestRunConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {
  private PsiElement mySourceElement;

  public GrailsTestRunConfigurationProducer() {
    super(GrailsRunConfigurationType.getInstance());
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  protected RunnerAndConfigurationSettingsImpl createConfigurationByElement(final Location location, final ConfigurationContext context) {

    final Module module = context.getModule();
    if (module == null || !GrailsUtils.hasGrailsSupport(module)) return null;

    final PsiElement element = location.getPsiElement();
    if (element instanceof PsiDirectory) {
      if (isTestDirectory((PsiDirectory)element)) {
        mySourceElement = element;
        return createConfiguration(location, module);
      }
    }

    final PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    if (isGrailsTestMethod(method)) {
      if (!GrailsConfigUtils.isAtLeastGrails1_1(module)) {
        return null; //give chance to junit
      }

      final PsiClass psiClass = method.getContainingClass();
      if (isGrailsTestClass(module, psiClass)) {
        mySourceElement = method;
        return createConfiguration(location, module);
      }
    }

    final PsiFile file = element.getContainingFile();
    if (file instanceof GroovyFile) {
      GroovyFile groovyFile = (GroovyFile)file;
      if (!(groovyFile.isScript()) && groovyFile.getTypeDefinitions().length == 1) {
        final GrTypeDefinition clazz = groovyFile.getTypeDefinitions()[0];
        if (clazz instanceof GrClassDefinition && isGrailsTestClass(module, clazz)) {
          mySourceElement = clazz;
          return createConfiguration(location, module);
        }
      }
    }

    return null;
  }

  public static boolean isGrailsTestMethod(PsiMethod method) {
    return method != null && Character.isLowerCase(method.getName().charAt(0));
  }

  @Nullable
  private static RunnerAndConfigurationSettingsImpl createConfiguration(Location location, Module module) {
    final RunnerAndConfigurationSettingsImpl settings =
      (RunnerAndConfigurationSettingsImpl)GrailsRunConfigurationType.getInstance().createConfigurationByLocation(location);
    if (settings != null) {
      //todo don't overwrite default module setting in run configuration (when it appears)
      final GrailsRunConfiguration configuration = (GrailsRunConfiguration)settings.getConfiguration();
      
      configuration.setModule(module);
      final CompileStepBeforeRun.MakeBeforeRunTask runTask =
        RunManagerEx.getInstanceEx(module.getProject()).getBeforeRunTask(configuration, CompileStepBeforeRun.ID);
      if (runTask != null) {
        runTask.setEnabled(false);
      }
    }
    return settings;
  }

  private static boolean isGrailsTestClass(Module module, PsiClass psiClass) {
    return InheritanceUtil.isInheritor(psiClass, "junit.framework.TestCase") && isUnderTestSources(psiClass) && GrailsUtils.isUnderGrailstestFolders(module, psiClass);
  }

  public static boolean isUnderTestSources(PsiClass c) {
    ProjectRootManager rm = ProjectRootManager.getInstance(c.getProject());
    VirtualFile f = c.getContainingFile().getVirtualFile();
    if (f == null) return false;
    return rm.getFileIndex().isInTestSourceContent(f);
  }


  public int compareTo(final Object o) {
    return PREFERED;
  }

  public static boolean isTestDirectory(final PsiDirectory dir) {
    final Project project = dir.getProject();
    final ModuleManager moduleManager = ModuleManager.getInstance(project);
    for (Module module : moduleManager.getModules()) {
      final ModuleRootManager manager = ModuleRootManager.getInstance(module);
      final ContentEntry[] entries = manager.getContentEntries();
      for (ContentEntry entry : entries) {
        for (SourceFolder folder : entry.getSourceFolders()) {
          final VirtualFile root = folder.getFile();
          if (folder.isTestSource() && root != null && VfsUtil.isAncestor(root, dir.getVirtualFile(), false)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
