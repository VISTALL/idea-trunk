/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.aop;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.LocalAopModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.xml.aop.BasicAdvice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public class SpringLocalAopModel extends LocalAopModel {

  public SpringLocalAopModel(@NotNull final PsiElement host, @Nullable BasicAdvice advice, @NotNull AopAdvisedElementsSearcher searcher) {
    super(host, advice == null ? null : advice.getMethod().getValue(), searcher);
  }

  @NotNull
  public ArgNamesManipulator getArgNamesManipulator() {
    return new SpringArgNamesManipulator(PsiTreeUtil.getParentOfType(getHost(), XmlTag.class));
  }

}
