package com.intellij.seam.model.jam.bijection;

import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.jam.reflect.JamAnnotationMeta;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Serega.Vasiliev
 */
public abstract class SeamJamOutjection<T extends PsiMember & PsiNamedElement> extends SeamJamBijection<T> {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.OUT_ANNOTATION);

  public SeamJamOutjection(T member) {
    super(member);
  }

  @NotNull
  public JamAnnotationMeta getJamAnnotationMeta() {
    return ANNOTATION_META;
  }

  public static class Method extends SeamJamOutjection<PsiMethod> {
    public Method(PsiMethod member) {
      super(member);
    }

    @Override
    public PsiType getType() {
      return getPsiElement().getReturnType();
    }

    @Override
    public String getNameAlias(@Nullable String delegatePsiTargetName) {
      return PropertyUtil.getPropertyName(delegatePsiTargetName);
    }
  }

  public static class Field extends SeamJamOutjection<PsiField> {
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
}
