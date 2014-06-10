/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiArgsExpression extends AopElementBase implements PsiPointcutExpression{
  private static final TypeArgumentMatcher ARGS_MATCHER = new TypeArgumentMatcher() {
      public PointcutMatchDegree fun(final PsiType actualType, final AopReferenceTarget target) {
        if (super.fun(actualType, target) == PointcutMatchDegree.TRUE || target.isAssignableFrom(actualType)) {
          return PointcutMatchDegree.TRUE;
        }

        final String typeText = target.getQualifiedName();
        final PsiPrimitiveType primitiveType = PsiElementFactoryImpl.getPrimitiveType(typeText);
        if (primitiveType != null && !(actualType instanceof PsiPrimitiveType)) {
          if (primitiveType == PsiPrimitiveType.getUnboxedType(actualType)) return PointcutMatchDegree.TRUE;
        }
        else if (actualType instanceof PsiPrimitiveType) {
          return PointcutMatchDegree.valueOf(Comparing.equal(((PsiPrimitiveType)actualType).getBoxedTypeName(), typeText) ||
                                             CommonClassNames.JAVA_LANG_NUMBER.equals(typeText) ||
                                             CommonClassNames.JAVA_LANG_OBJECT.equals(typeText));
        }
        return PointcutMatchDegree.FALSE;
      }
    };

  public PsiArgsExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiArgsExpression";
  }

  @Nullable
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    if (member instanceof PsiMethod) {
      final AopParameterList parameterList = getParameterList();
      return parameterList != null ? parameterList.matches(context, ((PsiMethod)member).getParameterList(), ARGS_MATCHER) : PointcutMatchDegree.FALSE;
    }
    return PointcutMatchDegree.FALSE;
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}