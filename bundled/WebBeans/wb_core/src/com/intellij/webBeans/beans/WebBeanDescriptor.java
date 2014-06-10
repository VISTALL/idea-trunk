package com.intellij.webBeans.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public interface WebBeanDescriptor {

  @Nullable
  PsiType getType();

   Set<PsiClass> getBindingTypes();

   @Nullable
   PsiClass getScopeType();

   @Nullable
   PsiClass getDeploymentType();

  @NotNull
  Set<PsiClass> getStereotypes();
}
