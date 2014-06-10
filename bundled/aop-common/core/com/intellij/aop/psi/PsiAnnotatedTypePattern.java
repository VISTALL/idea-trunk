/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author peter
 */
public class PsiAnnotatedTypePattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myAnnotationPattern;

  public PsiAnnotatedTypePattern(final AopPsiTypePattern annotationPattern) {
    myAnnotationPattern = annotationPattern;
  }

  public AopPsiTypePattern getAnnotationPattern() {
    return myAnnotationPattern;
  }

  public boolean accepts(@NotNull final PsiType type) {
    if (type instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null && acceptsAnnotationPattern(psiClass, myAnnotationPattern, false)) return true;
    }
    return false;
  }

  public static boolean acceptsAnnotationPattern(@NotNull final PsiModifierListOwner owner, final AopPsiTypePattern annoPattern, boolean shoulBeInherited) {
    return acceptsAnnotationPattern(owner, annoPattern, shoulBeInherited, new THashSet<PsiModifierListOwner>());
  }

  private static boolean acceptsAnnotationPattern(final PsiModifierListOwner owner, final AopPsiTypePattern annoPattern,
                                                  final boolean shoulBeInherited,
                                                  final Set<PsiModifierListOwner> visited) {
    visited.add(owner);
    if (annoPattern instanceof NotPattern) {
      return !acceptsAnnotationPattern(owner, ((NotPattern)annoPattern).getInnerPattern(), shoulBeInherited);
    }

    final PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      for (final PsiAnnotation annotation : modifierList.getAnnotations()) {
        final PsiJavaCodeReferenceElement element = annotation.getNameReferenceElement();
        if (element != null) {
          final PsiElement psiElement = element.resolve();
          if (psiElement instanceof PsiClass) {
            final PsiClass annoClass = (PsiClass)psiElement;
            if (annoPattern.accepts(JavaPsiFacade.getInstance(psiElement.getProject()).getElementFactory().createType(annoClass))) {
              final PsiModifierList list = annoClass.getModifierList();
              return !shoulBeInherited || list != null && list.findAnnotation(CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED) != null;
            }
          }
        }
        final String qualifiedName = annotation.getQualifiedName();
        if (qualifiedName != null && annoPattern.accepts(qualifiedName)) {
          return true;
        }
      }
    }
    if (owner instanceof PsiClass) {
      PsiClass superClass = ((PsiClass) owner).getSuperClass();
      return superClass != null && !visited.contains(superClass) && acceptsAnnotationPattern(superClass, annoPattern, true);
    }

    return false;
  }

  @NotNull
  public PointcutMatchDegree canBeAssignableFrom(@NotNull final PsiType type) {
    return PointcutMatchDegree.valueOf(accepts(type));
  }
}
