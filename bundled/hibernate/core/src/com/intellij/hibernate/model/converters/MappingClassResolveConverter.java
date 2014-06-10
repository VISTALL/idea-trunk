/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.xml.impl.mapping.HbmHibernateMappingImpl;
import com.intellij.hibernate.model.xml.mapping.HbmHibernateMapping;
import com.intellij.hibernate.model.xml.mapping.HbmImport;
import com.intellij.jpa.model.xml.impl.converters.ClassConverterBase;
import com.intellij.jpa.util.JpaCommonUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author Gregory.Shrago
 */
public class MappingClassResolveConverter extends ClassConverterBase {

  public PsiClass fromString(final String s, final ConvertContext context) {
    if (s == null) return null;
    if (context.getInvocationElement().getParent() instanceof HbmImport) return super.fromString(s, context);
    final HbmHibernateMappingImpl mapping = getRoot(context);
    final Set<String> list;
    final String qualifiedName = mapping == null || (list = mapping.getImportsMap().getKeys(s)) == null || list.isEmpty()? null : list.iterator().next();
    return super.fromString(qualifiedName != null? qualifiedName : s, context);
  }

  public String toString(final PsiClass t, final ConvertContext context) {
    if (t == null) return null;
    if (context.getInvocationElement().getParent() instanceof HbmImport) return super.toString(t, context);
    final HbmHibernateMappingImpl mapping = getRoot(context);
    final Set<String> list;
    final String alias = mapping == null || (list = mapping.getImportsMap().getValues(t.getQualifiedName())) == null || list.isEmpty()? null : list.iterator().next();
    return alias != null? alias : super.toString(t, context);
  }

  @Nullable
  protected String getDefaultPackageName(final ConvertContext context) {
    return getPackageName(context);
  }

  @Nullable
  public static String getPackageName(final ConvertContext context) {
    return getPackageName(getRoot(context));
  }

  @Nullable
  public static String getPackageName(HbmHibernateMapping mapping) {
    return mapping != null ? mapping.getPackage().getStringValue() : null;
  }

  @Nullable
  static HbmHibernateMappingImpl getRoot(final ConvertContext context) {
    return context.getInvocationElement().getParentOfType(HbmHibernateMappingImpl.class, false);
  }

  @NotNull
  protected GlobalSearchScope getResolveSearchScope(final ConvertContext context) {
    return JpaCommonUtil.getORMClassesSearchScope(context.getPsiManager().getProject(), context.getModule(), context.getFile());
  }

}