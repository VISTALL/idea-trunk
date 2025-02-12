package com.intellij.j2meplugin.codeInspection;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.J2MEModuleProperties;
import com.intellij.j2meplugin.module.MobileModuleUtil;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MissedExecutableInspection extends BaseJavaLocalInspectionTool {
  private static final Logger LOG = Logger.getInstance("#" + MissedExecutableInspection.class.getName());

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return J2MEBundle.message("j2me.plugin.inspection.group");
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return J2MEBundle.message("executable.class.misconfiguration.display.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "MissedExecutable";
  }

  public boolean isEnabledByDefault() {
    return true;
  }


  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override public void visitReferenceExpression(final PsiReferenceExpression expression) {
      }

      @Override public void visitClass(final PsiClass aClass) {
        super.visitClass(aClass);
        final Module module = ModuleUtil.findModuleForPsiElement(aClass);
        if (module != null && MobileModuleUtil.isExecutable(aClass, module)) {
          final String fqName = aClass.getQualifiedName();
          final J2MEModuleProperties moduleProperties = J2MEModuleProperties.getInstance(module);
          if (moduleProperties != null) {
            final MobileModuleSettings moduleSettings = MobileModuleSettings.getInstance(module);
            LOG.assertTrue(moduleSettings != null);
            if (!moduleSettings.containsMidlet(fqName)) {
              final MobileApplicationType applicationType = moduleProperties.getMobileApplicationType();
              holder.registerProblem(aClass.getNameIdentifier(),
                                     J2MEBundle.message("midlet.undefined.problem.description", applicationType.getPresentableClassName()),
                                     new AddExecutable2Configuration(aClass));
            }
          }
        }
      }
    };
  }

  private static class AddExecutable2Configuration implements LocalQuickFix {
    private final PsiClass myClass;

    private final Module myModule;

    public AddExecutable2Configuration(final PsiClass aClass) {
      myClass = aClass;
      myModule = ModuleUtil.findModuleForPsiElement(aClass);
    }

    @NotNull
    public String getName() {
      return J2MEBundle.message("append.to.suite.quickfix.text", myClass.getName(), myModule.getName());
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final MobileModuleSettings moduleSettings = MobileModuleSettings.getInstance(myModule);
      LOG.assertTrue(moduleSettings != null);
      moduleSettings.addMidlet(myClass.getQualifiedName());
    }
  }
}
