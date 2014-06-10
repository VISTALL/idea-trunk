package com.intellij.seam.model.jam.jsf;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.seam.constants.SeamAnnotationConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SeamJamConverter implements JamElement {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.JSF_CONVERTER_ANNOTATION);
  public static final JamClassMeta<SeamJamConverter> META = new JamClassMeta<SeamJamConverter>(SeamJamConverter.class).addAnnotation(ANNOTATION_META);

  @NotNull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

  @Nullable
  public String getId() {
    return AnnotationModelUtil.getObjectValue(getIdentifyingAnnotation().findDeclaredAttributeValue("id"), String.class);
  }

  public PsiAnnotation getIdentifyingAnnotation() {
    return ANNOTATION_META.getAnnotation(getPsiElement());
  }
}