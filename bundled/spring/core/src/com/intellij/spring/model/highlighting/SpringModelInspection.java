/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 15, 2006
 * Time: 1:06:27 PM
 */
package com.intellij.spring.model.highlighting;

import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBeanScope;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.impl.ExtendsClassChecker;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringModelInspection extends BasicDomElementsInspection<Beans> {
  protected boolean shouldCheckResolveProblems(final GenericDomValue value) {
    return !SpringBeanScope.class.equals(DomUtil.getGenericValueParameter(value.getDomElementType())) &&
           super.shouldCheckResolveProblems(value);

  }

  public SpringModelInspection() {
    super(Beans.class);
  }

  protected void checkDomElement(final DomElement element, final DomElementAnnotationHolder holder, final DomHighlightingHelper helper) {
    final int oldSize = holder.getSize();
    super.checkDomElement(element, holder, helper);

    if (oldSize == holder.getSize() && element instanceof GenericDomValue) {
      ExtendsClassChecker.checkExtendsClassInReferences((GenericDomValue)element, holder);
    }
  }
  

  @NotNull
  public String getGroupDisplayName() {
    return SpringBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("model.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringModelInspection";
  }
}