/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopBundle;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopModuleService;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.ui.SimpleTextAttributes;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author peter
 */
public class AopAnnotator implements Annotator {
  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (((AopPointcutExpressionFile)psiElement.getContainingFile()).getAopModel().getAdvisedElementsSearcher().shouldSuppressErrors()) return;

    final PsiElement parent = psiElement.getParent();
    if (psiElement instanceof AopReferenceExpression) {
      if (checkReference(psiElement, holder, parent)) return;
    }
    if (psiElement instanceof AopParameterList && !(parent instanceof PsiExecutionExpression)) {
      checkEllipsisAllowance(psiElement, holder);
    }
    if (psiElement instanceof AopArrayExpression) {
      if (((AopArrayExpression)psiElement).isVarargs()) {
        if (!(parent instanceof AopReferenceHolder) || parent.getParent() instanceof AopTypeParameterList || parent.getParent() instanceof AopParameterList && parent.getParent().getParent() instanceof PsiArgsExpression) {
          holder.createErrorAnnotation(psiElement.getLastChild(), AopBundle.message("error.varargs.not.allowed.here"));
          return;
        } else if (parent.getParent() instanceof AopParameterList) {
          final PsiElement[] parameters = ((AopParameterList)parent.getParent()).getParameters();
          if (parent != parameters[parameters.length - 1]) {
            holder.createErrorAnnotation(psiElement.getLastChild(), AopBundle.message("error.varargs.not.last"));
            return;
          }
        }
      }

      final PsiPointcutExpression expression = PsiTreeUtil.getParentOfType(psiElement, PsiPointcutExpression.class);
      if (expression instanceof PsiThisExpression || expression instanceof PsiTargetExpression || expression instanceof PsiWithinExpression) {
        holder.createErrorAnnotation(psiElement.getLastChild(), AopBundle.message("error.arrays.not.allowed.here"));
      }
    }
    if (psiElement instanceof AopGenericTypeExpression) {
      final PsiPointcutExpression expression = PsiTreeUtil.getParentOfType(psiElement, PsiPointcutExpression.class);
      if (expression instanceof PsiThisExpression || expression instanceof PsiTargetExpression || expression instanceof PsiWithinExpression) {
        holder.createErrorAnnotation(((AopGenericTypeExpression)psiElement).getTypeParameterList(), AopBundle.message("error.generics.not.allowed.here"));
      }
    }

    if ((psiElement instanceof AopSubtypeExpression || psiElement instanceof AopReferenceHolder && "*".equals(psiElement.getText())) &&
        PsiTreeUtil.getParentOfType(psiElement, PsiArgsExpression.class) != null &&
        PsiTreeUtil.getParentOfType(psiElement, AopTypeParameterList.class) != null &&
        PsiTreeUtil.getParentOfType(psiElement, AopParameterList.class) != null) {
      holder.createErrorAnnotation(psiElement.getLastChild(), AopBundle.message("error.wildcards.not.allowed.here"));
    }
    else if (psiElement instanceof PsiPointcutReferenceExpression) {
      checkPointcutArgumentCount(psiElement, holder);
    }
    else if (!(psiElement.getContainingFile().getContext() instanceof XmlElement)) {
      checkAndOrNot(psiElement, holder);
    }
  }

  private static boolean checkReference(final PsiElement psiElement, final AnnotationHolder holder, final PsiElement parent) {
    final AopReferenceExpression referenceExpression = (AopReferenceExpression)psiElement;
    if (referenceExpression.getResolvability() != AopReferenceExpression.Resolvability.PLAIN) return true;

    final TextRange range = referenceExpression.getRangeInElement().shiftRight(referenceExpression.getTextRange().getStartOffset());
    final ResolveResult[] results = referenceExpression.multiResolve(false);
    if (results.length > 0) {
      for (final ResolveResult result : results) {
        final PsiElement target = result.getElement();
        if (referenceExpression.isPointcutReference()) {
          if (!(target instanceof PsiMethod) || ((PsiMethod)target).getModifierList().findAnnotation(AopConstants.POINTCUT_ANNO) == null) {
            holder.createErrorAnnotation(range, AopBundle.message("error.cannot.resolve.pointcut", referenceExpression.getReferenceName()));
            return true;
          }

          final PsiMethod pointcutMethod = referenceExpression.getContainingFile().getAopModel().getPointcutMethod();
          if (pointcutMethod != null) {
            final AopPointcut pointcut = AopModuleService.getPointcut(pointcutMethod);
            if (pointcut != null && isRecursivePointcutRef(referenceExpression, pointcut, 3)) {
              holder.createErrorAnnotation(range, AopBundle.message("error.recursive.pointcut.reference", referenceExpression));
              return true;
            }
          }
        }

        if (referenceExpression.isAnnotationReference()) {
          final boolean error;
          if (target instanceof PsiClass) {
            error = !((PsiClass)target).isAnnotationType();
          }
          else if (target instanceof PsiParameter) {
            final PsiClass psiClass = PsiUtil.resolveClassInType(((PsiParameter)target).getType());
            error = psiClass == null || !psiClass.isAnnotationType();
          }
          else {
            error = target != null;
          }

          if (error) {
            holder.createErrorAnnotation(range, AopBundle.message("error.anno.expected"));
            return true;
          }
        }

      }
      return true;
    }

    final AopReferenceExpression qualifier = referenceExpression.getQualifier();
    if (qualifier != null && qualifier.resolve() == null) return true;
    if (qualifier == null && !(referenceExpression.getParent() instanceof AopReferenceQualifier)) {
      if (parent instanceof AopMemberReferenceExpression) return true;
    }

    final String message;
    if (referenceExpression.isPointcutReference()) {
      message = AopBundle.message("error.cannot.resolve.pointcut", referenceExpression.getReferenceName());
    } else {
      message = AopBundle.message("error.cannot.resolve.symbol", referenceExpression.getReferenceName());
    }
    holder.createErrorAnnotation(range, message).setEnforcedTextAttributes(SimpleTextAttributes.ERROR_ATTRIBUTES.toTextAttributes());
    return false;
  }

  private static boolean isRecursivePointcutRef(@NotNull final AopReferenceExpression aopReferenceExpression, @NotNull final AopPointcut startPointcut, final int depth) {
    final AopPointcut pointcut = aopReferenceExpression.resolvePointcut();
    if (pointcut == null) return false;
    if (pointcut.equals(startPointcut)) return true;
    if (depth == 0) return false;

    final PsiPointcutExpression expression = pointcut.getExpression().getValue();
    final Ref<Boolean> result = Ref.create(false);
    if (expression != null) {
      expression.accept(new PsiRecursiveElementVisitor() {
        @Override public void visitElement(final PsiElement element) {
          if (result.get()) return;
          if (element instanceof PsiPointcutExpression) super.visitElement(element);

          if (element instanceof PsiPointcutReferenceExpression) {
            final PsiPointcutReferenceExpression pointcutReferenceExpression = (PsiPointcutReferenceExpression) element;
            final AopReferenceExpression referenceExpression = pointcutReferenceExpression.getReferenceExpression();
            if (referenceExpression != null && isRecursivePointcutRef(referenceExpression, startPointcut, depth - 1)) {
              result.set(true);
            }
          }
        }
      });
    }
    return result.get();
  }

  private static void checkEllipsisAllowance(final PsiElement psiElement, final AnnotationHolder holder) {
    final AopParameterList list = (AopParameterList)psiElement;
    final Set<PsiElement> ellipsises = new THashSet<PsiElement>();
    for (final PsiElement parameter : list.getParameters()) {
      if (parameter.getNode().getElementType() == AopElementTypes.AOP_DOT_DOT) {
        ellipsises.add(parameter);
      }
    }
    if (ellipsises.size() > 1) {
      for (final PsiElement ellipsis : ellipsises) {
        holder.createErrorAnnotation(ellipsis, AopBundle.message("error.double.ellipsis.prohibited"));
      }
    }
  }

  private static void checkPointcutArgumentCount(final PsiElement psiElement, final AnnotationHolder holder) {
    final PsiPointcutReferenceExpression expression = (PsiPointcutReferenceExpression)psiElement;
    final AopReferenceExpression referenceExpression = expression.getReferenceExpression();
    if (referenceExpression != null) {
      final AopPointcut aopPointcut = referenceExpression.resolvePointcut();
      if (aopPointcut != null) {
        final int expected = aopPointcut.getParameterCount();
        if (expected >= 0) {
          final AopParameterList parameterList = expression.getParameterList();
          if (parameterList != null) {
            final PsiElement[] elements = parameterList.getParameters();
            final int actual = elements.length;
            if (actual != expected) {
              holder.createErrorAnnotation(parameterList, AopBundle.message("error.invalid.number.of.arguments", expected, actual));
            }
          }
        }
      }
    }
  }

  private static void checkAndOrNot(final PsiElement psiElement, final AnnotationHolder holder) {
    if (psiElement instanceof AopBinaryExpression) {
      final PsiElement token = ((AopBinaryExpression)psiElement).getOpToken();
      if (token != null) {
        @NonNls final String text = token.getText();
        if ("and".equals(text) || "or".equals(text)) {
          holder.createErrorAnnotation(token, AopBundle.message("error.0.or.1.expected", "&&", "||"));
        }
      }
    }
    else if (psiElement instanceof AopNotExpression) {
      final AopNotExpression expression = (AopNotExpression)psiElement;
      final PsiElement token = expression.getNotToken();
      if ("not".equals(token.getText())) {
        holder.createErrorAnnotation(token, AopBundle.message("error.0.expected", "!"));
      }
    }
  }
}
