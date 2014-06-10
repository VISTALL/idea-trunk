package com.intellij.seam.model.jam.context;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiElement;
import com.intellij.seam.constants.SeamAnnotationConstants;
import org.jetbrains.annotations.NotNull;

public abstract class SeamJamEnd implements JamElement {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.END_ANNOTATION);

  @NotNull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  public PsiElement getIdentifyingAnnotation() {
    return ANNOTATION_META.getAnnotation(getPsiElement());
  }
}