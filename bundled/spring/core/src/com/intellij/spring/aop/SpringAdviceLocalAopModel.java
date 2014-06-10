/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.aop;

import com.intellij.aop.AopIntroduction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.spring.model.xml.aop.BasicAdvice;
import com.intellij.spring.model.xml.aop.SpringAspect;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author peter
 */
public class SpringAdviceLocalAopModel extends SpringLocalAopModel {
  private final XmlTag myTag;
  @Nullable private final BasicAdvice myAdvice;

  public SpringAdviceLocalAopModel(final PsiElement host, @Nullable final BasicAdvice basicAdvice, final SpringAdvisedElementsSearcher searcher) {
    super(host, basicAdvice, searcher);
    myAdvice = basicAdvice;
    myTag = PsiTreeUtil.getParentOfType(host, XmlTag.class);
    assert myTag != null;
  }

  @NotNull
  public List<PsiParameter> resolveParameters(@NotNull @NonNls final String name) {
    if (!"pointcut".equals(myTag.getLocalName())) return super.resolveParameters(name);

    final List<PsiParameter> result = new SmartList<PsiParameter>();
    final GlobalSearchScope scope =
      GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(myTag.getProject()), StdFileTypes.XML);
    ReferencesSearch.search(myTag, scope).forEach(new Processor<PsiReference>() {
      public boolean process(final PsiReference reference) {
        final PsiElement psiElement = reference.getElement();
        final XmlTag xmlTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
        if (xmlTag != null) {
          final DomElement domElement = DomManager.getDomManager(psiElement.getProject()).getDomElement(xmlTag);
          if (domElement instanceof BasicAdvice) {
            final BasicAdvice advice = (BasicAdvice)domElement;
            final PsiMethod method = advice.getMethod().getValue();
            if (method != null) {
              final PsiParameter element = findParameter(name, method);
              result.add(element);
            }
          }
        }
        return true;
      }
    });

    return result;
  }

  public List<AopIntroduction> getIntroductions() {
    final List<AopIntroduction> introductions = super.getIntroductions();
    if (myAdvice != null) {
      for (final SpringAspect aspect : ((AopConfig)myAdvice.getParent().getParent()).getAspects()) {
        introductions.addAll(aspect.getIntroductions());
      }
    }
    return introductions;
  }
}
