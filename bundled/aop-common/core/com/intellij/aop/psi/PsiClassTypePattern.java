/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.*;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author peter
 */
public class PsiClassTypePattern extends AopPsiTypePattern {
  private final String myText;
  private final Pattern myPattern;
  private final String myRegex;

  public PsiClassTypePattern(final String pattern) {
    assert !"*".equals(pattern);
    myText = pattern;
    myRegex = "*".equals(pattern) ? ".*" : pattern.
      replaceAll("([\\[\\]\\^\\(\\)\\{\\}\\-])", "\\\\$1").
      replaceAll("\\*", "\\[\\^\\\\.]\\*").
      replaceAll("\\.", "\\\\.").
      replaceAll("\\\\.\\\\.", "\\(\\\\.\\|\\\\.\\.\\*\\\\.\\)");
    myPattern = Pattern.compile(myRegex);
  }

  public String getText() {
    return myText;
  }

  public String getRegex() {
    return myRegex;
  }

  public String toString() {
    return "PsiClassTypePattern:" + myText;
  }

  public boolean accepts(@NotNull final String qualifiedName) {
    return myPattern.matcher(qualifiedName).matches();
  }

  public boolean processPackages(final PsiManager manager, final Processor<PsiPackage> processor) {
    int asterisk = myText.indexOf('*');
    if (asterisk < 0) asterisk = Integer.MAX_VALUE;

    int i = 0;
    while (true) {
      final String prefix = myText.substring(0, i);
      final PsiPackage psiPackage = JavaPsiFacade.getInstance(manager.getProject()).findPackage(prefix);
      if (psiPackage == null) return true;

      int i1 = myText.indexOf('.', i + 1);
      if (i1 == i + 1) return processSubPackages(psiPackage, processor);

      if (!processor.process(psiPackage)) return false;

      if (i1 > asterisk) return true;
      if (i == myText.length()) return true;

      i = i1 < 0 ? myText.length() : i1;
    }
  }

  public boolean accepts(@NotNull PsiType type) {
    if (type instanceof PsiClassType) {
      final PsiClassType classType = (PsiClassType)type;
      final PsiClass psiClass = classType.resolve();
      if (psiClass != null) {
        final String qname = psiClass.getQualifiedName();
        if (qname != null && accepts(qname)) {
          return true;
        }
      }
    }
    return false;
  }
}
