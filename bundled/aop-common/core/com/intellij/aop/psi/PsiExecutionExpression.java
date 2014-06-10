/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiExecutionExpression extends MethodPatternPointcut {

  public PsiExecutionExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiExecutionExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    if (!(member instanceof PsiMethod)) return PointcutMatchDegree.FALSE;

    final AopMemberReferenceExpression methodReference = getMethodReference();
    if (methodReference == null) return PointcutMatchDegree.FALSE;

    final PsiMethod method = (PsiMethod)member;

    final AopReferenceExpression expression = methodReference.getReferenceExpression();
    if (expression == null || !expression.getRegex().matcher(method.getName()).matches()) return PointcutMatchDegree.FALSE;

    final AopModifierList modifierList = getModifierList();
    final AopParameterList parameterList = getParameterList();
    final AopReferenceHolder returnType = getReturnType();
    final AopThrowsList throwsList = getThrowsList();
    final AopAnnotationHolder annotationHolder = getAnnotationHolder();

    if (modifierList != null && !modifierList.accepts(member)) return PointcutMatchDegree.FALSE;
    if (!acceptsReturnType(returnType, method.getReturnType())) return PointcutMatchDegree.FALSE;
    if (throwsList != null && !throwsList.matches(method.getThrowsList())) return PointcutMatchDegree.FALSE;
    if (parameterList != null && parameterList.matches(context, method.getParameterList(), TypeArgumentMatcher.NO_AUTOBOXING) != PointcutMatchDegree.TRUE) return PointcutMatchDegree.FALSE;
    if (annotationHolder != null && !annotationHolder.accepts(method)) return PointcutMatchDegree.FALSE;

    if (processClass(member.getContainingClass(), method, new THashSet<PsiClass>(), methodReference.getPatterns())) return PointcutMatchDegree.TRUE;

    return PointcutMatchDegree.FALSE;

  }

  private static boolean processClass(PsiClass aClass, PsiMethod method, Set<PsiClass> visited, final Collection<AopPsiTypePattern> patterns) {
    final PsiMethod psiMethod = aClass.findMethodBySignature(method, true);
    if (psiMethod == null) return false;

    if (acceptsMethodClassAndName(aClass, patterns)) return true;

    visited.add(aClass);
    final PsiClass superClass = aClass.getSuperClass();
    if (superClass != null && !visited.contains(superClass) && processClass(superClass, method, visited, patterns)) {
      return true;
    }

    for (final PsiClass intf : aClass.getInterfaces()) {
      if (!visited.contains(intf) && processClass(intf, method, visited, patterns)) {
        return true;
      }
    }
    return false;
  }

  private static boolean acceptsMethodClassAndName(@NotNull final PsiClass declaringClass, final Collection<AopPsiTypePattern> patterns) {
    if (AopPsiTypePattern.accepts(patterns, JavaPsiFacade.getInstance(declaringClass.getProject()).getElementFactory().createType(
        declaringClass)) ==
        PointcutMatchDegree.TRUE) {
      return true;
    }
    return false;
  }

  private static boolean acceptsReturnType(final AopReferenceHolder returnType, final PsiType methodReturnType) {
    if (returnType == null || methodReturnType == null) return true;
    if (returnType.accepts(methodReturnType) == PointcutMatchDegree.TRUE) return true;
    return false;
  }

}
