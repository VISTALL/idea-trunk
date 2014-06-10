/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 15, 2006
 * Time: 1:06:27 PM
 */
package com.intellij.spring.model.highlighting;

import com.intellij.aop.AopBundle;
import com.intellij.aop.jam.AopJavaAnnotator;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.ide.IdeBundle;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiClass;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.aop.SpringAspect;
import com.intellij.spring.model.xml.aop.SpringPointcut;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringAopWarningsInspection extends BasicDomElementsInspection<Beans> {

  public SpringAopWarningsInspection() {
    super(Beans.class);
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  protected void checkDomElement(final DomElement element, final DomElementAnnotationHolder holder, final DomHighlightingHelper helper) {
    if (element.getXmlTag() == null) return;

    if (element instanceof SpringPointcut && ((SpringPointcut)element).getId().getXmlAttribute() == null) {
      holder.createProblem(element, HighlightSeverity.WARNING,
                           SpringBundle.message("warning.pointcut.should.have.id"), new DefineAttributeQuickFix("id"));
    }
    else if (element instanceof SpringAspect) {
      final GenericAttributeValue<SpringBeanPointer> ref = ((SpringAspect)element).getRef();
      if (ref.getXmlAttribute() == null) {
        holder.createProblem(element, HighlightSeverity.WARNING,
                             IdeBundle.message("attribute.0.should.be.defined", "ref"), new DefineAttributeQuickFix("ref"));
        return;
      }

      final SpringBeanPointer pointer = ref.getValue();
      if (pointer != null) {
        final PsiClass beanClass = pointer.getBeanClass();
        if (beanClass != null &&
            (!AopJavaAnnotator.getBoundAdvices(beanClass).isEmpty() || !AopJavaAnnotator.getBoundIntroductions(beanClass).isEmpty())) {
          holder.createProblem(ref, HighlightSeverity.WARNING, SpringBundle.message("aop.advice.matching.aspect.bean"));
        }
      }
    }

  }

  @NotNull
  public String getGroupDisplayName() {
    return AopBundle.message("inspection.group.display.name.aop");
  }

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("aop.warnings.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringAopWarningsInspection";
  }

}
