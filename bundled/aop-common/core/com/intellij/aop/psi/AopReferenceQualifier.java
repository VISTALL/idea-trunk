/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

/**
 * @author peter
 */
public interface AopReferenceQualifier extends AopTypeExpression {
  AopReferenceExpression.Resolvability getResolvability();

}
