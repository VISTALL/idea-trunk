/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.psi.PsiClass;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomJavaUtil;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementsInspection;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class RequiredBeanTypeInspection extends DomElementsInspection<Beans> {

  public RequiredBeanTypeInspection() {
    super(Beans.class);
  }

  @Override
  protected void checkDomElement(DomElement element, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
    if (element instanceof GenericAttributeValue) {
      Object value = ((GenericAttributeValue)element).getValue();

      if (value instanceof SpringBeanPointer) {
        final RequiredBeanType type = element.getAnnotation(RequiredBeanType.class);
        if (type != null) {
          SpringBeanPointer springBeanPointer = (SpringBeanPointer)value;

          PsiClass[] classes = springBeanPointer.getEffectiveBeanType();
          final PsiClass requiredClass = DomJavaUtil.findClass(type.value(), element);
          if (requiredClass != null) {
            boolean isAssignable = false;
            for (PsiClass psiClass : classes) {
              if (InheritanceUtil.isInheritorOrSelf(psiClass, requiredClass, true)) {
                isAssignable = true;
                break;
              }
            }
            if (!isAssignable) {
              if (holder != null) {
                final String message = SpringBundle.message("bean.must.be.of.type", requiredClass.getQualifiedName());
                holder.createProblem(element, message);
              }
            }
          }
        }
      }
    }
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return SpringBundle.message("model.inspection.group.name");
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("required.spring.bean.type.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "RequiredBeanTypeInspection";
  }
}