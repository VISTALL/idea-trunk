/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.*;
import com.intellij.util.Processor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/**
 * @author peter
 */
public abstract class AopPsiTypePattern {
  public static final AopPsiTypePattern FALSE = new AopPsiTypePattern() {
    public boolean accepts(@NotNull final PsiType type) {
      return false;
    }

    @NotNull
    public PointcutMatchDegree canBeAssignableFrom(@NotNull final PsiType type) {
      return PointcutMatchDegree.FALSE;
    }
  };
  public static final AopPsiTypePattern TRUE = new AopPsiTypePattern() {
    public boolean accepts(@NotNull final PsiType type) {
      return true;
    }

    public boolean accepts(@NotNull final String qualifiedName) {
      return true;
    }

    public boolean processPackages(final PsiManager manager, final Processor<PsiPackage> processor) {
      return processSubPackages(JavaPsiFacade.getInstance(manager.getProject()).findPackage(""), processor);
    }
    
    @NotNull
    public PointcutMatchDegree canBeAssignableFrom(@NotNull final PsiType type) {
      return PointcutMatchDegree.TRUE;
    }
  };

  public abstract boolean accepts(@NotNull PsiType type);

  public boolean accepts(@NotNull String qualifiedName) {
    return false;
  }

  public boolean processPackages(PsiManager manager, Processor<PsiPackage> processor) {
    return true;
  }

  @NotNull
  public PointcutMatchDegree canBeAssignableFrom(@NotNull PsiType type) {
    return canBeAssignableFrom(type, new THashSet<PsiType>());
  }

  private PointcutMatchDegree canBeAssignableFrom(final PsiType type, final Set<PsiType> visited) {
    visited.add(type);
    if (accepts(type)) return PointcutMatchDegree.TRUE;
    boolean maybe = false;
    for (final PsiType superType : getSuperTypes(type)) {
      if (!visited.contains(superType)) {
        final PointcutMatchDegree degree = canBeAssignableFrom(superType, visited);
        if (degree == PointcutMatchDegree.TRUE) return degree;
        maybe = degree == PointcutMatchDegree.MAYBE;
      }
    }
    return maybe ? PointcutMatchDegree.MAYBE : PointcutMatchDegree.FALSE;
  }

  private static PsiType[] getSuperTypes(final PsiType type) {
    if (type instanceof PsiWildcardType && ((PsiWildcardType)type).getBound() == null) {
      return PsiType.EMPTY_ARRAY;
    }
    return type.getSuperTypes();
  }

  protected static boolean processSubPackages(final PsiPackage pkg, final Processor<PsiPackage> processor) {
    if (!processor.process(pkg)) return false;
    for (final PsiPackage aPackage : pkg.getSubPackages()) {
      if (!processSubPackages(aPackage, processor)) return false;
    }
    return true;
  }

  public static PointcutMatchDegree accepts(AopTypeExpression expression, PsiType psiType) {
    return accepts(expression.getPatterns(), psiType);
  }

  public static PointcutMatchDegree accepts(final Collection<AopPsiTypePattern> patterns, final PsiType psiType) {
    for (final AopPsiTypePattern pattern : patterns) {
      if (pattern.accepts(psiType)) return PointcutMatchDegree.TRUE;
    }
    return PointcutMatchDegree.FALSE;
  }
}
