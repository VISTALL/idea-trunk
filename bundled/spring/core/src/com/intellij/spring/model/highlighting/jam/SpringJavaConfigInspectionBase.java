package com.intellij.spring.model.highlighting.jam;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.jam.JamService;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.model.jam.javaConfig.JavaSpringConfiguration;
import com.intellij.spring.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.semantic.SemKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SpringJavaConfigInspectionBase extends BaseJavaLocalInspectionTool {
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.model.highlighting.jam.SpringJamInspectionBase");

  @NotNull
  public String getGroupDisplayName() {
    return SpringBundle.message("model.inspection.group.name");
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @Nullable
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (JamCommonUtil.isPlainJavaFile(file)) {
      final Module module = ModuleUtil.findModuleForPsiElement(file);
      if (module != null && SpringFacet.getInstance(module) != null) {

        final ProblemsHolder holder = new ProblemsHolder(manager, file);

        checkJavaFile((PsiJavaFile)file, holder, isOnTheFly, module);

        final List<ProblemDescriptor> problemDescriptors = holder.getResults();
        if (problemDescriptors != null) return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
      }
    }
    return null;
  }

  protected void checkJavaFile(@NotNull final PsiJavaFile javaFile,
                               @NotNull final ProblemsHolder holder,
                               final boolean isOnTheFly,
                               @NotNull Module module) {
    for (PsiClass psiClass : javaFile.getClasses()) {
      checkClassInternal(psiClass, holder, module);
    }
  }

  private void checkClassInternal(final PsiClass aClass, final ProblemsHolder holder, @NotNull Module module) {
    checkClass(aClass, holder, module);
    for (PsiClass psiClass : aClass.getInnerClasses()) {
      checkClass(psiClass, holder, module);
    }
  }

  protected void checkClass(final PsiClass aClass, final ProblemsHolder holder, @NotNull Module module) {
    SpringJavaConfiguration configuration = getJavaConfiguration(aClass, module);

    if (configuration != null) {
      checkJavaConfiguration(configuration, module, holder);
    }
  }

  @Nullable
  protected SpringJavaConfiguration getJavaConfiguration(PsiClass aClass, Module module) {
    JavaSpringConfiguration javaSpringConfiguration = getJavaConfiguration(aClass, module, JavaSpringConfiguration.META.getJamKey());

    return javaSpringConfiguration == null ? getJavaConfiguration(aClass, module, JavaConfigConfiguration.META.getJamKey()) : javaSpringConfiguration;
  }

  private <T extends SpringJavaConfiguration> T getJavaConfiguration(PsiClass aClass, Module module, SemKey<T> jamKey) {
    return JamService.getJamService(module.getProject()).getJamElement(jamKey, aClass);
  }

  protected abstract void checkJavaConfiguration(final SpringJavaConfiguration javaConfiguration,
                                                 final Module module,
                                                 final ProblemsHolder holder);
}

