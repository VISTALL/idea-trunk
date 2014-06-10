package com.intellij.seam.jsf;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.seam.utils.beans.ContextVariable;
import com.intellij.seam.utils.beans.DomFactoryContextVariable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class JsfContextBeansUtils {
  private JsfContextBeansUtils() {
  }

  public static List<JspImplicitVariable> getJspImplicitVariables(final String name, final PsiFile file) {
    final List<JspImplicitVariable> resultVars = new ArrayList<JspImplicitVariable>();
    if (file != null) {
      Module module = ModuleUtil.findModuleForPsiElement(file);
      if (module != null) {

        for (ContextVariable contextVariable : SeamCommonUtils.getSeamContextVariablesWithDependencies(module)) {
          if (name == null || contextVariable.getName().startsWith(name)) {
            addSeamImplicitVariable(contextVariable.getModelElement().getIdentifyingPsiElement(), contextVariable.getName(), resultVars,
                                    getType(contextVariable), file);
          }
        }
      }                        
    }
    return resultVars;
  }

  private static PsiType getType(ContextVariable variable) {
    if (variable instanceof DomFactoryContextVariable)        {
      DomFactoryContextVariable factoryContextVariable = (DomFactoryContextVariable)variable;

      PsiType expressionType = factoryContextVariable.getELExpressionType();
      return expressionType == null ? variable.getType() : expressionType;
    }

    return  variable.getType();
  }

  public static List<JspImplicitVariable> getJspImplicitVariables(final PsiFile file) {
    final List<JspImplicitVariable> resultVars = new ArrayList<JspImplicitVariable>();
    if (file != null) {
      Module module = ModuleUtil.findModuleForPsiElement(file);
      if (module != null) {

        for (ContextVariable contextVariable : SeamCommonUtils.getSeamContextVariablesWithDependencies(module)) {
          addSeamImplicitVariable(contextVariable.getModelElement().getIdentifyingPsiElement(), contextVariable.getName(), resultVars,
                                  contextVariable.getType(), file);
        }
      }
    }
    return resultVars;
  }

  private static void addSeamImplicitVariable(final PsiElement psiElement,
                                              @Nullable final String name,
                                              final List<JspImplicitVariable> result,
                                              @Nullable final PsiType type,
                                              final PsiFile file) {
    if (name == null || name.length() == 0 || type == null) return;

    result.add(new JspImplicitVariableImpl(file, name, type, psiElement, JspImplicitVariableImpl.NESTED_RANGE));
    if (name.contains(".")) {
      result.add(new JspImplicitVariableImpl(file, formatName(name), type, psiElement, JspImplicitVariableImpl.NESTED_RANGE));
    }
  }

  private static String formatName(String name) {
    return name.replace('.', '$');
  }
}
