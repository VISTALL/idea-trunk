package com.intellij.spring.impl.model.util;

import com.intellij.psi.PsiType;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.model.beans.TypedCollectionImpl;
import com.intellij.spring.model.xml.util.UtilSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class UtilSetImpl extends DomSpringBeanImpl implements UtilSet {
  @NotNull
  public String getClassName() {
    return "org.springframework.beans.factory.config.SetFactoryBean";
  }

   @NotNull
   public List<? extends PsiType> getRequiredTypes() {
     return TypedCollectionImpl.getRequiredTypes(this);
  }
}
