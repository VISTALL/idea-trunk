/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringModelVisitor;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class UnparsedCustomBeanInspection extends SpringBeanInspectionBase {

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("unparsed.custom.bean.inspection");
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  @NotNull
  public String getShortName() {
    return "UnparsedCustomBeanInspection";
  }

  protected SpringModelVisitor createVisitor(final DomElementAnnotationHolder holder, final Beans beans, final SpringModel model) {
    return new SpringModelVisitor() {

      protected boolean visitBean(CommonSpringBean bean) {
        if (bean instanceof CustomBeanWrapper) {
          final CustomBeanWrapper wrapper = (CustomBeanWrapper)bean;
          if (!wrapper.isParsed()) {
            holder.createProblem(wrapper, HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING, SpringBundle.message("unparsed.custom.bean.message"));
          }
        }
        return true;
      }
    };
  }


}