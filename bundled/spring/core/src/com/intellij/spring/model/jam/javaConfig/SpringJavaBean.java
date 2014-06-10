package com.intellij.spring.model.jam.javaConfig;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiNamedElement;
import com.intellij.spring.model.jam.JamPsiMethodSpringBean;

public abstract class SpringJavaBean extends JamPsiMethodSpringBean {

  public PsiNamedElement getIdentifyingPsiElement() {
    return getPsiElement();
  }

  public abstract PsiAnnotation getPsiAnnotation();

  public boolean isPublic() {
    return getPsiElement().getModifierList().hasModifierProperty(PsiModifier.PUBLIC);
  }
}
