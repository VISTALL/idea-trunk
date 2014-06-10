package com.intellij.spring.model.xml;

import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public interface SpringQualifier extends CommonModelElement {

  @NotNull
  PsiElement getIdentifyingPsiElement();

  @Nullable
  PsiClass getQualifierType();

  @Nullable
  String getQualifierValue();

  @NotNull
  List<? extends QualifierAttribute> getQualifierAttributes();

  @Nullable
  CommonSpringBean getQualifiedBean();
}
