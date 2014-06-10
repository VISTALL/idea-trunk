package com.intellij.seam.impl.model.xml.components;

import com.intellij.seam.model.xml.components.SeamProperty;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.beanProperties.BeanProperty;

/**
 * User: Sergey.Vasiliev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SeamPropertyImpl implements SeamProperty {
  public String getPropertyName() {
    return getName().getStringValue();
  }

  public PsiType getPropertyType() {
    final BeanProperty beanProperty = getName().getValue();
    if (beanProperty == null) return null;

    return beanProperty.getPropertyType();
  }
}
