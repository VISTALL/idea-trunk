/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.ArgNamesManipulator;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class SetArgNamesQuickFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance("#com.intellij.aop.jam.SetArgNamesQuickFix");
  private final String myName;
  private final boolean mySet;
  private final ArgNamesManipulator myManipulator;
  private final PsiMethod myMethod;

  public SetArgNamesQuickFix(final String name, final boolean set, final ArgNamesManipulator manipulator, final PsiMethod method) {
    myName = name;
    mySet = set;
    myManipulator = manipulator;
    myMethod = method;
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    try {
      CheckUtil.checkWritable(descriptor.getPsiElement());
      if (mySet) {
        final PsiMethod method = myMethod;
        StringBuilder result = new StringBuilder();
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
          final String name = parameter.getName();
          if (result.length() > 0) result.append(",");
          result.append(name);
        }
        myManipulator.setArgNames(result.toString());
      } else {
        myManipulator.setArgNames(null);
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  @NotNull
  public String getFamilyName() {
    return myName;
  }

  @NotNull
  public String getName() {
    return myName;
  }
}
