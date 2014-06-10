/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.PairFunction;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public abstract class AopAbstractList<T> extends AopElementBase {
  private static final TokenSet LIST_ELEMENT_TYPES = TokenSet.create(AopElementTypes.AOP_DOT_DOT, AopElementTypes.AOP_REFERENCE_HOLDER);
  private final NotNullLazyValue<ArrayTailCondition<T>> myMatcher;

  public AopAbstractList(@NotNull final ASTNode node) {
    super(node);
    myMatcher = new NotNullLazyValue<ArrayTailCondition<T>>() {
      @NotNull
      protected ArrayTailCondition<T> compute() {
        return createMatcher(getParameters(), 0);
      }
    };
  }

  protected abstract PsiType getPsiType(@NotNull T t);

  public final PsiElement[] getParameters() {
    return findChildrenByType(LIST_ELEMENT_TYPES, PsiElement.class);
  }

  public final PointcutMatchDegree accepts(final PointcutContext context, T[] list, final PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher) {
    return myMatcher.getValue().value(context, matcher, list, 0);
  }

  private ArrayTailCondition<T> createMatcher(final PsiElement[] aopParameters, final int matchStart) {
    if (matchStart >= aopParameters.length) return new ArrayTailCondition<T>() {
      public PointcutMatchDegree value(final PointcutContext context,
                                       final PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher, final T[] array, final int start) {
        return PointcutMatchDegree.valueOf(start >= array.length);
      }
    };
    final PsiElement psiElement = aopParameters[matchStart];
    final ArrayTailCondition<T> tail = createMatcher(aopParameters, matchStart + 1);
    if (psiElement instanceof AopReferenceHolder) {
      final AopReferenceHolder pattern = (AopReferenceHolder)psiElement;
      return new ArrayTailCondition<T>() {
        public PointcutMatchDegree value(final PointcutContext context,
                                         final PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher, final T[] array,
                                         final int start) {
          if (start >= array.length) return PointcutMatchDegree.FALSE;
          return PointcutMatchDegree.and(matcher.fun(getPsiType(array[start]), context.resolve(pattern)), tail.value(context, matcher, array, start + 1));
        }
      };
    }
    return new ArrayTailCondition<T>() {
      public PointcutMatchDegree value(final PointcutContext context,
                                       final PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher, final T[] array, final int start) {
        PointcutMatchDegree result = PointcutMatchDegree.FALSE;
        for (int i = start; i < array.length; i++) {
          result = PointcutMatchDegree.or(result, tail.value(context, matcher, array, i));
        }
        return PointcutMatchDegree.or(result, PointcutMatchDegree.valueOf(matchStart == aopParameters.length - 1));
      }
    };
  }

  private interface ArrayTailCondition<T> {
    ArrayTailCondition TRUE = new ArrayTailCondition<Object>() {
      public PointcutMatchDegree value(final PointcutContext context,
                                       final PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher, final Object[] array,
                                       final int start) {
        return PointcutMatchDegree.TRUE;
      }
    };

    PointcutMatchDegree value(final PointcutContext context, final PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher, T[] array,
                              int start);
  }

}
