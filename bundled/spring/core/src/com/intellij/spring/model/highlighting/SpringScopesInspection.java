/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.highlighting;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import com.intellij.spring.model.xml.beans.SpringBeanScope;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.SpringBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public class SpringScopesInspection extends BasicDomElementsInspection<Beans> {

  protected void checkDomElement(final DomElement element, final DomElementAnnotationHolder holder, final DomHighlightingHelper helper) {
    if (element instanceof GenericDomValue) {
      GenericDomValue value = (GenericDomValue)element;
      if (SpringBeanScope.class.equals(DomUtil.getGenericValueParameter(value.getDomElementType()))) {
        helper.checkResolveProblems(value, holder);
      }
    }
  }

  public SpringScopesInspection() {
    super(Beans.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return SpringBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("scopes.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringScopesInspection";
  }
}
