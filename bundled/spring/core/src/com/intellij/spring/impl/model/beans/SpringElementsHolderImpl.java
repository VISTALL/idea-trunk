/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringElementsHolder;
import com.intellij.spring.model.xml.beans.SpringRef;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringElementsHolderImpl implements SpringElementsHolder {

  @Nullable
  public PsiType[] getTypesByValue() {
    //todo: this method can return PsiType[] if value type is FactoryBean which produces multiple product types 
    Project project = getManager().getProject();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);

    PsiClass psiClass = null;

    if (DomUtil.hasXml(getValue())) {
      final PsiType type = getValue().getType().getValue();
      if (type != null) {
        return new PsiType[]{type};
      }
      else {
        psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(String.class.getCanonicalName(), scope);
      }
    }
    else if (DomUtil.hasXml(getBean())) {
      final PsiClass[] classes = SpringUtils.getEffectiveBeanTypes(getBean());
      if (classes.length > 0) {
        psiClass = classes[0];
      }
    }
    else if (DomUtil.hasXml(getRef())) {
      final SpringRef springRef = getRef();

      final SpringBeanPointer beanPointer = springRef.getBean().getValue();
      if (beanPointer != null) {
        final PsiClass[] classes = beanPointer.getEffectiveBeanType();
        if (classes.length > 0) {
          psiClass = classes[0];
        }
      }
      else {
        final SpringBeanPointer localPointer = springRef.getLocal().getValue();
        if (localPointer != null) {
          final PsiClass[] classes = localPointer.getEffectiveBeanType();
          if (classes.length > 0) {
            psiClass = classes[0];
          }
        }
        else {
          final SpringBeanPointer parentPointer = springRef.getParentAttr().getValue();
          if (parentPointer != null) {
            psiClass = parentPointer.getBeanClass();
          }
        }
      }
    }
    else if (DomUtil.hasXml(getIdref())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(String.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getList())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(List.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getMap())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Map.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getSet())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Set.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getProps())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Properties.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getNull())) {
      return new PsiType[]{PsiType.NULL};
    }

    return psiClass == null ? null : new PsiType[] {JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(psiClass)};
  }


}
