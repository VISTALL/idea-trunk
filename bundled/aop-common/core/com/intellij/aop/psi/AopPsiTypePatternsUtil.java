/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.openapi.util.Pair;
import com.intellij.util.PairFunction;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author peter
 */
public class AopPsiTypePatternsUtil {
  private static final List<Pair<Pair<Class, Class>, PairFunction>> ourAnders = new ArrayList<Pair<Pair<Class, Class>, PairFunction>>();

  private static <T extends AopPsiTypePattern, V extends AopPsiTypePattern> void addAnder(Class<T> first, Class<V> second, PairFunction<T,V,AopPsiTypePattern> function) {
    ourAnders.add(Pair.create(Pair.create((Class)first, (Class)second), (PairFunction)function));
  }

  static {
    addAnder(PsiPrimitiveTypePattern.class, PsiPrimitiveTypePattern.class, new PairFunction<PsiPrimitiveTypePattern, PsiPrimitiveTypePattern, AopPsiTypePattern>() {
      public AopPsiTypePattern fun(final PsiPrimitiveTypePattern psiPrimitiveTypePattern,
                                   final PsiPrimitiveTypePattern psiPrimitiveTypePattern1) {
        return psiPrimitiveTypePattern.accepts(psiPrimitiveTypePattern1.getType())
               ? psiPrimitiveTypePattern : AopPsiTypePattern.FALSE;
      }
    });

    addAnder(AopPsiTypePattern.class, AndPsiTypePattern.class, new PairFunction<AopPsiTypePattern, AndPsiTypePattern, AopPsiTypePattern>() {
      public AopPsiTypePattern fun(final AopPsiTypePattern aopPsiTypePattern, final AndPsiTypePattern aopPsiTypePattern1) {
        final Set<AopPsiTypePattern> result = new THashSet<AopPsiTypePattern>();
        final AopPsiTypePattern[] patterns = aopPsiTypePattern1.getPatterns();
        for (final AopPsiTypePattern pattern : patterns) {
          final AopPsiTypePattern pattern1 = conjunctPatterns(pattern, aopPsiTypePattern);
          if (pattern1 instanceof AndPsiTypePattern) {
            result.addAll(Arrays.asList(((AndPsiTypePattern)pattern1).getPatterns()));
          } else {
            result.add(pattern1);
          }
        }
        return new AndPsiTypePattern(result.toArray(new AopPsiTypePattern[result.size()]));
      }
    });

    addAnder(AopPsiTypePattern.class, AopPsiTypePattern.class, new PairFunction<AopPsiTypePattern, AopPsiTypePattern, AopPsiTypePattern>() {
      public AopPsiTypePattern fun(final AopPsiTypePattern aopPsiTypePattern, final AopPsiTypePattern aopPsiTypePattern1) {
        return new AndPsiTypePattern(aopPsiTypePattern, aopPsiTypePattern1);
      }
    });
  }


  public static AopPsiTypePattern conjunctPatterns(AopPsiTypePattern pattern1, AopPsiTypePattern pattern2) {
    if (pattern1 == AopPsiTypePattern.FALSE || pattern2 == AopPsiTypePattern.FALSE) return AopPsiTypePattern.FALSE;
    if (pattern1 == AopPsiTypePattern.TRUE) return pattern2;
    if (pattern2 == AopPsiTypePattern.TRUE) return pattern1;

    for (final Pair<Pair<Class, Class>, PairFunction> ander : ourAnders) {
      final Pair<Class, Class> pair = ander.first;
      if (pair.first.isInstance(pattern1) && pair.second.isInstance(pattern2)) return (AopPsiTypePattern)ander.second.fun(pattern1, pattern2);
      if (pair.first.isInstance(pattern2) && pair.second.isInstance(pattern1)) return (AopPsiTypePattern)ander.second.fun(pattern2, pattern1);
    }
    throw new AssertionError();
  }

}
