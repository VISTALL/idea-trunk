/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.advancedtools.webservices.utils;

import static com.advancedtools.webservices.rest.RestAnnotations.*;
import com.intellij.javaee.model.annotations.AnnotationGenericValue;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import com.intellij.util.containers.ArrayListSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @by Konstantin Bulenkov
 */
public class RestUtils {
  private RestUtils() {}

  public static final @NonNls String[] PREDEFINED_MIME_TYPES = {
      "*/*",
      "text/plain",
      "text/html",
      "application/xml",
      "application/json",
      "application/atom+xml"};  

  public static boolean isAnnotatedAs(@NotNull final String fqn, @NotNull final PsiElement c) {
    PsiModifierList modifiers = null;
    if (c instanceof PsiModifierListOwner) {
      modifiers = ((PsiModifierListOwner)c).getModifierList();
    }
    return modifiers != null && modifiers.findAnnotation(fqn) != null;
  }

  public static boolean isResourceClass(PsiClass resource) {
    while (resource != null) {
      if (isAnnotatedAs(PATH, resource)) {
        return true;
      } else {
        resource = resource.getSuperClass();
      }
    }
    return false;
  }

  @Nullable
  public static PsiAnnotation findAnnotation(@NotNull final String fqn, @NotNull final PsiModifierListOwner owner) {
    PsiModifierList modifiers = owner.getModifierList();
    if (modifiers == null || modifiers.getAnnotations().length == 0) return null;
    final PsiAnnotation[] annos = modifiers.getAnnotations();
    for (PsiAnnotation anno : annos) {
      if (fqn.equals(anno.getQualifiedName())) return anno;
    }
    return null;
  }

  @Nullable
  public static PsiClass getParent(final @NotNull PsiAnnotation anno) {
    PsiElement el = anno.getParent();
    while (el != null) {
      if (el instanceof PsiClass) return (PsiClass)el;
      el = el.getParent();
    }
    return null;
  }

  public static boolean isNonAbstractAndPublicClass(final PsiClass c) {
    PsiModifierList modifiers = (c == null) ? null : c.getModifierList();
    return !(modifiers == null
        || c.isAnnotationType() || c.isEnum() || c.isInterface()
        || modifiers.hasExplicitModifier(PsiModifier.ABSTRACT)
        || !modifiers.hasExplicitModifier(PsiModifier.PUBLIC));

  }

  @Nullable
  public static String getAnnotationValue(final @NotNull PsiAnnotation anno) {
    return AnnotationModelUtil.getStringValue(anno, VALUE, null).getValue();
  }
 
  static final @NonNls String VALUE = "value";

  public static String[] getAllMimes(Project project) {
    Collection<PsiAnnotation> annos = JavaAnnotationIndex.getInstance().get(PRODUCE_MIME_SHORT, project, GlobalSearchScope.projectScope(project));
    annos.addAll(JavaAnnotationIndex.getInstance().get(CONSUME_MIME_SHORT, project, GlobalSearchScope.projectScope(project)));
    annos.addAll(JavaAnnotationIndex.getInstance().get(PRODUCES_SHORT, project, GlobalSearchScope.projectScope(project)));
    annos.addAll(JavaAnnotationIndex.getInstance().get(CONSUMES_SHORT, project, GlobalSearchScope.projectScope(project)));
    Set<String> types = new ArrayListSet<String>();
    for (PsiAnnotation produce : annos) {
      final List<AnnotationGenericValue<String>> values = AnnotationModelUtil.getStringArrayValue(produce, VALUE);
      for (AnnotationGenericValue<String> value : values) {
        types.add(value.getValue());
      }
    }
    return types.toArray(new String[types.size()]);
  }

  public static String[] getAllMimes(Project project, @NotNull String[] mergeWith) {
    return merge(mergeWith, getAllMimes(project), String.class);
  }

  public static boolean isResourceMethod(@NotNull PsiMethod method) {
    return    isAnnotatedAs(GET,    method)
           || isAnnotatedAs(POST,   method)
           || isAnnotatedAs(PUT,    method)
           || isAnnotatedAs(DELETE, method)
           || isAnnotatedAs(HEAD,   method);
  }

  @NonNls @NotNull
  public static String getShortName(@NonNls @NotNull String fqn) {
    return fqn.contains(".") ? fqn.substring(fqn.lastIndexOf(".") + 1) : fqn;
  }

  @Nullable
  public static PsiClass findInheritorClassAnnotatedAs(@NotNull @NonNls String fqn, @NotNull PsiClass c) {
    final Query<PsiClass> query = ClassInheritorsSearch.search(c, GlobalSearchScope.projectScope(c.getProject()), true);
    final Collection<PsiClass> classes = query.findAll();
    for (PsiClass clazz : classes) {
      if (findAnnotation(fqn, clazz) != null) return clazz;
      PsiClass cl = findInheritorClassAnnotatedAs(fqn, clazz);
      if (cl != null) return cl;
    }
    return null;
  }

  public static <T> T[] merge(T[] a1, T[] a2, Class<T> type) {
    Set<T> set = new ArrayListSet<T>();
    set.addAll(Arrays.asList(a1));
    set.addAll(Arrays.asList(a2));
    T[] array = (T[])Array.newInstance(type, set.size());
    return set.toArray(array);
  }

  public static boolean isRootResourceClass(PsiElement element) {
    return (element instanceof PsiClass)
           && isNonAbstractAndPublicClass((PsiClass)element)
           && isAnnotatedAs(PATH, element);
  }

  public static String[] getAllPathes(Project project) {
    Collection<String> pathes = new ArrayListSet<String>();
    Collection<PsiAnnotation> annos = JavaAnnotationIndex.getInstance().get(PATH_SHORT, project, GlobalSearchScope.projectScope(project));
    PsiAnnotation[] pathAnnos = annos.toArray(new PsiAnnotation[annos.size()]);
    for (PsiAnnotation pathAnno : pathAnnos) {
      PsiElement parent = pathAnno.getParent();
      if (parent != null) parent = parent.getParent();
      if (isRootResourceClass(parent)) {
        String path = AnnotationModelUtil.getStringValue(pathAnno, VALUE, "/").getValue();
        if (path == null || parent == null) continue;
        if (!path.startsWith("/")) path="/" + path;
        pathes.add(path);
        if (!path.endsWith("/")) path += "/";        
        PsiMethod[] methods = ((PsiClass)parent).getAllMethods();
        for (PsiMethod method : methods) {
          if (isAnnotatedAs(PATH, method)) {
            String value = AnnotationModelUtil.getStringValue(method.getModifierList().findAnnotation(PATH), VALUE, "").getValue();
            if (value == null || value.trim().length() == 0) continue;

            if (value.startsWith("/")) value = value.substring(1);
            pathes.add(path + value);
          }
        }
      }
    }

    return pathes.toArray(new String[pathes.size()]);
  }

  static boolean isParent(PsiElement candidate, PsiElement child) {
    if (candidate == null || child == null) return false;
    PsiElement el;
    while ((el = child.getParent()) != null) {
      if (candidate == el) return true;
    }
    return false;
  }
}
