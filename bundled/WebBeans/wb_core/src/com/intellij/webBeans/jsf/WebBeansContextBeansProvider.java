package com.intellij.webBeans.jsf;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.impl.source.jsp.el.impl.CustomJsfContextBeansProvider;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.webBeans.jam.NamedWebBean;
import com.intellij.webBeans.jam.WebBeansJamModel;

import java.util.List;

public class WebBeansContextBeansProvider implements CustomJsfContextBeansProvider {

  public void addVars(final List<JspImplicitVariable> resultVars, final PsiFile file) {
    if (file == null) {
      return;
    }
    Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null) {
      return;
    }
    for (NamedWebBean<?> webBean : WebBeansJamModel.getModel(module).getNamedWebBeans()) {
      final PsiType type = webBean.getType();
      if (type != null) {
        PsiNamedElement psiElement = webBean.getIdentifyingPsiElement();
        final String name = psiElement.getName();
        if (StringUtil.isNotEmpty(name)) {
          resultVars.add(new JspImplicitVariableImpl(file, name, type, psiElement, JspImplicitVariableImpl.NESTED_RANGE));
        }
      }
    }
  }
}
