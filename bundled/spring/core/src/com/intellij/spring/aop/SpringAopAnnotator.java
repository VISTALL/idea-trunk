/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.aop;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopIntroduction;
import com.intellij.aop.jam.AopJavaAnnotator;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.aop.DeclareParents;
import com.intellij.spring.model.xml.aop.SpringAopAdvice;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.xml.util.XmlTagUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author peter
 */
public class SpringAopAnnotator implements LineMarkerProvider {

  public LineMarkerInfo getLineMarkerInfo(final PsiElement element) {
    return null;
  }

  public void collectSlowLineMarkers(final List<PsiElement> elements, final Collection<LineMarkerInfo> result) {
    if (elements.isEmpty()) return;

    final PsiFile file = elements.get(0).getContainingFile();
    if (!(file instanceof XmlFile)) return;

    final XmlFile xmlFile = (XmlFile)file;
    final DomFileElement<Beans> fileElement = DomManager.getDomManager(xmlFile.getProject()).getFileElement(xmlFile, Beans.class);
    if (fileElement == null) return;

    for (final PsiElement element : elements) {
      annotate(element, result);
    }
  }

  private static void annotate(PsiElement psiElement, final Collection<LineMarkerInfo> result) {
    if (psiElement instanceof XmlToken && psiElement.getParent() instanceof XmlTag) {
      final XmlTag tag = (XmlTag)psiElement.getParent();
      if (XmlTagUtil.getStartTagNameElement(tag) == psiElement) {
        final DomElement element = DomManager.getDomManager(psiElement.getProject()).getDomElement(tag);
        if (element instanceof SpringAopAdvice) {
          final SpringAopAdvice advice = (SpringAopAdvice)element;
          result.add(AopJavaAnnotator.addNavigationToInterceptedMethods(advice, advice.getSearcher()).createLineMarkerInfo(psiElement));
        }
        else if (element instanceof DeclareParents) {
          final NavigationGutterIconBuilder<PsiElement> builder = AopJavaAnnotator.addNavigationToIntroducedClasses((DeclareParents)element) ;
          if (builder != null) {
            result.add(builder.createLineMarkerInfo(psiElement));
          }
        }
        else if (element instanceof DomSpringBean) {
          final PsiClass psiClass = ((DomSpringBean)element).getBeanClass();
          if (psiClass != null) {
            final Map<AopAdvice,Integer> advices = AopJavaAnnotator.getBoundAdvices(psiClass);
            if (!advices.isEmpty()) {
              result.add(AopJavaAnnotator.addNavigationToBoundAdvices(advices).createLineMarkerInfo(psiElement));
            }
            final List<AopIntroduction> introductions = AopJavaAnnotator.getBoundIntroductions(psiClass);
            if (!introductions.isEmpty()) {
              result.add(AopJavaAnnotator.addNavigationToBoundIntroductions(introductions).createLineMarkerInfo(psiElement));
            }
          }
        }
      }
    }
  }
}
