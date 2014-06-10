package com.intellij.seam.model.jam.dataModel;

import com.intellij.jam.JamElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.psi.*;
import com.intellij.seam.constants.SeamAnnotationConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SeamJamDataModelSelectionIndex<T extends PsiMember & PsiNamedElement> implements JamElement {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.JSF_DATA_MODEL_SELECTION_INDEX_ANNOTATION);

  public final T myMember;

  public SeamJamDataModelSelectionIndex(T member) {
    myMember = member;
  }

  public class Method extends SeamJamDataModelSelectionIndex<PsiMethod> {
    public Method(PsiMethod member) {
      super(member);
    }
  }

  public class Field extends SeamJamDataModelSelectionIndex<PsiField> {
    public Field(PsiField member) {
      super(member);
    }
  }

  @NotNull
  public T getPsiElement() {
    return myMember;
  }

  @Nullable
  public PsiAnnotation getIdentifyingAnnotation() {
    return ANNOTATION_META.getAnnotation(getPsiElement());
  }
}