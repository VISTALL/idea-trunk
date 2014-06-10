/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.util.PairFunction;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * @author peter
 */
public class PsiAtArgsExpression extends AopElementBase implements PsiPointcutExpression, PsiAtPointcutDesignator{

  public PsiAtArgsExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtArgsExpression";
  }

  @Nullable
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    if (!(member instanceof PsiMethod)) return PointcutMatchDegree.FALSE;

    final AopParameterList list = getParameterList();
    if (list == null) return PointcutMatchDegree.FALSE;



    return list.matches(context, ((PsiMethod)member).getParameterList(), new PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree>() {
      public PointcutMatchDegree fun(final PsiType actualType, final AopReferenceTarget holder) {
        return actualType instanceof PsiClassType
               ? canHaveAnnotation(((PsiClassType)actualType).resolve(), holder, PointcutMatchDegree.TRUE, PointcutMatchDegree.MAYBE)
               : PointcutMatchDegree.FALSE;
      }
    });
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }


  public static PointcutMatchDegree canHaveAnnotation(@Nullable PsiClass psiClass, @Nullable final AopReferenceHolder holder, final PointcutContext context,
                                                      final PointcutMatchDegree maybeTrue, final PointcutMatchDegree maybeFalse) {
    if (holder == null) return PointcutMatchDegree.FALSE;
    return canHaveAnnotation(psiClass, context.resolve(holder), maybeTrue, maybeFalse);
  }

  public static PointcutMatchDegree canHaveAnnotation(@Nullable PsiClass psiClass, @NotNull final AopReferenceTarget holder,
                                                      final PointcutMatchDegree maybeTrue, final PointcutMatchDegree maybeFalse) {
    if (psiClass == null) return PointcutMatchDegree.FALSE;

    PsiModifierList modifierList = psiClass.getModifierList();
    if (modifierList == null) return PointcutMatchDegree.FALSE;

    final String annoName = holder.getQualifiedName();
    final PsiClass annoClass = holder.findClass();
    if (annoClass == null || !annoClass.isAnnotationType()) return PointcutMatchDegree.FALSE;

    final PsiModifierList annoModifierList = annoClass.getModifierList();
    boolean isInheritedAnno = annoModifierList != null && annoModifierList.findAnnotation(CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED) != null;
    boolean isFinal = modifierList.hasModifierProperty(PsiModifier.FINAL);
    boolean hasAnno = modifierList.findAnnotation(annoName) != null;
    if (hasAnno) return isFinal || isInheritedAnno ? PointcutMatchDegree.TRUE : maybeTrue;
    if (isFinal) return PointcutMatchDegree.FALSE;
    if (!isInheritedAnno) return maybeFalse;

    if (psiClass.isInterface()) return maybeFalse;

    Set<PsiClass> visited = new THashSet<PsiClass>();
    visited.add(psiClass);

    while (true) {
      psiClass = psiClass.getSuperClass();
      if (visited.contains(psiClass)) break;
      visited.add(psiClass);

      if (psiClass == null) break;
      modifierList = psiClass.getModifierList();
      if (modifierList != null && modifierList.findAnnotation(annoName) != null) return PointcutMatchDegree.TRUE;
    }
    return maybeFalse;
  }
}