/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.meta.PsiMetaData;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public class AopMetaData implements PsiMetaData {
  private PsiAnnotation myAnnotation;

  public PsiElement getDeclaration() {
    return myAnnotation;
  }

  @NonNls
  public String getName(PsiElement context) {
    return getName();
  }

  @NonNls
  public final String getName() {
    return ((PsiMethod)myAnnotation.getParent().getParent()).getName();
  }

  public void init(PsiElement element) {
    myAnnotation = (PsiAnnotation) element;
  }

  public Object[] getDependences() {
    return new Object[]{myAnnotation};
  }
}
