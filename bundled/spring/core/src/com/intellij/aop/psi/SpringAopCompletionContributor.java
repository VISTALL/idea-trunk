/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.model.SpringUtils;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author peter
 */
public class SpringAopCompletionContributor extends CompletionContributor{
  public static final @NonNls String[] SPRING20_AOP_POINTCUTS = {"execution", "target", "this", "within", "@target", "@within", "@annotation", "args", "@args"};

  public SpringAopCompletionContributor() {
    extend(CompletionType.BASIC, AopCompletionData.POINTCUT_PATTERN, new CompletionProvider<CompletionParameters>(true) {
      protected void addCompletions(@NotNull final CompletionParameters parameters, final ProcessingContext context, @NotNull final CompletionResultSet result) {
        final AopPointcutExpressionFile file = (AopPointcutExpressionFile)parameters.getPosition().getContainingFile();
        final AopAdvisedElementsSearcher searcher = file.getAopModel().getAdvisedElementsSearcher();
        final boolean isSpring = searcher instanceof SpringAdvisedElementsSearcher;
        if (isSpring) {
          for (final String pointcut : SPRING20_AOP_POINTCUTS) {
            result.addElement(AopCompletionData.createPointcutDesignatorElement(pointcut));
          }

          final Module module = ModuleUtil.findModuleForPsiElement(parameters.getPosition());
          if (module != null && SpringUtils.isSpring25(module)) {
            result.addElement(AopCompletionData.createPointcutDesignatorElement("bean"));
          }
        }

        final Set<String> designators = AopCompletionData.getAllPointcutDesignators();
        result.runRemainingContributors(parameters, new Consumer<LookupElement>() {
          public void consume(final LookupElement lookupElement) {
            if (!isSpring || !designators.contains(lookupElement.getLookupString())) {
              result.addElement(lookupElement);
            }
          }
        });
      }
    });
  }

}
