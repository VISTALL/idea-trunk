package com.intellij.webBeans.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.webBeans.beans.SimpleWebBeanDescriptor;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeanInjectionInspection extends BaseWebBeanInspection {
  @Override
  protected void checkSimpleWebBean(SimpleWebBeanDescriptor descriptor, ProblemsHolder holder) {
    checkInjectableFields(descriptor, holder);

  }

  private static void checkInjectableFields(SimpleWebBeanDescriptor descriptor, ProblemsHolder holder) {
    List<PsiField> injectableFields = descriptor.getInjectableFields();

    for (PsiField field : injectableFields) {
      PsiModifierList modifierList = field.getModifierList();
      if (modifierList == null) continue;
      if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
        holder.registerProblem(field.getNameIdentifier(),
                               WebBeansInspectionBundle.message("WebBeanInjectionInspection.field.cannot.be.static"));
      }
      if (modifierList.hasModifierProperty(PsiModifier.FINAL)) {
        holder.registerProblem(field.getNameIdentifier(),
                               WebBeansInspectionBundle.message("WebBeanInjectionInspection.field.cannot.be.final"));
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return WebBeansInspectionBundle.message("inspection.name.injection.errors");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "WebBeanInjectionInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}