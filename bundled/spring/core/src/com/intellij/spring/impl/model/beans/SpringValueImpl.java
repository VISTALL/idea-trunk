package com.intellij.spring.impl.model.beans;

import com.intellij.spring.model.xml.beans.SpringValue;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Collections;

/**
 * @author Dmitry Avdeev
 */
public abstract class SpringValueImpl implements SpringValue {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    final PsiType type = getType().getValue();
    return type == null ? Collections.<PsiType>emptyList() : Collections.singletonList(type);
  }
}
