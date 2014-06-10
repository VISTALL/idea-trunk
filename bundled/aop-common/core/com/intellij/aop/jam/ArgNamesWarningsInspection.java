/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.jam;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopBundle;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.*;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.*;
import com.intellij.psi.PsiThisExpression;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author peter
 */
public class ArgNamesWarningsInspection extends AbstractArgNamesInspection {
  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  protected void checkAnnotation(final PsiParameter[] parameters, final ProblemsHolder holder,
                                 final ArgNamesManipulator manipulator, final PsiMethod method) {
    if (manipulator.getArgNames() == null) {
      if (!canInferParameters(parameters, manipulator, method)) {

        holder.registerProblem(manipulator.getArgNamesProblemElement(),
                               AopBundle.message("warning.argNames.should.be.defined", manipulator.getArgNamesAttributeName()),
                               new SetArgNamesQuickFix(
                                   AopBundle.message("quickfix.name.define.attribute", manipulator.getArgNamesAttributeName()), true,
                                   manipulator, method));
        return;
      }
    }

    final AopAdviceType adviceType = manipulator.getAdviceType();
    if (parameters.length > 0 && adviceType != null && adviceType != AopAdviceType.AROUND) {
      if (parameters[0].getType().equalsToText(AopConstants.PROCEEDING_JOIN_POINT)) {
        holder.registerProblem(manipulator.getArgNamesProblemElement(), AopBundle.message("error.pjp.not.allowed"));
      }
    }
  }

  private static boolean canInferParameters(final PsiParameter[] parameters, ArgNamesManipulator manipulator, PsiMethod method) {
    if (parameters.length == 0) return true;

    Set<PsiParameter> set = CollectionFactory.newTroveSet(parameters);
    if (LocalAopModel.isJoinPointParamer(parameters[0])) {
      set.remove(parameters[0]);
      if (set.isEmpty()) return true;
    }

    if (manipulator.getThrowingReference() != null && containsOnlyOneParameter(method, set, CommonClassNames.JAVA_LANG_THROWABLE)) {
      return true;
    }

    final Class<PsiAtPointcutDesignator> designatorClass = PsiAtPointcutDesignator.class;
    List<PsiParameter> shouldBeAnnos = findParametersUsedInPointcuts(set, designatorClass);
    if (shouldBeAnnos.size() == 1 && containsOnlyOneParameter(method, set, CommonClassNames.JAVA_LANG_ANNOTATION_ANNOTATION)) return true;

    if (manipulator.getReturningReference() != null) {
      return set.size() == 1;
    }

    List<PsiParameter> canBePrimitive = findParametersUsedInPointcuts(set, PsiArgsExpression.class);
    if (canBePrimitive.size() == 1) {
      final List<PsiParameter> primitives = ContainerUtil.findAll(set, new Condition<PsiParameter>() {
        public boolean value(final PsiParameter psiParameter) {
          return psiParameter.getType() instanceof PsiPrimitiveType;
        }
      });
      if (primitives.size() == 1) {
        set.removeAll(primitives);

        if (set.isEmpty()) return true;
      }
    }

    if (set.size() == 1) {
      if (canBePrimitive.size() == 1) return true;
      if (findParametersUsedInPointcuts(set, PsiThisExpression.class).size() == 1) return true;
      if (findParametersUsedInPointcuts(set, PsiTargetExpression.class).size() == 1) return true;
    }

    return false;
  }

  private static List<PsiParameter> findParametersUsedInPointcuts(final Set<PsiParameter> set, final Class<?> designatorClass) {
    return ContainerUtil.findAll(set, new Condition<PsiParameter>() {
      public boolean value(final PsiParameter psiParameter) {
        return !ReferencesSearch.search(psiParameter).forEach(new Processor<PsiReference>() {
          public boolean process(final PsiReference reference) {
            if (reference instanceof AopReferenceExpression) {
              if (designatorClass.isInstance(PsiTreeUtil.getParentOfType((AopReferenceExpression)reference, PsiPointcutExpression.class))) {
                return false;
              }
            }
            return true;
          }
        });
      }
    });
  }

  private static boolean containsOnlyOneParameter(final PsiMethod method, final Set<PsiParameter> set, final String className) {
    final PsiClassType baseType = JavaPsiFacade.getInstance(method.getManager().getProject()).getElementFactory()
      .createTypeByFQClassName(className, method.getResolveScope());
    List<PsiParameter> instanceofs = ContainerUtil.findAll(set, new Condition<PsiParameter>() {
      public boolean value(final PsiParameter psiParameter) {
        return baseType.isAssignableFrom(psiParameter.getType());
      }
    });
    if (instanceofs.size() == 1) {
      set.removeAll(instanceofs);
      if (set.isEmpty()) return true;
    }
    return false;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return AopBundle.message("inspection.display.name.argNames.warnings");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "ArgNamesWarningsInspection";
  }
}