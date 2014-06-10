/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.lang.pratt.PrattBuilder;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
*/
public abstract class PointcutDescriptor {
  private final String myTokenText;

  protected PointcutDescriptor(@NonNls final String tokenText) {
    myTokenText = tokenText;
  }

  public String getTokenText() {
    return myTokenText;
  }

  public abstract void parseToken(final PrattBuilder builder);
  public abstract PsiPointcutExpression createPsi(final ASTNode node);
}
