/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiMethod;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public class AopAnnotationHolder extends AopElementBase {
  public AopAnnotationHolder(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopAnnotationHolder";
  }

  public final Collection<AopPsiTypePattern> getPatterns() {
    List<AopPsiTypePattern> result = Collections.emptyList();
    for (final AopAnnotationPattern expression : getAnnotationPatterns()) {
      List<AopPsiTypePattern> portion = new ArrayList<AopPsiTypePattern>();
      for (final AopPsiTypePattern pattern : expression.getPatterns()) {
        if (pattern instanceof AndPsiTypePattern) {
          portion.add(new AndPsiTypePattern(ContainerUtil.map2Array(((AndPsiTypePattern)pattern).getPatterns(), AopPsiTypePattern.class, new Function<AopPsiTypePattern, AopPsiTypePattern>() {
            public AopPsiTypePattern fun(final AopPsiTypePattern pattern) {
              return new PsiAnnotatedTypePattern(pattern);
            }
          })));
        } else {
          portion.add(new PsiAnnotatedTypePattern(pattern));
        }
      }
      if (result == Collections.<AopPsiTypePattern>emptyList()) {
        result = portion;
      } else {
        final ArrayList<AopPsiTypePattern> newResult = new ArrayList<AopPsiTypePattern>();
        AopBinaryExpression.conjunctPatterns(result, portion, newResult);
        result = newResult;
      }
    }
    return result;
  }

  public final boolean accepts(@NotNull final PsiMethod method) {
    for (final AopPsiTypePattern pattern : getPatterns()) {
      if (pattern instanceof AndPsiTypePattern) {
        boolean accepts = true;
        for (final AopPsiTypePattern typePattern : ((AndPsiTypePattern)pattern).getPatterns()) {
          if (!PsiAnnotatedTypePattern.acceptsAnnotationPattern(method, ((PsiAnnotatedTypePattern)typePattern).getAnnotationPattern(), false)) {
            accepts = false;
            break;
          }
        }
        if (accepts) return true;
      } else {
        if (PsiAnnotatedTypePattern.acceptsAnnotationPattern(method, ((PsiAnnotatedTypePattern)pattern).getAnnotationPattern(), false)) return true;
      }
    }
    return false;
  }

  private AopAnnotationPattern[] getAnnotationPatterns() {
    return findChildrenByClass(AopAnnotationPattern.class);
  }

}