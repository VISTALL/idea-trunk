/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.Alias;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.text.StringTokenizer;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuplicatedBeanNamesInspection extends SpringBeanInspectionBase {

  public void checkFileElement(final DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder) {
    final XmlFile xmlFile = domFileElement.getFile();
    final SpringModel model = SpringManager.getInstance(xmlFile.getProject()).getSpringModelByFile(xmlFile);
    final Beans beans = domFileElement.getRootElement();
    for (CommonSpringBean bean : SpringUtils.getChildBeans(beans, false)) {
      if (bean instanceof DomSpringBean) {
        checkBean((DomSpringBean)bean, holder, model);
      }
    }
    for (Alias alias : beans.getAliases()) {
      checkAlias(alias, holder, model);
    }
  }

  private static void checkBean(final DomSpringBean bean, final DomElementAnnotationHolder holder, final SpringModel springModel) {

    final String id = bean.getId().getStringValue();
    if (id != null && springModel.isNameDuplicated(id)) {
      holder.createProblem(bean.getId(), HighlightSeverity.ERROR, SpringBundle.message("spring.bean.dublicate.bean.name"));
    }

    if (bean instanceof SpringBean) {
      final SpringBean springBean = (SpringBean)bean;
      final GenericAttributeValue<List<String>> name = springBean.getName();
      final String value = name.getStringValue();
      if (value != null) {
        final StringTokenizer tokenizer = new StringTokenizer(value, SpringUtils.SPRING_DELIMITERS);
        while (tokenizer.hasMoreTokens()) {
          final String s = tokenizer.nextToken();
          if (springModel.isNameDuplicated(s)) {
            holder.createProblem(name, HighlightSeverity.ERROR, SpringBundle.message("spring.bean.dublicate.bean.name"),
                                 TextRange.from(tokenizer.getCurrentPosition() - s.length() + 1, s.length()));
          }
        }
      }
    }
  }

  private static void checkAlias(final Alias alias, final DomElementAnnotationHolder holder, final SpringModel model) {

    final GenericAttributeValue<String> value = alias.getAlias();
    final String aliasName = value.getStringValue();
    if (aliasName != null && model.isNameDuplicated(aliasName)) {
      holder.createProblem(value, HighlightSeverity.ERROR, SpringBundle.message("spring.bean.dublicate.bean.name"));
    }
  }
  
  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.bean.duplicated.bean.name.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "DuplicatedBeanNamesInspection";
  }
}