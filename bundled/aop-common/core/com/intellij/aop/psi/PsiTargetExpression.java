/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.MethodSignatureUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiTargetExpression extends PsiTypedPointcutExpression {

  public PsiTargetExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiTargetExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    final PsiClass psiClass = member.getContainingClass();
    final AopReferenceHolder baseClassPattern = getTypeReference();
    if (baseClassPattern == null || psiClass == null) return PointcutMatchDegree.FALSE;

    final PsiClass myClass = context.resolve(baseClassPattern).findClass();
    if (myClass == null || !InheritanceUtil.isInheritorOrSelf(myClass, psiClass, true)) return PointcutMatchDegree.FALSE;

    if (member instanceof PsiMethod) {
      final PsiMethod method = (PsiMethod)member;
      if (MethodSignatureUtil.findMethodBySuperMethod(myClass, method, true) == method) {
        return PointcutMatchDegree.TRUE;
      }
    }
    return PointcutMatchDegree.FALSE;
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }

  public static PointcutMatchDegree canBeInstanceOf(final PsiClass psiClass, final boolean allowPatterns, @Nullable final AopTypeExpression typeExpression) {
    if (typeExpression == null) return PointcutMatchDegree.FALSE;

    if (typeExpression instanceof AopReferenceExpression) {
      final PsiElement psiElement = ((AopReferenceExpression)typeExpression).resolve();
      if (psiElement instanceof PsiClass) {
        final PointcutMatchDegree degree = canBeInstanceOf(allowPatterns, (PsiClass)psiElement, psiClass);
        if (degree != null) return degree;
      }
      if (!allowPatterns) return PointcutMatchDegree.FALSE;
    }
    if (typeExpression instanceof AopSubtypeExpression && !allowPatterns) return PointcutMatchDegree.FALSE;


    final Collection<AopPsiTypePattern> typePatterns = typeExpression.getPatterns();
    boolean maybe = false;
    for (final AopPsiTypePattern typePattern : typePatterns) {
      final PointcutMatchDegree degree = typePattern.canBeAssignableFrom(
        JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass));
      if (degree == PointcutMatchDegree.TRUE) return PointcutMatchDegree.TRUE;
      if (degree == PointcutMatchDegree.MAYBE) maybe = true;
    }
    return maybe ? PointcutMatchDegree.MAYBE : PointcutMatchDegree.FALSE;
  }

  @Nullable
  public static PointcutMatchDegree canBeInstanceOf(final boolean allowPatterns, final PsiClass superClass, final PsiClass subClass) {
    if (subClass.getManager().areElementsEquivalent(subClass, superClass) || !allowPatterns && subClass.isInheritor(superClass, true)) {
      return PointcutMatchDegree.TRUE;
    }
    if (subClass.hasModifierProperty(PsiModifier.FINAL)) return PointcutMatchDegree.FALSE;
    if (!allowPatterns && superClass.isInterface()) return PointcutMatchDegree.MAYBE;
    return null;
  }
}
