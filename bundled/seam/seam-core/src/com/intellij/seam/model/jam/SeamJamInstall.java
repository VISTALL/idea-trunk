package com.intellij.seam.model.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.seam.model.SeamInstallPrecedence;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public abstract class SeamJamInstall implements JamElement {
  public static final JamClassMeta<SeamJamInstall> META = new JamClassMeta<SeamJamInstall>(SeamJamInstall.class);

  private final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.INSTALL_ANNOTATION);

  @NotNull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

  public int getPrecedence() {
    PsiAnnotation annotation = ANNOTATION_META.getAnnotation(getPsiElement());
    if (annotation == null) return SeamInstallPrecedence.APPLICATION;
    final Integer value = AnnotationModelUtil.getObjectValue(annotation.findDeclaredAttributeValue("precedence"), Integer.class);

    return value == null ? SeamInstallPrecedence.APPLICATION : value;
  }

  public boolean isInstall() {
    PsiAnnotation annotation = ANNOTATION_META.getAnnotation(getPsiElement());
    if (annotation == null) return true;

    Boolean value = AnnotationModelUtil.getObjectValue(annotation.findDeclaredAttributeValue("value"), Boolean.class);

    return value == null || value;
  }
}