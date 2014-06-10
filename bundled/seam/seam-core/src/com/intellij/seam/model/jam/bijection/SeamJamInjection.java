package com.intellij.seam.model.jam.bijection;

import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.jam.reflect.JamAnnotationMeta;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Serega.Vasiliev
 */
public abstract class SeamJamInjection<T extends PsiMember & PsiNamedElement> extends SeamJamBijection<T> {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.IN_ANNOTATION);

  public SeamJamInjection(T member) {
    super(member);
  }

  @NotNull
  protected JamAnnotationMeta getJamAnnotationMeta() {
    return ANNOTATION_META;
  }

  public static class Method extends SeamJamInjection<PsiMethod> {
    public Method(PsiMethod member) {
      super(member);
    }

    @Override
    public PsiType getType() {
      return PropertyUtil.getPropertyType(getPsiElement());
    }

    @Override
    public String getNameAlias(@Nullable String delegatePsiTargetName) {
      return PropertyUtil.getPropertyName(delegatePsiTargetName);
    }
  }

  public static class Field extends SeamJamInjection<PsiField> {
    public Field(PsiField member) {
      super(member);
    }

    @Override
    public PsiType getType() {
      return getPsiElement().getType();
    }

    @Override
    public String getNameAlias(@Nullable String delegatePsiTargetName) {
      return delegatePsiTargetName;
    }
  }

  public boolean isCreate() {
    PsiAnnotation annotation = getIdentifyingAnnotation();
    Boolean value = null;
    if (annotation != null) {
      value = AnnotationModelUtil.getObjectValue(annotation.findDeclaredAttributeValue("create"), Boolean.class);
    }

    return value != null && value;
  }

}
