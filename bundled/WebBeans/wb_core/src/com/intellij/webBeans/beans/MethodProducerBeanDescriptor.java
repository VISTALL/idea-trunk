package com.intellij.webBeans.beans;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class MethodProducerBeanDescriptor extends ProducerBeanDescriptor<PsiMethod> {
  private final PsiMethod myPsiMethod;

  public MethodProducerBeanDescriptor(@NotNull final PsiMethod psiMethod) {
    myPsiMethod = psiMethod;
  }

  @NotNull
  public PsiMethod getAnnotatedItem() {
    return myPsiMethod ;
  }

  @Nullable
  public PsiType getType() {
    return getAnnotatedItem().getReturnType();  
  }
}
