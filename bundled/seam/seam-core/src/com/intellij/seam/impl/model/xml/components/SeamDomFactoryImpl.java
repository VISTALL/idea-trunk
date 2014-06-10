package com.intellij.seam.impl.model.xml.components;

import com.intellij.psi.PsiType;
import com.intellij.psi.PsiElement;
import com.intellij.seam.model.CommonSeamFactoryComponent;
import com.intellij.seam.model.SeamComponentScope;
import com.intellij.seam.model.xml.components.SeamDomFactory;
import com.intellij.seam.utils.SeamCommonUtils;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SeamDomFactoryImpl implements CommonSeamFactoryComponent, SeamDomFactory {
  @Nullable
  public String getFactoryName() {
    return getName().getValue();
  }

  @Nullable
  public PsiType getFactoryType() {
    return SeamCommonUtils.getFactoryType(this, getModule());
  }

  @Nullable
  public SeamComponentScope getFactoryScope() {
    return getScope().getValue();
  }

  public PsiElement getIdentifyingPsiElement() {
    return getXmlElement();
  }
}
