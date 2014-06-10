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

package com.intellij.uml.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.VisibilityUtil;
import com.intellij.uml.components.UmlClassProperty;
import com.intellij.uml.presentation.VisibilityLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Konstantin Bulenkov
 */
public class PsiUtils {
  private PsiUtils() {}

  public static List<PsiField> getFields(final PsiClass psiClass, final VisibilityLevel visibility) {
    final List<PsiField> fields = new ArrayList<PsiField>();
    for (PsiField psiField : psiClass.getFields()) {
      final VisibilityLevel level = toVisibilityLevel(VisibilityUtil.getVisibilityModifier(psiField.getModifierList()));
      if (level.compareTo(visibility) <= 0) {
        fields.add(psiField);
      }
    }
    return fields;
  }

  public static VisibilityLevel toVisibilityLevel(String modifier) {
    if (PsiModifier.PUBLIC.equals(modifier)) return VisibilityLevel.PUBLIC;
    if (PsiModifier.PROTECTED.equals(modifier)) return VisibilityLevel.PROTECTED;
    if (PsiModifier.PRIVATE.equals(modifier)) return VisibilityLevel.PRIVATE;
    return VisibilityLevel.PACKAGE;
  }

  public static List<PsiMethod> getConstructors(PsiClass psiClass, VisibilityLevel visibility) {
    return getConstructors(Arrays.asList(psiClass.getConstructors()), visibility);
  }

  public static List<PsiMethod> getConstructors(Collection<PsiMethod> methods, VisibilityLevel visibility) {
    final List<PsiMethod> ctors = new ArrayList<PsiMethod>();
    for (PsiMethod ctor : methods) {
      if (!ctor.isConstructor()) continue;
      final VisibilityLevel level = toVisibilityLevel(VisibilityUtil.getVisibilityModifier(ctor.getModifierList()));
      if (level.weakerThan(visibility)) {
        ctors.add(ctor);
      }
    }
    return ctors;
  }

  public static List<PsiMethod> getMethods(PsiClass psiClass, VisibilityLevel visibility) {
    return getMethods(Arrays.asList(psiClass.getMethods()), visibility);
  }

  public static List<PsiMethod> getMethods(Collection<PsiMethod> methods, VisibilityLevel visibility) {
    final List<PsiMethod> result = new ArrayList<PsiMethod>();
    for (PsiMethod method : methods) {
      if (method.isConstructor()) continue;
      final VisibilityLevel level = toVisibilityLevel(VisibilityUtil.getVisibilityModifier(method.getModifierList()));
      if (level.weakerThan(visibility)) {
        result.add(method);
      }
    }
    return result;    
  }

  public static List<UmlClassProperty> getProperties(PsiClass clazz, VisibilityLevel visibility) {
    List<UmlClassProperty> result = new ArrayList<UmlClassProperty>();
    final Map<String,PsiMethod> properties = PropertyUtil.getAllProperties(clazz, true, true, false);
    for (PsiMethod method : properties.values()) {
      final VisibilityLevel level = toVisibilityLevel(VisibilityUtil.getVisibilityModifier(method.getModifierList()));
      if (level.weakerThan(visibility)) {
        result.add(new UmlClassProperty(clazz, method));
      }
    }
    return result;
  }

  public static void removePropertiesFromMethods(PsiClass clazz, List<PsiMethod> methods) {
    if (methods == null || methods.size() == 0) return;
    List<PsiMethod> toRemove = new ArrayList<PsiMethod>();
    for (PsiMethod method : methods) {
      if (PropertyUtil.isSimplePropertyAccessor(method)) {
        toRemove.add(method);
      }
    }
    methods.removeAll(toRemove);
  }

  public static void removePropertiesFromFields(PsiClass clazz, List<PsiField> fields) {
    if (fields == null || fields.size() == 0) return;
    List<PsiField> toRemove = new ArrayList<PsiField>();
    final Project project = clazz.getProject();
    final Set<String> names = PropertyUtil.getAllProperties(clazz, true, true).keySet();
    for (PsiField field : fields) {
      if (names.contains(PropertyUtil.suggestPropertyName(project, field))) {
        toRemove.add(field);
      }
    }
    fields.removeAll(toRemove);
  }

  public static PsiAnnotation[] getAnnotations(@NotNull PsiModifierListOwner listOwner, boolean inHierarchy) {
    final PsiModifierList modifierList = listOwner.getModifierList();
    if (modifierList == null) {
      return PsiAnnotation.EMPTY_ARRAY;
    }
    if (inHierarchy) {
      final Set<PsiAnnotation> all = new HashSet<PsiAnnotation>() {
        public boolean add(PsiAnnotation o) {
          // don't overwrite "higher level" annotations
          return !contains(o) && super.add(o);
        }
      };
      if (listOwner instanceof PsiMethod) {
        all.addAll(Arrays.asList(modifierList.getAnnotations()));
        return all.toArray(new PsiAnnotation[all.size()]);
      }
      if (listOwner instanceof PsiParameter && ((PsiParameter)listOwner).getDeclarationScope() instanceof PsiMethod) {
        all.addAll(Arrays.asList(modifierList.getAnnotations()));
        return all.toArray(new PsiAnnotation[all.size()]);
      }
    }
    return modifierList.getAnnotations();
  }

  @Nullable
  public static PsiClass getPsiClass(PsiAnnotation anno) {
    final String fqn;
    if (anno == null || (fqn = anno.getQualifiedName()) == null) return null;

    final Project project = anno.getProject();
    return JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.allScope(project));
  }

  public static boolean isAnonymousClass(PsiClass psiClass) {
    return psiClass instanceof PsiAnonymousClass;
  }
}
