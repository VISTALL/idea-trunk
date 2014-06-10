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
import com.intellij.aop.psi.*;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.aop.Advisor;
import com.intellij.spring.model.xml.aop.BasicAdvice;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.SmartList;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SpringAopErrorsInspection extends BasicDomElementsInspection<Beans> {

  public SpringAopErrorsInspection() {
    super(Beans.class);
  }

  protected void checkDomElement(final DomElement element, final DomElementAnnotationHolder holder, final DomHighlightingHelper helper) {
    final XmlTag tag = element.getXmlTag();
    if (element instanceof BasicAdvice && tag != null) {
      final BasicAdvice advice = (BasicAdvice)element;
      if (advice.getPointcut().getXmlAttribute() == null && advice.getPointcutRef().getXmlAttribute() == null) {
        createPointcutProblem(element, holder);
      }
    }
    else if (element instanceof Advisor && tag != null) {
      final Advisor advisor = (Advisor)element;
      if (advisor.getPointcut().getXmlAttribute() == null && advisor.getPointcutRef().getXmlAttribute() == null) {
        createPointcutProblem(element, holder);
      }
    }
  }

  public ProblemDescriptor[] checkFile(@NotNull final PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
    if (file instanceof AopPointcutExpressionFile) {
      final List<ProblemDescriptor> result = new SmartList<ProblemDescriptor>();
      final PsiPointcutExpression expression = ((AopPointcutExpressionFile)file).getPointcutExpression();
      if (expression != null) {
        expression.accept(new PsiRecursiveElementVisitor() {
          @Override public void visitElement(final PsiElement element) {
            if (!(element instanceof PsiPointcutExpression)) return;
            super.visitElement(element);
            final PsiElement firstChild = element.getFirstChild();
            if (element instanceof PsiPointcutReferenceExpression || element instanceof AopNotExpression ||
                element instanceof AopBinaryExpression || element instanceof AopParenthesizedExpression) return;
            if (firstChild == null || firstChild.getFirstChild() != null) return;

            @NonNls final String text = firstChild.getText();
            if (StringUtil.isEmptyOrSpaces(text)) return;

            if (!Arrays.asList(SpringAopCompletionContributor.SPRING20_AOP_POINTCUTS).contains(text) && !"bean".equals(text)) {
              result.add(manager.createProblemDescriptor(firstChild,
                                                         SpringBundle.message("this.pointcut.designator.isn.t.supported.by.spring", text),
                                                         LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
            }
          }
        });
      }
      return result.toArray(new ProblemDescriptor[result.size()]);
    }

    return super.checkFile(file, manager, isOnTheFly);
  }

  private static void createPointcutProblem(final DomElement element, final DomElementAnnotationHolder holder) {
    holder.createProblem(element, HighlightSeverity.ERROR, SpringBundle.message("error.pointcut.or.pointcut.ref.should.be.defined"),
                         new DefineAttributeQuickFix("pointcut"), new DefineAttributeQuickFix("pointcut-ref"));
  }

  @NotNull
  public String getGroupDisplayName() {
    return AopBundle.message("inspection.group.display.name.aop");
  }

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("aop.errors.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringAopErrorsInspection";
  }

}