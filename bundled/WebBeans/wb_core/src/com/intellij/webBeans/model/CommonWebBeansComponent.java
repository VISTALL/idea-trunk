package com.intellij.webBeans.model;

import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.psi.PsiType;
import com.intellij.util.xml.NameValue;
import com.intellij.util.xml.PrimaryKey;
import org.jetbrains.annotations.Nullable;

public interface CommonWebBeansComponent extends CommonModelElement {

  @Nullable
  @NameValue
  String getName();

  @Nullable
  @PrimaryKey
  PsiType getType();
}
