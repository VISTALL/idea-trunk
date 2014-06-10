/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.SpringQualifier;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.util.xml.model.DomModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public interface SpringModel extends DomModel<Beans> {

  SpringModel[] EMPTY_ARRAY = new SpringModel[0];
  
  @NotNull
  String getId();

  @NotNull
  SpringModel[] getDependencies();

  SpringFileSet getFileSet();

  @Nullable
  SpringBeanPointer findBean(@NonNls @NotNull String beanName);

  @Nullable
  SpringBeanPointer findParentBean(@NonNls @NotNull String beanName);

  /**
   * Returns all beans configured in the model and its dependencies.
   * @return all beans configured in the model and its dependencies.
   */
  @NotNull
  Collection<SpringBaseBeanPointer> getAllDomBeans();

  @NotNull
  Collection<SpringBaseBeanPointer> getAllDomBeans(boolean withDepenedencies);

  @NotNull
  Set<String> getAllBeanNames(@NotNull final String beanName);

  boolean isNameDuplicated(@NotNull String beanName);

  @NotNull
  Collection<? extends SpringBaseBeanPointer> getAllCommonBeans(boolean withDepenedencies);

  @NotNull
  Collection<? extends SpringBaseBeanPointer> getAllCommonBeans();

  @NotNull
  Collection<? extends SpringBaseBeanPointer> getAllParentBeans();

  @NotNull
  List<SpringBaseBeanPointer> findBeansByPsiClass(@NotNull PsiClass psiClass);

  @NotNull 
  List<SpringBaseBeanPointer> findBeansByPsiClassWithInheritance(@NotNull PsiClass psiClass);

  @NotNull
  List<SpringBaseBeanPointer> findBeansByEffectivePsiClassWithInheritance(@NotNull PsiClass psiClass);

  @NotNull
  List<SpringBaseBeanPointer> getChildren(@NotNull SpringBeanPointer parent);

  @NotNull
  List<SpringBaseBeanPointer> getDescendants(@NotNull CommonSpringBean context);

  @Nullable
  Module getModule();

  Collection<SpringBaseBeanPointer> getOwnBeans();

  List<SpringBaseBeanPointer> findQualifiedBeans(final @NotNull SpringQualifier qualifier);

  Collection<XmlTag> getCustomBeanCandidates(String id);
}
