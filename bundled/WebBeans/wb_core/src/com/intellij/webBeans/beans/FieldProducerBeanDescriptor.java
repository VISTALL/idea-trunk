package com.intellij.webBeans.beans;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class FieldProducerBeanDescriptor extends ProducerBeanDescriptor<PsiField> {
  private final PsiField myPsiField;

  public FieldProducerBeanDescriptor(@NotNull final PsiField field) {
    myPsiField = field;
  }

  @NotNull
  public PsiField getAnnotatedItem() {
    return myPsiField ;
  }

  public PsiType getType() {
    return getAnnotatedItem().getType(); 
  }
}
