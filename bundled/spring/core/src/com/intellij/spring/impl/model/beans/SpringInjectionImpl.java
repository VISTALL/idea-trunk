/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringInjection;
import com.intellij.spring.model.xml.beans.SpringValue;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringInjectionImpl extends SpringValueHolderImpl implements SpringInjection {

  @Nullable
  public PsiType[] getTypesByValue() {

    Project project = getManager().getProject();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);

    final SpringBeanPointer refAttrPointer = getRefAttr().getValue();
    PsiClass psiClass;

    if (DomUtil.hasXml(getValueAttr())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(CommonClassNames.JAVA_LANG_STRING, scope);
      return psiClass == null ? null : new PsiType[]{JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(psiClass)};
    }
    else if (refAttrPointer != null) {
      final PsiClass[] classes = refAttrPointer.getEffectiveBeanType();
      return ContainerUtil.map2Array(classes, PsiType.class, new Function<PsiClass, PsiType>() {
        public PsiType fun(PsiClass psiClass) {
          return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(psiClass);
        }
      });
    }
    else {
      return super.getTypesByValue();
    }
  }

  public GenericDomValue<?> getValueElement() {
    final SpringValue springValue = getValue();
    if (!DomUtil.hasXml(springValue)) return getValueAttr();
    return springValue;
  }

}
