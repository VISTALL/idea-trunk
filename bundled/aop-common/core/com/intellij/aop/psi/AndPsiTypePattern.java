/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiType;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
*/
public class AndPsiTypePattern extends AopPsiTypePattern {
  private final AopPsiTypePattern[] myPatterns;

  public AndPsiTypePattern(final AopPsiTypePattern... patterns) {
    myPatterns = patterns;
  }

  public AopPsiTypePattern[] getPatterns() {
    return myPatterns;
  }

  public boolean accepts(@NotNull final PsiType type) {
    for (final AopPsiTypePattern typePattern : myPatterns) {
      if (!typePattern.accepts(type)) return false;
    }
    return true;
  }

  public boolean processPackages(final PsiManager manager, final Processor<PsiPackage> processor) {
    final Ref<THashSet<PsiPackage>> set = Ref.create(new THashSet<PsiPackage>());
    myPatterns[0].processPackages(manager, new CommonProcessors.CollectProcessor<PsiPackage>(set.get()));
    for (int i = 1; i < myPatterns.length; i++) {
      AopPsiTypePattern pattern = myPatterns[i];
      final THashSet<PsiPackage> all = set.get();
      set.set(new THashSet<PsiPackage>());
      pattern.processPackages(manager, new Processor<PsiPackage>() {
        public boolean process(final PsiPackage psiPackage) {
          if (all.contains(psiPackage)) {
            set.get().add(psiPackage);
          }
          return true;
        }
      });
    }
    for (final PsiPackage psiPackage : set.get()) {
      if (!processor.process(psiPackage)) return false;
    }
    return true;
  }
}
