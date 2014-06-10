/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring;

import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider;
import com.intellij.jam.JamService;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.jam.JamPsiMemberSpringBean;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.custom.CustomBeanFakePsiElement;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public class SpringTypeDeclarationProvider implements TypeDeclarationProvider{
  public PsiElement[] getSymbolTypeDeclarations(final PsiElement symbol) {
    if (symbol instanceof CustomBeanFakePsiElement) {
      return getBeanTypeDeclaration(((CustomBeanFakePsiElement)symbol).getBean());
    }
    if (symbol instanceof XmlTag) {
      final XmlTag tag = (XmlTag)symbol;
      final DomElement element = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
      if (element instanceof DomSpringBean) {
        return getBeanTypeDeclaration((DomSpringBean)element);
      }
    }
    if (symbol instanceof PsiAnnotation) {
      final PsiMember member = PsiTreeUtil.getParentOfType(symbol, PsiMember.class);
      final CommonSpringBean springBean =
        member == null ? null : JamService.getJamService(symbol.getProject()).getJamElement(JamPsiMemberSpringBean.class, member);
      if (springBean != null) {
        return getBeanTypeDeclaration(springBean);
      }
    }
    return null;
  }

  @Nullable
  private static PsiElement[] getBeanTypeDeclaration(final CommonSpringBean bean) {
    final PsiClass psiClass = bean.getBeanClass(true);
    return psiClass != null ? new PsiElement[]{psiClass} : null;
  }
}
