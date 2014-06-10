/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringModel;
import com.intellij.spring.gutter.DomElementListCellRenderer;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.NotNullFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SpringDomAnnotator implements Annotator {

  private static final DomElementListCellRenderer RENDERER =
    new DomElementListCellRenderer(SpringBundle.message("spring.bean.with.unknown.name"));
  
  private static final NotNullFunction<SpringBaseBeanPointer, Collection<? extends PsiElement>> CONVERTER =
    new NotNullFunction<SpringBaseBeanPointer, Collection<? extends PsiElement>>() {

      @NotNull
      public Collection<? extends PsiElement> fun(final SpringBaseBeanPointer pointer) {
        return ContainerUtil.createMaybeSingletonList(pointer.getPsiElement());
      }
    };

  private static void annotateBean(DomSpringBean bean, AnnotationHolder holder) {
    final XmlTag tag = bean.getXmlTag();
    if (tag == null) return;

    final SpringModel model = SpringUtils.getSpringModel(bean);
    final List<SpringBaseBeanPointer> children = model.getChildren(SpringBeanPointer.createSpringBeanPointer(bean));
    if (children.size() > 0) {

      final NavigationGutterIconBuilder<SpringBaseBeanPointer> iconBuilder =
        NavigationGutterIconBuilder.create(SpringIcons.PARENT_GUTTER, CONVERTER);
      iconBuilder.
        setTargets(children).
        setPopupTitle(SpringBundle.message("spring.bean.class.navigate.choose.class.title")).
        setCellRenderer(RENDERER).
        setTooltipText(SpringBundle.message("spring.parent.bean.tooltip")).
        install(holder, bean.getXmlTag());
    }
  }

  public void annotate(PsiElement psiElement, AnnotationHolder holder) {
    if (psiElement instanceof XmlTag) {
      final DomElement element = DomManager.getDomManager(psiElement.getProject()).getDomElement((XmlTag)psiElement);
      if (element instanceof DomSpringBean) {
        annotateBean((DomSpringBean)element, holder);
      }
    }
  }
}
