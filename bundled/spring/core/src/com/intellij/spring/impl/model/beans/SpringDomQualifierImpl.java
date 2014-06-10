package com.intellij.spring.impl.model.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.QualifierAttribute;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringDomQualifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringDomQualifierImpl implements SpringDomQualifier {

  public PsiClass getQualifierType() {
    return getType().getValue();
  }

  public String getQualifierValue() {
    return getValue().getValue();
  }

  @NotNull
  public List<? extends QualifierAttribute> getQualifierAttributes() {
    return getAttributes();
  }

  @Nullable
  public CommonSpringBean getQualifiedBean() {
    return getParentOfType(SpringBean.class, false);
  }

  @NotNull
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }
}
