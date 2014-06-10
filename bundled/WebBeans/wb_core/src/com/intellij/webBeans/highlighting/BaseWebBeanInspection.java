package com.intellij.webBeans.highlighting;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.compiler.util.InspectionValidatorWrapper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.webBeans.beans.ProducerBeanDescriptor;
import com.intellij.webBeans.beans.SimpleWebBeanDescriptor;
import com.intellij.webBeans.beans.WebBeanDescriptor;
import com.intellij.webBeans.manager.WebBeansManager;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class BaseWebBeanInspection extends BaseJavaLocalInspectionTool {

  @NotNull
  public String getGroupDisplayName() {
    return WebBeansInspectionBundle.message("model.inspection.group.name");
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @Nullable
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (!JamCommonUtil.isPlainJavaFile(file)) return null;
    if (!isFileAccepted(file.getContainingFile())) return null;
    Module module = ModuleUtil.findModuleForPsiElement(file);

    if (module == null || !WebBeansCommonUtils.isWebBeansFacetDefined(module)) return null;

    final ProblemsHolder holder = new ProblemsHolder(manager, file);
    checkJavaFile((PsiJavaFile)file, holder, isOnTheFly, module);
    final List<ProblemDescriptor> problemDescriptors = holder.getResults();
    if (problemDescriptors != null) return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);

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
    if (WebBeansCommonUtils.isSimpleWebBean(aClass)) {
      Set<WebBeanDescriptor> descriptors = WebBeansManager.getService(module).resolveWebBeanByType(aClass);
      for (WebBeanDescriptor descriptor : descriptors) {
        checkWebBeanDescriptor(descriptor, holder);

        if (descriptor instanceof SimpleWebBeanDescriptor) {
          for (ProducerBeanDescriptor producerBeanDescriptor : ((SimpleWebBeanDescriptor)descriptor).getProducerWebBeansDescriptors()) {
            checkWebBeanDescriptor(producerBeanDescriptor, holder);
          }
        }
      }
    }
  }

  protected void checkWebBeanDescriptor(WebBeanDescriptor descriptor, ProblemsHolder holder) {
    if (descriptor instanceof SimpleWebBeanDescriptor) {
      checkSimpleWebBean((SimpleWebBeanDescriptor)descriptor, holder);
    }
    else if (descriptor instanceof ProducerBeanDescriptor) {
      checkProducerBeanDescriptor((ProducerBeanDescriptor)descriptor, holder);
    }
  }

  protected void checkProducerBeanDescriptor(ProducerBeanDescriptor producerBeanDescriptor, ProblemsHolder holder) {
  }

  protected void checkSimpleWebBean(SimpleWebBeanDescriptor descriptor, ProblemsHolder holder) {
  }

  protected static boolean isFileAccepted(final PsiFile file) {
    if (!InspectionValidatorWrapper.isCompilationThread()) return true;
    final Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null) return false;
    return true;
  }
}


