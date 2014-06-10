package com.intellij.webBeans.beans;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.psi.JavaPsiFacade;

/**
 * User: Sergey.Vasiliev
 */
public class SimpleWebBeanDescriptor extends WebBeanPsiClassDescriptor {
  public SimpleWebBeanDescriptor(@NotNull PsiClass psiClass) {
    super(psiClass);
  }

  public PsiType getType() {
    return JavaPsiFacade.getElementFactory(getModule().getProject()).createType(getAnnotatedItem());
  }
}
