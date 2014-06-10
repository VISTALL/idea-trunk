package com.intellij.seam.jsf;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.jsp.el.impl.CustomJsfContextBeansProvider;
import com.intellij.psi.jsp.JspImplicitVariable;

import java.util.List;

public class SeamJsfContexBeansProvider implements CustomJsfContextBeansProvider {

  public void addVars(final List<JspImplicitVariable> resultVars, final PsiFile file) {
    resultVars.addAll(JsfContextBeansUtils.getJspImplicitVariables(file));
  }
}