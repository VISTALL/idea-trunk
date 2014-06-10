package com.intellij.spring.model.highlighting.jam;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.spring.model.jam.utils.SpringJamUtils;
import com.intellij.spring.model.jam.javaConfig.SpringJavaExternalBean;
import com.intellij.spring.model.xml.DomSpringBean;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpringJavaBeanReferencesFindUsagesHandler extends FindUsagesHandler {
  private final DomSpringBean mySpringBean;

  public SpringJavaBeanReferencesFindUsagesHandler(final DomSpringBean springBean) {
    super(springBean.getIdentifyingPsiElement());
    mySpringBean = springBean;
  }

  @NotNull
  public PsiElement[] getSecondaryElements() {
    final List<SpringJavaExternalBean> list = SpringJamUtils.findExternalBeanReferences(mySpringBean);

    Set<PsiElement> psiElements = new HashSet<PsiElement>();

    for (SpringJavaExternalBean externalBean : list) {
      final PsiMethod method = externalBean.getPsiElement();
      if (method != null) {
         psiElements.add(method);
      }
    }
    return psiElements.toArray(new PsiElement[psiElements.size()]);
  }
}
