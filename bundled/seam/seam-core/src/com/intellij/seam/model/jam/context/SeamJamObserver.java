package com.intellij.seam.model.jam.context;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.javaee.model.annotations.AnnotationGenericValue;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.psi.PsiMethod;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.seam.model.jam.BooleanJamConverter;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Serega.Vasiliev
 */
public abstract class SeamJamObserver implements JamElement {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.OBSERVER_ANNOTATION);

  public static final JamStringAttributeMeta.Single<Boolean> CREATE_META =
    JamAttributeMeta.singleString("create", new BooleanJamConverter());

  @NotNull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  public String[] getEventTypes() {
    final List<AnnotationGenericValue<String>> list = AnnotationModelUtil.getStringArrayValue(ANNOTATION_META.getAnnotation(getPsiElement()), "value");
    return ContainerUtil.map2Array(list, String.class, new Function<AnnotationGenericValue<String>, String>() {
      public String fun(final AnnotationGenericValue<String> genericValue) {
        return genericValue.getValue();
      }
    });
  }

  boolean isCreate() {
    return ANNOTATION_META.getAttribute(getPsiElement(), CREATE_META).getValue();
  }
}