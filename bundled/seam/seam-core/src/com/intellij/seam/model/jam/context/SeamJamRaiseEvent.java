package com.intellij.seam.model.jam.context;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.psi.PsiMethod;
import com.intellij.seam.constants.SeamAnnotationConstants;
import org.jetbrains.annotations.NotNull;

/**
 * @author Serega.Vasiliev
 */
public abstract class SeamJamRaiseEvent implements JamElement {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.RAISE_EVENT_ANNOTATION);

  public static final JamStringAttributeMeta.Single<String> NAME_META = JamAttributeMeta.singleString("value");

  @NotNull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  public String getEventType() {
      return ANNOTATION_META.getAttribute(getPsiElement(), NAME_META).getStringValue();
  }
}