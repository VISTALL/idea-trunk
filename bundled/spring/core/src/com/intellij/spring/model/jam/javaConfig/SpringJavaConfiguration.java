package com.intellij.spring.model.jam.javaConfig;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class SpringJavaConfiguration implements JamElement {

  private final JamAnnotationMeta myMeta;

  protected SpringJavaConfiguration(@NotNull String annotation) {
    myMeta = new JamAnnotationMeta(annotation);
  }

  public PsiAnnotation getAnnotation() {
    return myMeta.getAnnotation(getPsiElement());
  }

  @NotNull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

  public abstract List<? extends SpringJavaBean> getBeans();

  public PsiClass getPsiClass() {
    return getPsiElement();
  }
}
