/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class AbstractBeanReferencesInspection extends SpringBeanInspectionBase {

  protected void checkBean(SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {
    for (SpringValueHolderDefinition property: SpringUtils.getValueHolders(springBean)) {
      checkAbstractBeanReferences(property, holder);
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.bean.abstract.bean.references.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "AbstractBeanReferencesInspection";
  }

  private static void checkAbstractBeanReferences(final SpringValueHolderDefinition definition, final DomElementAnnotationHolder holder) {
    final GenericDomValue<SpringBeanPointer> refElement = definition.getRefElement();
    if (refElement != null) {
      final SpringBeanPointer ref = refElement.getValue();
      if (ref != null) {
        checkNotAbstract(refElement, ref, holder);
      }
    }

    if (definition instanceof SpringValueHolder) {
      final SpringValueHolder springInjection = (SpringValueHolder)definition;
      checkSpringRefBeans(springInjection.getRef(), holder);

      if (DomUtil.hasXml(springInjection.getBean())) {
        final SpringBean innerBean = springInjection.getBean();
        checkNotAbstract(innerBean, SpringBeanPointer.createSpringBeanPointer(innerBean), holder);
      }

      checkIdrefBeans(springInjection.getIdref(), holder);

      if (DomUtil.hasXml(springInjection.getList())) {
        checkCollectionReferences(springInjection.getList(), holder);
      }
      if (DomUtil.hasXml(springInjection.getSet())) {
        checkCollectionReferences(springInjection.getSet(), holder);
      }

      if (DomUtil.hasXml(springInjection.getMap())) {
        checkMapReferences(springInjection.getMap(), holder);
      }
    }
  }

  private static void checkNotAbstract(final DomElement annotated, final SpringBeanPointer springBean, final DomElementAnnotationHolder holder) {
     if (springBean.isAbstract())  {
        holder.createProblem(annotated, SpringBundle.message("spring.bean.referenced.by.abstract.bean"));
     }
  }

  private static void checkMapReferences(final SpringMap map, final DomElementAnnotationHolder beans) {
    for (SpringEntry entry : map.getEntries()) {
      checkAbstractBeanReferences(entry, beans);
    }
  }

  private static void checkIdrefBeans(final Idref idref, final DomElementAnnotationHolder holder) {
    final SpringBeanPointer local = idref.getLocal().getValue();
    if (local != null) {
      checkNotAbstract(idref.getLocal(), local, holder);
    }
    final SpringBeanPointer bean = idref.getBean().getValue();
    if (bean != null) {
      checkNotAbstract(idref.getBean(), bean, holder);
    }
  }

  private static void checkSpringRefBeans(final SpringRef springRef, final DomElementAnnotationHolder holder) {
    if (DomUtil.hasXml(springRef)) {
      final SpringBeanPointer bean = springRef.getBean().getValue();
      if (bean != null) {
        checkNotAbstract(springRef.getBean(), bean, holder);
      }
      final SpringBeanPointer local = springRef.getLocal().getValue();
      if (local != null) {
        checkNotAbstract(springRef.getLocal(), local, holder);
      }
    }
  }

  private static void checkCollectionReferences(final CollectionElements elements, final DomElementAnnotationHolder holder) {
    for (SpringRef springRef : elements.getRefs()) {
      checkSpringRefBeans(springRef, holder);
    }
    for (Idref idref : elements.getIdrefs()) {
      checkIdrefBeans(idref, holder);
    }
    for (ListOrSet listOrSet : elements.getLists()) {
      checkCollectionReferences(listOrSet, holder);
    }
    for (ListOrSet listOrSet : elements.getSets()) {
      checkCollectionReferences(listOrSet, holder);
    }
    for (SpringBean innerBean : elements.getBeans()) {
       checkNotAbstract(innerBean, SpringBeanPointer.createSpringBeanPointer(innerBean), holder);
    }
    for (SpringMap map : elements.getMaps()) {
      checkMapReferences(map, holder);
    }
  }
}
