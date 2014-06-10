package com.intellij.spring.aop;

import com.intellij.aop.AopIntroduction;
import com.intellij.aop.jam.AopJavaAnnotator;
import com.intellij.psi.PsiClass;
import com.intellij.spring.model.SpringBeanEffectiveTypeProvider;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SpringAopEffectiveTypesProvider extends SpringBeanEffectiveTypeProvider {


  public void processEffectiveTypes(@NotNull final CommonSpringBean bean, final Collection<PsiClass> result) {
    final Set<PsiClass> toAdd = new THashSet<PsiClass>();
    List<PsiClass> toRemove = new SmartList<PsiClass>();
    for (final PsiClass psiClass : result) {
      for (final AopIntroduction introduction : AopJavaAnnotator.getBoundIntroductions(psiClass)) {
        ContainerUtil.addIfNotNull(introduction.getImplementInterface().getValue(), toAdd);
      }
   }
    result.addAll(toAdd);
    result.removeAll(toRemove);
  }


  
}