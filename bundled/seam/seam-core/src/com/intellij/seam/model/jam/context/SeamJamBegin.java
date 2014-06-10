package com.intellij.seam.model.jam.context;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiElement;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.seam.model.jam.BooleanJamConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SeamJamBegin implements JamElement {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.BEGIN_ANNOTATION);

  public static final JamStringAttributeMeta.Single<String> CONVERSATION_META = JamAttributeMeta.singleString("conversation");
  public static final JamStringAttributeMeta.Single<Boolean> JOIN_META = JamAttributeMeta.singleString("join", new BooleanJamConverter());
  public static final JamStringAttributeMeta.Single<Boolean> NESTED_META = JamAttributeMeta.singleString("nested", new BooleanJamConverter());

  @NotNull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  @Nullable
  public String conversation() {
    return ANNOTATION_META.getAttribute(getPsiElement(), CONVERSATION_META).getStringValue();
  }

  public boolean isJoin() {
    return ANNOTATION_META.getAttribute(getPsiElement(), JOIN_META).getValue();
  }

  public boolean isNested(){
    return ANNOTATION_META.getAttribute(getPsiElement(), NESTED_META).getValue();
  }

  public PsiElement getIdentifyingAnnotation() {
    return ANNOTATION_META.getAnnotation(getPsiElement());
  }
}