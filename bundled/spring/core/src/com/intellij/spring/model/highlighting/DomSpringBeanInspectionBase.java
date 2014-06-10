/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.Consumer;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementsInspection;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public abstract class DomSpringBeanInspectionBase extends DomElementsInspection<Beans> {

  public DomSpringBeanInspectionBase() {
    super(Beans.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return SpringBundle.message("model.inspection.group.name");
  }

  public void checkFileElement(final DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder) {
    final XmlFile xmlFile = domFileElement.getFile();
    final Beans beans = domFileElement.getRootElement();
    final SpringModel model = SpringManager.getInstance(xmlFile.getProject()).getSpringModelByFile(xmlFile);
    final Consumer<DomElement> consumer = new Consumer<DomElement>() {
      public void consume(final DomElement element) {
        if (element instanceof DomSpringBean) {
          checkBean((DomSpringBean)element, beans, holder, model);
        }
        else if (!(element instanceof GenericDomValue) && DomUtil.hasXml(element)){
          checkChildren(element, this);
        }
      }
    };
    consumer.consume(domFileElement.getRootElement());
  }

  protected void checkBean(DomSpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {}
}